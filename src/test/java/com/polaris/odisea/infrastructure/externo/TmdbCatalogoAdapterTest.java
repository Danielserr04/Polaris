package com.polaris.odisea.infrastructure.externo;

import com.polaris.odisea.domain.model.ResultadoCatalogo;
import com.polaris.odisea.domain.model.TipoContenido;
import com.polaris.odisea.domain.model.Titulo;
import com.polaris.shared.error.ExternalServiceException;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * El JSON de TMDB no se puede probar contra la API real sin clave, y es la
 * parte mas facil de tener mal: peliculas y series no llaman igual a los mismos
 * campos. Se prueba contra un servidor simulado con respuestas reales recortadas.
 */
class TmdbCatalogoAdapterTest {

    private static final String URL_BASE = "https://api.themoviedb.org/3";
    private static final String IMAGEN_BASE = "https://image.tmdb.org/t/p/w500";

    private MockRestServiceServer servidor;

    private TmdbCatalogoAdapter adaptadorCon(String token) {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        return new TmdbCatalogoAdapter(builder, URL_BASE, token, IMAGEN_BASE, "es-ES");
    }

    @Test
    @DisplayName("buscar una pelicula lee title, release_date y poster_path")
    void buscarPeliculaMapeaLosCamposDePelicula() {
        TmdbCatalogoAdapter adaptador = adaptadorCon("token-de-prueba");
        servidor.expect(requestTo(org.hamcrest.Matchers.startsWith(URL_BASE + "/search/movie")))
                .andExpect(header("Authorization", "Bearer token-de-prueba"))
                .andRespond(withSuccess("""
                        {"results":[{
                          "id":157336,
                          "title":"Interstellar",
                          "original_title":"Interstellar",
                          "overview":"Un grupo de exploradores...",
                          "poster_path":"/abc123.jpg",
                          "release_date":"2014-11-05"
                        }]}
                        """, MediaType.APPLICATION_JSON));

        List<ResultadoCatalogo> resultados = adaptador.buscar("interstellar", TipoContenido.PELICULA);

        assertThat(resultados).hasSize(1);
        ResultadoCatalogo resultado = resultados.getFirst();
        assertThat(resultado.getIdExterno()).isEqualTo("157336");
        assertThat(resultado.getTitulo()).isEqualTo("Interstellar");
        assertThat(resultado.getAnio()).isEqualTo(2014);
        assertThat(resultado.getImagenUrl()).isEqualTo(IMAGEN_BASE + "/abc123.jpg");
        assertThat(resultado.getTituloId()).isNull();
    }

    @Test
    @DisplayName("buscar una serie lee name y first_air_date, que son campos distintos")
    void buscarSerieMapeaLosCamposDeSerie() {
        TmdbCatalogoAdapter adaptador = adaptadorCon("token-de-prueba");
        servidor.expect(requestTo(org.hamcrest.Matchers.startsWith(URL_BASE + "/search/tv")))
                .andRespond(withSuccess("""
                        {"results":[{
                          "id":1396,
                          "name":"Breaking Bad",
                          "original_name":"Breaking Bad",
                          "first_air_date":"2008-01-20",
                          "poster_path":"/xyz.jpg"
                        }]}
                        """, MediaType.APPLICATION_JSON));

        ResultadoCatalogo resultado = adaptador.buscar("breaking bad", TipoContenido.SERIE).getFirst();

        assertThat(resultado.getTitulo()).isEqualTo("Breaking Bad");
        assertThat(resultado.getTituloOriginal()).isEqualTo("Breaking Bad");
        assertThat(resultado.getAnio()).isEqualTo(2008);
    }

    @Test
    @DisplayName("obtener una pelicula trae generos y runtime")
    void obtenerPeliculaTraeGenerosYDuracion() {
        TmdbCatalogoAdapter adaptador = adaptadorCon("token-de-prueba");
        servidor.expect(requestTo(org.hamcrest.Matchers.startsWith(URL_BASE + "/movie/157336")))
                .andRespond(withSuccess("""
                        {
                          "id":157336,
                          "title":"Interstellar",
                          "overview":"Un grupo de exploradores...",
                          "poster_path":"/abc123.jpg",
                          "release_date":"2014-11-05",
                          "runtime":169,
                          "genres":[{"id":12,"name":"Aventura"},{"id":18,"name":"Drama"}]
                        }
                        """, MediaType.APPLICATION_JSON));

        Titulo titulo = adaptador.obtener("157336", TipoContenido.PELICULA);

        assertThat(titulo.getDuracionMin()).isEqualTo(169);
        assertThat(titulo.getGeneros()).isEqualTo("Aventura, Drama");
        assertThat(titulo.getIdExterno()).isEqualTo("157336");
        assertThat(titulo.getTipo()).isEqualTo(TipoContenido.PELICULA);
    }

    @Test
    @DisplayName("obtener una serie coge la duracion de episode_run_time, que es una lista")
    void obtenerSerieCogeLaDuracionDeLaLista() {
        TmdbCatalogoAdapter adaptador = adaptadorCon("token-de-prueba");
        servidor.expect(requestTo(org.hamcrest.Matchers.startsWith(URL_BASE + "/tv/1396")))
                .andRespond(withSuccess("""
                        {"id":1396,"name":"Breaking Bad","episode_run_time":[45,47]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(adaptador.obtener("1396", TipoContenido.SERIE).getDuracionMin()).isEqualTo(45);
    }

    @Test
    @DisplayName("una sinopsis vacia se guarda como null, no como cadena vacia")
    void sinopsisVaciaSeGuardaComoNull() {
        TmdbCatalogoAdapter adaptador = adaptadorCon("token-de-prueba");
        servidor.expect(requestTo(org.hamcrest.Matchers.startsWith(URL_BASE + "/movie/1")))
                .andRespond(withSuccess("""
                        {"id":1,"title":"Sin traducir","overview":"","poster_path":null}
                        """, MediaType.APPLICATION_JSON));

        Titulo titulo = adaptador.obtener("1", TipoContenido.PELICULA);

        assertThat(titulo.getSinopsis()).isNull();
        assertThat(titulo.getImagenUrl()).isNull();
        assertThat(titulo.getAnio()).isNull();
    }

    @Test
    @DisplayName("si TMDB falla se lanza ExternalServiceException, que es un 502 y no un 500")
    void fallaDeTmdbEsExternalServiceException() {
        TmdbCatalogoAdapter adaptador = adaptadorCon("token-de-prueba");
        servidor.expect(requestTo(org.hamcrest.Matchers.startsWith(URL_BASE + "/search/movie")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> adaptador.buscar("lo que sea", TipoContenido.PELICULA))
                .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    @DisplayName("sin token configurado se avisa con ValidationException y no se llama a TMDB")
    void sinTokenNoSeLlamaATmdb() {
        TmdbCatalogoAdapter adaptador = adaptadorCon("");

        assertThatThrownBy(() -> adaptador.buscar("interstellar", TipoContenido.PELICULA))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("POLARIS_TMDB_TOKEN");

        servidor.verify();
    }

    @Test
    @DisplayName("solo soporta peliculas y series")
    void soportaSoloPeliculasYSeries() {
        TmdbCatalogoAdapter adaptador = adaptadorCon("token-de-prueba");

        assertThat(adaptador.soporta(TipoContenido.PELICULA)).isTrue();
        assertThat(adaptador.soporta(TipoContenido.SERIE)).isTrue();
        assertThat(adaptador.soporta(TipoContenido.JUEGO)).isFalse();
        assertThat(adaptador.soporta(TipoContenido.LIBRO)).isFalse();
    }
}
