package com.polaris.fusion.infrastructure.externo;

import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.FuenteAlimento;
import com.polaris.fusion.domain.model.ResultadoCatalogoAlimento;
import com.polaris.shared.error.ExternalServiceException;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Las respuestas son recortes con la forma real de Open Food Facts. Lo que mas
 * se prueba es el filtro de calidad: nunca debe salir un dato que no se
 * guardaria.
 */
class OpenFoodFactsAdapterTest {

    private static final String URL_BASE = "https://world.openfoodfacts.org";

    private MockRestServiceServer servidor;

    private OpenFoodFactsAdapter adaptador() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        return new OpenFoodFactsAdapter(builder, URL_BASE, "Polaris/tests");
    }

    private static String producto(String nombre, String nutrientes) {
        return "{\"code\":\"111\",\"product_name\":" + (nombre == null ? "null" : "\"" + nombre + "\"")
                + ",\"brands\":\"Ferrero, Nutella\",\"nutriments\":{" + nutrientes + "}}";
    }

    private static final String COMPLETOS = """
            "energy-kcal_100g":539,"proteins_100g":6.3,"carbohydrates_100g":57.5,"fat_100g":30.9""";

    private void respuestaBusqueda(String... productos) {
        servidor.expect(requestTo(startsWith(URL_BASE + "/cgi/search.pl")))
                .andExpect(header("User-Agent", "Polaris/tests"))
                .andRespond(withSuccess("{\"count\":1,\"products\":[" + String.join(",", productos) + "]}",
                        MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("buscar mapea nombre, primera marca y macros, y se identifica con User-Agent")
    void buscarMapeaLosCampos() {
        OpenFoodFactsAdapter adaptador = adaptador();
        respuestaBusqueda(producto("Nutella", COMPLETOS));

        List<ResultadoCatalogoAlimento> resultados = adaptador.buscar("nutella");

        assertThat(resultados).hasSize(1);
        ResultadoCatalogoAlimento r = resultados.getFirst();
        assertThat(r.getFuenteExterna()).isEqualTo(FuenteAlimento.OPEN_FOOD_FACTS);
        assertThat(r.getIdExterno()).isEqualTo("111");
        assertThat(r.getNombre()).isEqualTo("Nutella");
        assertThat(r.getMarca()).isEqualTo("Ferrero");
        assertThat(r.getKcal100g()).isEqualByComparingTo("539");
        assertThat(r.getProteinas100g()).isEqualByComparingTo("6.3");
        assertThat(r.getCarbohidratos100g()).isEqualByComparingTo("57.5");
        assertThat(r.getGrasas100g()).isEqualByComparingTo("30.9");
        servidor.verify();
    }

    @Test
    @DisplayName("buscar codifica el texto y pide el formato json")
    void buscarCodificaElTexto() {
        OpenFoodFactsAdapter adaptador = adaptador();
        servidor.expect(requestTo(containsString("search_terms=leche%20desnatada")))
                .andExpect(requestTo(containsString("json=1")))
                .andRespond(withSuccess("{\"products\":[]}", MediaType.APPLICATION_JSON));

        assertThat(adaptador.buscar("leche desnatada")).isEmpty();
        servidor.verify();
    }

    @Test
    @DisplayName("buscar descarta productos sin nombre, sin nombre util o sin alguno de los cuatro valores")
    void buscarDescartaLosIncompletos() {
        OpenFoodFactsAdapter adaptador = adaptador();
        respuestaBusqueda(
                producto(null, COMPLETOS),
                producto("  ", COMPLETOS),
                producto("Sin grasas", "\"energy-kcal_100g\":100,\"proteins_100g\":1,\"carbohydrates_100g\":1"),
                producto("Sin kcal", "\"proteins_100g\":1,\"carbohydrates_100g\":1,\"fat_100g\":1"),
                "{\"code\":\"222\",\"product_name\":\"Sin nutrientes\"}",
                producto("Bueno", COMPLETOS));

        List<ResultadoCatalogoAlimento> resultados = adaptador.buscar("x");

        assertThat(resultados).extracting(ResultadoCatalogoAlimento::getNombre).containsExactly("Bueno");
    }

    @Test
    @DisplayName("buscar descarta valores fuera de rango (macros 0-100, kcal 0-900) y negativos")
    void buscarDescartaFueraDeRango() {
        OpenFoodFactsAdapter adaptador = adaptador();
        respuestaBusqueda(
                producto("Kcal 901", "\"energy-kcal_100g\":901,\"proteins_100g\":1,\"carbohydrates_100g\":1,\"fat_100g\":1"),
                producto("Prote 101", "\"energy-kcal_100g\":100,\"proteins_100g\":101,\"carbohydrates_100g\":1,\"fat_100g\":1"),
                producto("Negativo", "\"energy-kcal_100g\":100,\"proteins_100g\":-1,\"carbohydrates_100g\":1,\"fat_100g\":1"),
                producto("Limite", "\"energy-kcal_100g\":900,\"proteins_100g\":100,\"carbohydrates_100g\":0,\"fat_100g\":100"));

        assertThat(adaptador.buscar("x")).extracting(ResultadoCatalogoAlimento::getNombre)
                .containsExactly("Limite");
    }

    @Test
    @DisplayName("sin kcal usa los kJ divididos entre 4,184 (energy-kj_100g y, si no, energy_100g)")
    void convierteKilojulios() {
        OpenFoodFactsAdapter adaptador = adaptador();
        respuestaBusqueda(
                producto("Con kj", "\"energy-kj_100g\":1046,\"proteins_100g\":1,\"carbohydrates_100g\":1,\"fat_100g\":1"),
                producto("Con energy", "\"energy_100g\":2092,\"proteins_100g\":1,\"carbohydrates_100g\":1,\"fat_100g\":1"));

        List<ResultadoCatalogoAlimento> resultados = adaptador.buscar("x");

        assertThat(resultados).hasSize(2);
        assertThat(resultados.get(0).getKcal100g()).isEqualByComparingTo("250.00");
        assertThat(resultados.get(1).getKcal100g()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("kJ que convertidos superan 900 kcal se descartan")
    void kilojulioFueraDeRango() {
        OpenFoodFactsAdapter adaptador = adaptador();
        respuestaBusqueda(producto("Absurdo",
                "\"energy_100g\":9000,\"proteins_100g\":1,\"carbohydrates_100g\":1,\"fat_100g\":1"));

        assertThat(adaptador.buscar("x")).isEmpty();
    }

    @Test
    @DisplayName("acepta valores como cadena numerica, redondea a 2 decimales y trata lo ilegible como ausente")
    void valoresComoCadena() {
        OpenFoodFactsAdapter adaptador = adaptador();
        respuestaBusqueda(
                producto("Cadenas", "\"energy-kcal_100g\":\"250.456\",\"proteins_100g\":\"3\","
                        + "\"carbohydrates_100g\":\"1.005\",\"fat_100g\":2"),
                producto("Ilegible", "\"energy-kcal_100g\":\"abc\",\"proteins_100g\":1,"
                        + "\"carbohydrates_100g\":1,\"fat_100g\":1"));

        List<ResultadoCatalogoAlimento> resultados = adaptador.buscar("x");

        assertThat(resultados).hasSize(1);
        assertThat(resultados.getFirst().getKcal100g()).isEqualTo(new BigDecimal("250.46"));
        assertThat(resultados.getFirst().getCarbohidratos100g()).isEqualTo(new BigDecimal("1.01"));
    }

    @Test
    @DisplayName("recorta nombre a 150 y marca a 100 caracteres; marca vacia queda a null")
    void recortaTextos() {
        OpenFoodFactsAdapter adaptador = adaptador();
        respuestaBusqueda(
                "{\"code\":\"1\",\"product_name\":\"" + "n".repeat(200) + "\",\"brands\":\"" + "m".repeat(150)
                        + "\",\"nutriments\":{" + COMPLETOS + "}}",
                "{\"code\":\"2\",\"product_name\":\"Sin marca\",\"brands\":\" , \",\"nutriments\":{" + COMPLETOS + "}}");

        List<ResultadoCatalogoAlimento> resultados = adaptador.buscar("x");

        assertThat(resultados.get(0).getNombre()).hasSize(150);
        assertThat(resultados.get(0).getMarca()).hasSize(100);
        assertThat(resultados.get(1).getMarca()).isNull();
    }

    @Test
    @DisplayName("buscar devuelve como mucho 20 resultados")
    void buscarLimitaA20() {
        OpenFoodFactsAdapter adaptador = adaptador();
        String[] treinta = new String[30];
        for (int i = 0; i < treinta.length; i++) {
            treinta[i] = "{\"code\":\"" + i + "\",\"product_name\":\"P" + i + "\",\"nutriments\":{" + COMPLETOS + "}}";
        }
        respuestaBusqueda(treinta);

        assertThat(adaptador.buscar("x")).hasSize(20);
    }

    @Test
    @DisplayName("buscar con JSON vacio, sin products o con products no array devuelve lista vacia")
    void buscarJsonVacio() {
        OpenFoodFactsAdapter adaptador = adaptador();
        servidor.expect(requestTo(startsWith(URL_BASE + "/cgi/search.pl")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        servidor.expect(requestTo(startsWith(URL_BASE + "/cgi/search.pl")))
                .andRespond(withSuccess("{\"products\":[]}", MediaType.APPLICATION_JSON));
        servidor.expect(requestTo(startsWith(URL_BASE + "/cgi/search.pl")))
                .andRespond(withSuccess("{\"products\":\"raro\"}", MediaType.APPLICATION_JSON));

        assertThat(adaptador.buscar("a")).isEmpty();
        assertThat(adaptador.buscar("b")).isEmpty();
        assertThat(adaptador.buscar("c")).isEmpty();
    }

    @Test
    @DisplayName("buscar con error HTTP de OFF lanza ExternalServiceException (502)")
    void buscarErrorHttp() {
        OpenFoodFactsAdapter adaptador = adaptador();
        servidor.expect(requestTo(startsWith(URL_BASE + "/cgi/search.pl")))
                .andRespond(withServerError());
        servidor.expect(requestTo(startsWith(URL_BASE + "/cgi/search.pl")))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> adaptador.buscar("x")).isInstanceOf(ExternalServiceException.class);
        assertThatThrownBy(() -> adaptador.buscar("x")).isInstanceOf(ExternalServiceException.class);
    }

    @Test
    @DisplayName("buscar con una respuesta que no es JSON (OFF sirve HTML al saturarse) lanza ExternalServiceException")
    void buscarRespuestaIlegible() {
        OpenFoodFactsAdapter adaptador = adaptador();
        servidor.expect(requestTo(startsWith(URL_BASE + "/cgi/search.pl")))
                .andRespond(withSuccess("<html>overloaded</html>", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adaptador.buscar("x")).isInstanceOf(ExternalServiceException.class);
    }

    @Test
    @DisplayName("obtener devuelve la ficha completa desde /api/v2/product/{codigo}.json")
    void obtenerDevuelveLaFicha() {
        OpenFoodFactsAdapter adaptador = adaptador();
        servidor.expect(requestTo(startsWith(URL_BASE + "/api/v2/product/3017620422003.json")))
                .andExpect(header("User-Agent", "Polaris/tests"))
                .andRespond(withSuccess("{\"code\":\"3017620422003\",\"status\":1,\"product\":"
                        + producto("Nutella", COMPLETOS) + "}", MediaType.APPLICATION_JSON));

        Alimento alimento = adaptador.obtener("3017620422003");

        assertThat(alimento.getNombre()).isEqualTo("Nutella");
        assertThat(alimento.getMarca()).isEqualTo("Ferrero");
        assertThat(alimento.getKcal100g()).isEqualByComparingTo("539");
        assertThat(alimento.getFuenteExterna()).isEqualTo(FuenteAlimento.OPEN_FOOD_FACTS);
        assertThat(alimento.getIdExterno()).isEqualTo("3017620422003");
    }

    @Test
    @DisplayName("obtener con un producto sin nombre, sin macros o fuera de rango lanza ValidationException (400)")
    void obtenerNoImportableEs400() {
        OpenFoodFactsAdapter adaptador = adaptador();
        List<String> productos = List.of(
                producto(null, COMPLETOS),
                producto("Sin fat", "\"energy-kcal_100g\":1,\"proteins_100g\":1,\"carbohydrates_100g\":1"),
                producto("Fuera", "\"energy-kcal_100g\":1,\"proteins_100g\":150,\"carbohydrates_100g\":1,\"fat_100g\":1"));
        for (String p : productos) {
            servidor.expect(requestTo(startsWith(URL_BASE + "/api/v2/product/1.json")))
                    .andRespond(withSuccess("{\"status\":1,\"product\":" + p + "}", MediaType.APPLICATION_JSON));
        }
        for (int i = 0; i < productos.size(); i++) {
            assertThatThrownBy(() -> adaptador.obtener("1")).isInstanceOf(ValidationException.class);
        }
    }

    @Test
    @DisplayName("obtener un codigo inexistente (404 o status 0 sin producto) es ValidationException")
    void obtenerInexistenteEs400() {
        OpenFoodFactsAdapter adaptador = adaptador();
        servidor.expect(requestTo(startsWith(URL_BASE + "/api/v2/product/999.json")))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"status\":0,\"status_verbose\":\"product not found\"}"));
        servidor.expect(requestTo(startsWith(URL_BASE + "/api/v2/product/998.json")))
                .andRespond(withSuccess("{\"status\":0}", MediaType.APPLICATION_JSON));
        servidor.expect(requestTo(startsWith(URL_BASE + "/api/v2/product/997.json")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adaptador.obtener("999")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> adaptador.obtener("998")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> adaptador.obtener("997")).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("obtener con error 5xx o 429 de OFF lanza ExternalServiceException (502)")
    void obtenerErrorHttpEs502() {
        OpenFoodFactsAdapter adaptador = adaptador();
        servidor.expect(requestTo(startsWith(URL_BASE + "/api/v2/product/1.json")))
                .andRespond(withServerError());
        servidor.expect(requestTo(startsWith(URL_BASE + "/api/v2/product/1.json")))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> adaptador.obtener("1")).isInstanceOf(ExternalServiceException.class);
        assertThatThrownBy(() -> adaptador.obtener("1")).isInstanceOf(ExternalServiceException.class);
    }

    @Test
    @DisplayName("obtener rechaza ids que no son un codigo de barras, sin llamar a OFF")
    void obtenerRechazaIdsRaros() {
        OpenFoodFactsAdapter adaptador = adaptador();

        assertThatThrownBy(() -> adaptador.obtener("../etc/passwd")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> adaptador.obtener("12a")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> adaptador.obtener(null)).isInstanceOf(ValidationException.class);
        servidor.verify();
    }

    @Test
    @DisplayName("la fuente es OPEN_FOOD_FACTS")
    void fuente() {
        assertThat(adaptador().fuente()).isEqualTo(FuenteAlimento.OPEN_FOOD_FACTS);
    }
}
