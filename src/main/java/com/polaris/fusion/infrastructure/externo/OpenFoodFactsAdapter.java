package com.polaris.fusion.infrastructure.externo;

import com.fasterxml.jackson.databind.JsonNode;
import com.polaris.fusion.application.out.CatalogoAlimentoExternoPort;
import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.FuenteAlimento;
import com.polaris.fusion.domain.model.ResultadoCatalogoAlimento;
import com.polaris.shared.error.ExternalServiceException;
import com.polaris.shared.error.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Open Food Facts como catalogo de alimentos. Gratis, abierta y sin clave; ver
 * docs/decisiones/018-alimentos-open-food-facts.md.
 *
 * <p>OFF es colaborativa y su calidad es irregular, asi que este adaptador es
 * el filtro: <b>nunca devuelve datos que Polaris no guardaria</b>.
 * <ul>
 *   <li>Sin nombre, o sin alguno de los cuatro valores, o con alguno fuera de
 *       rango (macros 0-100, kcal 0-900): en la busqueda se descarta el
 *       resultado; al importar es un 400.</li>
 *   <li>Kcal: se usa {@code energy-kcal_100g}. Si falta, se convierte desde los
 *       kJ ({@code energy-kj_100g} o {@code energy_100g}, que en OFF va en kJ)
 *       dividiendo entre 4,184.</li>
 *   <li>Nombre y marca se recortan a 150 y 100 caracteres (las columnas); de
 *       la marca solo se guarda la primera, OFF las devuelve separadas por
 *       comas.</li>
 * </ul>
 * Cualquier fallo de red, timeout, HTTP no 2xx o JSON ilegible es un 502.
 * OFF exige un User-Agent que identifique a quien llama.
 */
@Component
public class OpenFoodFactsAdapter implements CatalogoAlimentoExternoPort {

    private static final int MAX_RESULTADOS = 20;

    /** Se piden mas de los que se devuelven: los descartados por calidad no cuentan. */
    private static final int TAMANO_PAGINA = 40;

    private static final String CAMPOS = "code,product_name,brands,nutriments";

    private static final int MAX_NOMBRE = 150;
    private static final int MAX_MARCA = 100;

    private static final BigDecimal KJ_POR_KCAL = new BigDecimal("4.184");
    private static final BigDecimal MAX_KCAL = new BigDecimal("900");
    private static final BigDecimal MAX_MACRO = new BigDecimal("100");

    private final RestClient cliente;

    @Autowired
    public OpenFoodFactsAdapter(
            RestClient.Builder builder,
            @Value("${polaris.openfoodfacts.url-base}") String urlBase,
            @Value("${polaris.openfoodfacts.user-agent}") String userAgent,
            @Value("${polaris.openfoodfacts.timeout-segundos}") int timeoutSegundos) {
        this(builder.requestFactory(factoria(Duration.ofSeconds(timeoutSegundos))), urlBase, userAgent);
    }

    /** Sin factoria propia: los tests inyectan la suya (MockRestServiceServer). */
    OpenFoodFactsAdapter(RestClient.Builder builder, String urlBase, String userAgent) {
        this.cliente = builder
                .baseUrl(urlBase)
                .defaultHeader("User-Agent", userAgent)
                .build();
    }

