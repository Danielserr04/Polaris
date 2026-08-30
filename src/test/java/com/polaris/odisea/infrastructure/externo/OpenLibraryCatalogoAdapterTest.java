package com.polaris.odisea.infrastructure.externo;

import com.polaris.odisea.domain.model.FuenteExterna;
import com.polaris.odisea.domain.model.ResultadoCatalogo;
import com.polaris.odisea.domain.model.TipoContenido;
import com.polaris.odisea.domain.model.Titulo;
import com.polaris.shared.error.ExternalServiceException;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

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
 * Las respuestas de estos tests son recortes reales de openlibrary.org. Lo que
 * mas se prueba es la limpieza de las materias: vienen a decenas y mezcladas
 * con etiquetas internas, y generos es un varchar(255).
 */
class OpenLibraryCatalogoAdapterTest {

    private static final String URL_BASE = "https://openlibrary.org";
    private static final String IMAGEN_BASE = "https://covers.openlibrary.org/b/id/";

    private MockRestServiceServer servidor;

    private OpenLibraryCatalogoAdapter adaptador() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        return new OpenLibraryCatalogoAdapter(builder, URL_BASE, IMAGEN_BASE, "Polaris/tests");
    }

    @Test
    @DisplayName("buscar mapea titulo, ano, autoria y portada, y se identifica con User-Agent")
    void buscarMapeaLosCampos() {
        OpenLibraryCatalogoAdapter adaptador = adaptador();
        servidor.expect(requestTo(startsWith(URL_BASE + "/search.json")))
                .andExpect(header("User-Agent", "Polaris/tests"))
                .andRespond(withSuccess("""
                        {"numFound":48036,"docs":[{
                          "key":"/works/OL893414W",
                          "title":"Dune",
                          "author_name":["Frank Herbert"],
                          "first_publish_year":1965,
                          "cover_i":11481354,
                          "number_of_pages_median":607,
                          "subject":["Science fiction","Fiction"]
                        }]}
                        """, MediaType.APPLICATION_JSON));

        List<ResultadoCatalogo> resultados = adaptador.buscar("dune", TipoContenido.LIBRO);

        assertThat(resultados).hasSize(1);
        ResultadoCatalogo resultado = resultados.getFirst();
        assertThat(resultado.getFuenteExterna()).isEqualTo(FuenteExterna.OPEN_LIBRARY);
        assertThat(resultado.getTitulo()).isEqualTo("Dune");
        assertThat(resultado.getAnio()).isEqualTo(1965);
        assertThat(resultado.getSinopsis()).isEqualTo("Frank Herbert");
        assertThat(resultado.getImagenUrl()).isEqualTo(IMAGEN_BASE + "11481354-L.jpg");
    }

    @Test
    @DisplayName("el id se queda sin el prefijo /works/")
    void elIdPierdeElPrefijoWorks() {
        OpenLibraryCatalogoAdapter adaptador = adaptador();
        servidor.expect(requestTo(startsWith(URL_BASE + "/search.json")))
                .andRespond(withSuccess("""
                        {"docs":[{"key":"/works/OL893414W","title":"Dune"}]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(adaptador.buscar("dune", TipoContenido.LIBRO).getFirst().getIdExterno())
                .isEqualTo("OL893414W");
    }

    @Test
    @DisplayName("obtener consulta por clave, no por el endpoint de detalle")
    void obtenerConsultaPorClave() {
        OpenLibraryCatalogoAdapter adaptador = adaptador();
        servidor.expect(requestTo(containsString("key:/works/OL893414W")))
                .andRespond(withSuccess("""
                        {"docs":[{
                          "key":"/works/OL893414W",
                          "title":"Dune",
                          "author_name":["Frank Herbert"],
                          "first_publish_year":1965,
                          "number_of_pages_median":607,
                          "subject":["Science fiction"]
                        }]}
                        """, MediaType.APPLICATION_JSON));

        Titulo titulo = adaptador.obtener("OL893414W", TipoContenido.LIBRO);

        assertThat(titulo.getTipo()).isEqualTo(TipoContenido.LIBRO);
        assertThat(titulo.getIdExterno()).isEqualTo("OL893414W");
        assertThat(titulo.getFuenteExterna()).isEqualTo(FuenteExterna.OPEN_LIBRARY);
        // duracion_min son paginas en un libro. Es el unico tipo que lo llena.
        assertThat(titulo.getDuracionMin()).isEqualTo(607);
    }

    @Test
    @DisplayName("las materias internas de OpenLibrary se descartan y se cortan a cinco")
    void lasMateriasSeLimpian() {
        OpenLibraryCatalogoAdapter adaptador = adaptador();
        servidor.expect(requestTo(startsWith(URL_BASE + "/search.json")))
                .andRespond(withSuccess("""
                        {"docs":[{"key":"/works/OL1W","title":"Dune","subject":[
                          "Science fiction",
                          "nyt:mass-market-monthly=2021-11-07",
                          "Fiction",
                          "award:hugo_award=1966",
                          "Ecology","Fantasy fiction","Large type books","Adventure"
                        ]}]}
                        """, MediaType.APPLICATION_JSON));

        Titulo titulo = adaptador.obtener("OL1W", TipoContenido.LIBRO);

        assertThat(titulo.getGeneros())
                .isEqualTo("Science fiction, Fiction, Ecology, Fantasy fiction, Large type books");
        assertThat(titulo.getGeneros()).hasSizeLessThanOrEqualTo(255);
    }

    @Test
    @DisplayName("un libro sin autoria, portada ni materias no revienta")
    void libroIncompletoNoRevienta() {
        OpenLibraryCatalogoAdapter adaptador = adaptador();
        servidor.expect(requestTo(startsWith(URL_BASE + "/search.json")))
                .andRespond(withSuccess("""
                        {"docs":[{"key":"/works/OL2W","title":"Sin datos"}]}
                        """, MediaType.APPLICATION_JSON));

        Titulo titulo = adaptador.obtener("OL2W", TipoContenido.LIBRO);

        assertThat(titulo.getTitulo()).isEqualTo("Sin datos");
        assertThat(titulo.getSinopsis()).isNull();
        assertThat(titulo.getImagenUrl()).isNull();
        assertThat(titulo.getGeneros()).isNull();
        assertThat(titulo.getDuracionMin()).isNull();
        assertThat(titulo.getAnio()).isNull();
    }

    @Test
    @DisplayName("una busqueda sin resultados devuelve lista vacia, no un error")
    void busquedaSinResultados() {
        OpenLibraryCatalogoAdapter adaptador = adaptador();
        servidor.expect(requestTo(startsWith(URL_BASE + "/search.json")))
                .andRespond(withSuccess("{\"numFound\":0,\"docs\":[]}", MediaType.APPLICATION_JSON));

        assertThat(adaptador.buscar("asdfghjkl", TipoContenido.LIBRO)).isEmpty();
    }

    @Test
    @DisplayName("obtener un id que no existe es ExternalServiceException")
    void obtenerIdInexistente() {
        OpenLibraryCatalogoAdapter adaptador = adaptador();
        servidor.expect(requestTo(startsWith(URL_BASE + "/search.json")))
                .andRespond(withSuccess("{\"docs\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adaptador.obtener("OL999999W", TipoContenido.LIBRO))
                .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    @DisplayName("un 422 de OpenLibrary es 400: la busqueda no vale, la API no esta rota")
    void respuesta4xxEsValidationException() {
        OpenLibraryCatalogoAdapter adaptador = adaptador();
        // OpenLibrary contesta 422 a busquedas de una sola palabra corriente.
        servidor.expect(requestTo(startsWith(URL_BASE + "/search.json")))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        assertThatThrownBy(() -> adaptador.buscar("el", TipoContenido.LIBRO))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("si OpenLibrary falla se lanza ExternalServiceException, que es un 502")
    void falloDeOpenLibraryEsExternalServiceException() {
        OpenLibraryCatalogoAdapter adaptador = adaptador();
        servidor.expect(requestTo(startsWith(URL_BASE + "/search.json")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> adaptador.buscar("dune", TipoContenido.LIBRO))
                .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    @DisplayName("solo soporta libros")
    void soportaSoloLibros() {
        OpenLibraryCatalogoAdapter adaptador = adaptador();

        assertThat(adaptador.soporta(TipoContenido.LIBRO)).isTrue();
        assertThat(adaptador.soporta(TipoContenido.PELICULA)).isFalse();
        assertThat(adaptador.soporta(TipoContenido.SERIE)).isFalse();
        assertThat(adaptador.soporta(TipoContenido.JUEGO)).isFalse();
    }
}
