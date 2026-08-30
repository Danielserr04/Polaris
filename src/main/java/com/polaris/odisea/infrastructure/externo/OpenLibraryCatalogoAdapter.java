package com.polaris.odisea.infrastructure.externo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.polaris.odisea.application.out.CatalogoExternoPort;
import com.polaris.odisea.domain.model.FuenteExterna;
import com.polaris.odisea.domain.model.ResultadoCatalogo;
import com.polaris.odisea.domain.model.TipoContenido;
import com.polaris.odisea.domain.model.Titulo;
import com.polaris.shared.error.ExternalServiceException;
import com.polaris.shared.error.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * OpenLibrary para libros. Se eligio sobre Google Books porque no necesita
 * ninguna clave: es el unico adaptador que no obliga a pasar por una consola.
 *
 * <p><b>Solo se usa el endpoint de busqueda</b>, tambien para pedir una ficha
 * concreta ({@code q=key:/works/OL...}). El de detalle de un work parecia lo
 * natural, pero devuelve documentos de tipo redirect cuando OpenLibrary ha
 * fusionado dos obras, y ademas no trae el numero de paginas, que vive en las
 * ediciones. La busqueda devuelve todo de una vez y siempre la clave canonica.
 *
 * <p>OpenLibrary pide un User-Agent que identifique a quien llama, y limita el
 * trafico anonimo.
 */
@Component
public class OpenLibraryCatalogoAdapter implements CatalogoExternoPort {

    private static final int MAX_RESULTADOS = 20;

    /** Sin esto la respuesta trae decenas de campos que no se usan. */
    private static final String CAMPOS =
            "key,title,author_name,first_publish_year,cover_i,number_of_pages_median,subject";

    /** generos es un varchar(255) y OpenLibrary devuelve materias a decenas. */
    private static final int MAX_GENEROS = 5;
    private static final int MAX_LONGITUD_GENEROS = 255;

    private final RestClient cliente;
    private final String imagenBase;

    public OpenLibraryCatalogoAdapter(
            RestClient.Builder builder,
            @Value("${polaris.openlibrary.url-base}") String urlBase,
            @Value("${polaris.openlibrary.imagen-base}") String imagenBase,
            @Value("${polaris.openlibrary.user-agent}") String userAgent) {
        this.cliente = builder
                .baseUrl(urlBase)
                .defaultHeader("User-Agent", userAgent)
                .build();
        this.imagenBase = imagenBase;
    }

    @Override
    public boolean soporta(TipoContenido tipo) {
        return tipo == TipoContenido.LIBRO;
    }

    @Override
    public FuenteExterna fuente() {
        return FuenteExterna.OPEN_LIBRARY;
    }

    @Override
    public List<ResultadoCatalogo> buscar(String texto, TipoContenido tipo) {
        return consultar(texto, MAX_RESULTADOS).stream()
                .map(this::aResultado)
                .toList();
    }

    @Override
    public Titulo obtener(String idExterno, TipoContenido tipo) {
        LibroOpenLibrary libro = consultar("key:/works/" + idExterno, 1).stream()
                .findFirst()
                .orElseThrow(() -> new ExternalServiceException(
                        "OpenLibrary no devolvio la ficha " + idExterno));

        return Titulo.builder()
                .tipo(TipoContenido.LIBRO)
                .titulo(libro.title())
                // OpenLibrary no distingue titulo original del traducido.
                .tituloOriginal(null)
                .anio(libro.firstPublishYear())
                .sinopsis(autores(libro))
                .imagenUrl(imagen(libro.coverI()))
                .generos(generos(libro))
                // duracion_min son paginas en un libro, y eso si lo tiene.
                .duracionMin(libro.numberOfPagesMedian())
                .fuenteExterna(FuenteExterna.OPEN_LIBRARY)
                .idExterno(idExterno)
                .build();
    }

    private ResultadoCatalogo aResultado(LibroOpenLibrary libro) {
        return ResultadoCatalogo.builder()
                .fuenteExterna(FuenteExterna.OPEN_LIBRARY)
                .idExterno(idDesdeClave(libro.key()))
                .tipo(TipoContenido.LIBRO)
                .titulo(libro.title())
                .anio(libro.firstPublishYear())
                .sinopsis(autores(libro))
                .imagenUrl(imagen(libro.coverI()))
                .build();
    }

    private List<LibroOpenLibrary> consultar(String consulta, int limite) {
        try {
            RespuestaBusqueda respuesta = cliente.get()
                    .uri(uri -> uri
                            .path("/search.json")
                            .queryParam("q", consulta)
                            .queryParam("fields", CAMPOS)
                            .queryParam("limit", limite)
                            .build())
                    .retrieve()
                    .body(RespuestaBusqueda.class);

            return respuesta == null || respuesta.docs() == null ? List.of() : respuesta.docs();
        } catch (HttpClientErrorException e) {
            // OpenLibrary contesta 422 a busquedas que no sabe resolver, como
            // una sola palabra corriente ("el"). Eso no es que la API este
            // rota, es que la consulta no vale: 400 y no 502.
            throw new ValidationException(
                    "OpenLibrary no admite esa busqueda. Prueba con el titulo o el autor completos");
        } catch (RestClientException e) {
            throw new ExternalServiceException("OpenLibrary no ha respondido correctamente", e);
        }
    }

    /** La clave viene como "/works/OL893414W"; se guarda solo el identificador. */
    private String idDesdeClave(String clave) {
        if (clave == null) {
            return null;
        }

        int barra = clave.lastIndexOf('/');
        return barra < 0 ? clave : clave.substring(barra + 1);
    }

    /**
     * OpenLibrary no da sinopsis en la busqueda, y la del work llega unas veces
     * como texto y otras como objeto. En vez de pelearse con eso, aqui se guarda
     * la autoria, que es lo que de verdad quieres ver en una lista de libros.
     */
    private String autores(LibroOpenLibrary libro) {
        if (libro.authorName() == null || libro.authorName().isEmpty()) {
            return null;
        }

        return String.join(", ", libro.authorName());
    }

    /** cover_i es un id numerico; -L es el tamano grande. */
    private String imagen(Integer coverI) {
        return coverI == null ? null : imagenBase + coverI + "-L.jpg";
    }

    /**
     * Las materias de OpenLibrary son una lista larga y sucia: mezcla generos
     * de verdad con etiquetas internas tipo "nyt:trade-fiction-paperback=2021".
     * Se filtran las que llevan ':' o '=' y se corta a los primeros cinco.
     */
    private String generos(LibroOpenLibrary libro) {
        if (libro.subject() == null || libro.subject().isEmpty()) {
            return null;
        }

        String generos = libro.subject().stream()
                .filter(Objects::nonNull)
                .filter(materia -> !materia.contains(":") && !materia.contains("="))
                .distinct()
                .limit(MAX_GENEROS)
                .collect(Collectors.joining(", "));

        if (generos.isBlank()) {
            return null;
        }

        return generos.length() > MAX_LONGITUD_GENEROS
                ? generos.substring(0, MAX_LONGITUD_GENEROS)
                : generos;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RespuestaBusqueda(List<LibroOpenLibrary> docs) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LibroOpenLibrary(
            String key,
            String title,
            @JsonProperty("author_name") List<String> authorName,
            @JsonProperty("first_publish_year") Integer firstPublishYear,
            @JsonProperty("cover_i") Integer coverI,
            @JsonProperty("number_of_pages_median") Integer numberOfPagesMedian,
            List<String> subject) {
    }
}