    private static JdkClientHttpRequestFactory factoria(Duration timeout) {
        JdkClientHttpRequestFactory factoria = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(timeout).build());
        factoria.setReadTimeout(timeout);
        return factoria;
    }

    @Override
    public FuenteAlimento fuente() {
        return FuenteAlimento.OPEN_FOOD_FACTS;
    }

    @Override
    public List<ResultadoCatalogoAlimento> buscar(String texto) {
        JsonNode respuesta = leer(() -> cliente.get()
                .uri(uri -> uri
                        .path("/cgi/search.pl")
                        .queryParam("search_terms", "{texto}")
                        .queryParam("search_simple", 1)
                        .queryParam("action", "process")
                        .queryParam("json", 1)
                        .queryParam("page_size", TAMANO_PAGINA)
                        .queryParam("fields", CAMPOS)
                        .build(texto))
                .retrieve()
                .body(JsonNode.class));

        List<ResultadoCatalogoAlimento> resultados = new ArrayList<>();
        JsonNode productos = respuesta == null ? null : respuesta.get("products");
        if (productos == null || !productos.isArray()) {
            return resultados;
        }

        for (JsonNode producto : productos) {
            String codigo = texto(producto.get("code"));
            if (codigo == null) {
                continue;
            }
            convertir(producto, codigo)
                    .map(this::aResultado)
                    .ifPresent(resultados::add);
            if (resultados.size() == MAX_RESULTADOS) {
                break;
            }
        }
        return resultados;
    }

    @Override
    public Alimento obtener(String idExterno) {
        // El id va en la ruta: solo se admiten codigos de barras (digitos).
        if (idExterno == null || !idExterno.matches("\\d{1,20}")) {
            throw new ValidationException("El idExterno de Open Food Facts es un codigo de barras (solo digitos)");
        }

        JsonNode respuesta;
        try {
            respuesta = cliente.get()
                    .uri("/api/v2/product/{codigo}.json?fields={campos}", idExterno, CAMPOS)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ValidationException("Open Food Facts no tiene el producto " + idExterno);
        } catch (RestClientException e) {
            throw new ExternalServiceException("Open Food Facts no ha respondido correctamente", e);
        }

        JsonNode producto = respuesta == null ? null : respuesta.get("product");
        if (producto == null || producto.isNull() || producto.isEmpty()) {
            throw new ValidationException("Open Food Facts no tiene el producto " + idExterno);
        }

        return convertir(producto, idExterno).orElseThrow(() -> new ValidationException(
                "El producto " + idExterno + " no se puede importar: en Open Food Facts le falta el nombre, "
                        + "alguno de los cuatro valores nutricionales por 100 g, o alguno esta fuera de rango"));
    }

    private JsonNode leer(java.util.function.Supplier<JsonNode> llamada) {
        try {
            return llamada.get();
        } catch (RestClientException e) {
            throw new ExternalServiceException("Open Food Facts no ha respondido correctamente", e);
        }
    }

    /** Empty si el producto no cumple: sin nombre, sin los 4 valores, o fuera de rango. */
    private Optional<Alimento> convertir(JsonNode producto, String codigo) {
        String nombre = texto(producto.get("product_name"));
        JsonNode nutrientes = producto.get("nutriments");
        if (nombre == null || nutrientes == null || !nutrientes.isObject()) {
            return Optional.empty();
        }

        BigDecimal kcal = enRango(kcal(nutrientes), MAX_KCAL);
        BigDecimal proteinas = enRango(numero(nutrientes.get("proteins_100g")), MAX_MACRO);
        BigDecimal carbohidratos = enRango(numero(nutrientes.get("carbohydrates_100g")), MAX_MACRO);
        BigDecimal grasas = enRango(numero(nutrientes.get("fat_100g")), MAX_MACRO);
        if (kcal == null || proteinas == null || carbohidratos == null || grasas == null) {
            return Optional.empty();
        }

        return Optional.of(Alimento.builder()
                .nombre(recortar(nombre, MAX_NOMBRE))
                .marca(marca(texto(producto.get("brands"))))
                .kcal100g(kcal)
                .proteinas100g(proteinas)
                .carbohidratos100g(carbohidratos)
                .grasas100g(grasas)
                .fuenteExterna(FuenteAlimento.OPEN_FOOD_FACTS)
                .idExterno(codigo)
                .build());
    }

    private ResultadoCatalogoAlimento aResultado(Alimento alimento) {
        return ResultadoCatalogoAlimento.builder()
                .fuenteExterna(alimento.getFuenteExterna())
                .idExterno(alimento.getIdExterno())
                .nombre(alimento.getNombre())
                .marca(alimento.getMarca())
                .kcal100g(alimento.getKcal100g())
                .proteinas100g(alimento.getProteinas100g())
                .carbohidratos100g(alimento.getCarbohidratos100g())
                .grasas100g(alimento.getGrasas100g())
                .build();
    }

    /** kcal directas; si faltan, los kJ divididos entre 4,184. */
    private BigDecimal kcal(JsonNode nutrientes) {
        BigDecimal kcal = numero(nutrientes.get("energy-kcal_100g"));
        if (kcal != null) {
            return kcal;
        }

        BigDecimal kj = numero(nutrientes.get("energy-kj_100g"));
        if (kj == null) {
            kj = numero(nutrientes.get("energy_100g"));
        }
        return kj == null ? null : kj.divide(KJ_POR_KCAL, 4, RoundingMode.HALF_UP);
    }

    /** null si falta o esta fuera de [0, max]; con dos decimales, como la columna. */
    private BigDecimal enRango(BigDecimal valor, BigDecimal max) {
        if (valor == null) {
            return null;
        }

        BigDecimal redondeado = valor.setScale(2, RoundingMode.HALF_UP);
        if (redondeado.signum() < 0 || redondeado.compareTo(max) > 0) {
            return null;
        }
        return redondeado;
    }

    /** OFF mezcla numeros y cadenas numericas; lo ilegible cuenta como ausente. */
    private BigDecimal numero(JsonNode nodo) {
        if (nodo == null || nodo.isNull()) {
            return null;
        }

        try {
            if (nodo.isNumber()) {
                return nodo.decimalValue();
            }
            if (nodo.isTextual() && !nodo.asText().isBlank()) {
                return new BigDecimal(nodo.asText().trim());
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    private String texto(JsonNode nodo) {
        if (nodo == null || !nodo.isTextual() || nodo.asText().isBlank()) {
            return null;
        }
        return nodo.asText().trim();
    }

    /** OFF devuelve "Nestle, Nutella"; se guarda la primera. */
    private String marca(String marcas) {
        if (marcas == null) {
            return null;
        }

        for (String marca : marcas.split(",")) {
            if (!marca.isBlank()) {
                return recortar(marca.trim(), MAX_MARCA);
            }
        }
        return null;
    }

    private String recortar(String texto, int maximo) {
        return texto.length() > maximo ? texto.substring(0, maximo) : texto;
    }
}
