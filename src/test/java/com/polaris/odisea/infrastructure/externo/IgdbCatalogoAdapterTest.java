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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

/**
 * IGDB tiene dos rarezas que no tiene TMDB y son las que se prueban aqui: el
 * token de Twitch que hay que pedir antes de cada consulta, y que la respuesta
 * no trae fechas ni URLs hechas, sino un epoch y un image_id suelto.
 */
class IgdbCatalogoAdapterTest {

    private static final String URL_API = "https://api.igdb.com/v4";
    private static final String URL_TOKEN = "https://id.twitch.tv/oauth2/token";
    private static final String IMAGEN_BASE = "https://images.igdb.com/igdb/image/upload/t_cover_big/";

    private MockRestServiceServer servidor;

    private IgdbCatalogoAdapter adaptadorCon(String clientId, String clientSecret) {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        return new IgdbCatalogoAdapter(builder, URL_API, URL_TOKEN, clientId, clientSecret, IMAGEN_BASE);
    }

    private void esperaToken(String token, long duracionSegundos) {
        servidor.expect(once(), requestTo(startsWith(URL_TOKEN)))
                .andRespond(withSuccess("""
                        {"access_token":"%s","expires_in":%d,"token_type":"bearer"}
                        """.formatted(token, duracionSegundos), MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("buscar pide el token a Twitch y lo manda en las cabeceras de IGDB")
    void buscarAutenticaConElTokenDeTwitch() {
        IgdbCatalogoAdapter adaptador = adaptadorCon("mi-id", "mi-secret");
        esperaToken("token-abc", 5000000);
        servidor.expect(once(), requestTo(URL_API + "/games"))
                .andExpect(header("Client-ID", "mi-id"))
                .andExpect(header("Authorization", "Bearer token-abc"))
                .andExpect(content().string(containsString("search \"zelda\"")))
                .andRespond(withSuccess("""
                        [{"id":1029,"name":"The Legend of Zelda: Breath of the Wild",
                          "first_release_date":1488499200,
                          "cover":{"image_id":"co3p2d"}}]
                        """, MediaType.APPLICATION_JSON));

        List<ResultadoCatalogo> resultados = adaptador.buscar("zelda", TipoContenido.JUEGO);

        assertThat(resultados).hasSize(1);
        ResultadoCatalogo resultado = resultados.getFirst();
        assertThat(resultado.getIdExterno()).isEqualTo("1029");
        assertThat(resultado.getTitulo()).isEqualTo("The Legend of Zelda: Breath of the Wild");
        assertThat(resultado.getAnio()).isEqualTo(2017);
        assertThat(resultado.getImagenUrl()).isEqualTo(IMAGEN_BASE + "co3p2d.jpg");
        servidor.verify();
    }

    @Test
    @DisplayName("el token se reutiliza: dos busquedas seguidas no piden dos tokens")
    void elTokenSeCachea() {
        IgdbCatalogoAdapter adaptador = adaptadorCon("mi-id", "mi-secret");
        esperaToken("token-abc", 5000000);
        servidor.expect(org.springframework.test.web.client.ExpectedCount.twice(),
                        requestTo(URL_API + "/games"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        adaptador.buscar("zelda", TipoContenido.JUEGO);
        adaptador.buscar("mario", TipoContenido.JUEGO);

        // El expect(once()) del token falla la verificacion si se pidio dos veces.
        servidor.verify();
    }

    @Test
    @DisplayName("un token ya caducado se vuelve a pedir en vez de reutilizarse")
    void elTokenCaducadoSeRenueva() {
        IgdbCatalogoAdapter adaptador = adaptadorCon("mi-id", "mi-secret");
        // expires_in 0: nace caducado, asi que la segunda busqueda pide otro.
        servidor.expect(org.springframework.test.web.client.ExpectedCount.twice(),
                        requestTo(startsWith(URL_TOKEN)))
                .andRespond(withSuccess("""
                        {"access_token":"token-corto","expires_in":0}
                        """, MediaType.APPLICATION_JSON));
        servidor.expect(org.springframework.test.web.client.ExpectedCount.twice(),
                        requestTo(URL_API + "/games"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        adaptador.buscar("zelda", TipoContenido.JUEGO);
        adaptador.buscar("mario", TipoContenido.JUEGO);

        servidor.verify();
    }

    @Test
    @DisplayName("obtener compone la consulta por id y mapea generos y portada")
    void obtenerMapeaLaFichaCompleta() {
        IgdbCatalogoAdapter adaptador = adaptadorCon("mi-id", "mi-secret");
        esperaToken("token-abc", 5000000);
        servidor.expect(once(), requestTo(URL_API + "/games"))
                .andExpect(content().string(containsString("where id = 1029;")))
                .andRespond(withSuccess("""
                        [{"id":1029,"name":"Breath of the Wild",
                          "summary":"Un Hyrule abierto.",
                          "first_release_date":1488499200,
                          "cover":{"image_id":"co3p2d"},
                          "genres":[{"name":"Aventura"},{"name":"Rol"}]}]
                        """, MediaType.APPLICATION_JSON));

        Titulo titulo = adaptador.obtener("1029", TipoContenido.JUEGO);

        assertThat(titulo.getTipo()).isEqualTo(TipoContenido.JUEGO);
        assertThat(titulo.getGeneros()).isEqualTo("Aventura, Rol");
        assertThat(titulo.getSinopsis()).isEqualTo("Un Hyrule abierto.");
        assertThat(titulo.getIdExterno()).isEqualTo("1029");
        // Un juego no tiene duracion fija: el campo se queda a null a proposito.
        assertThat(titulo.getDuracionMin()).isNull();
    }

    @Test
    @DisplayName("un juego sin fecha ni portada no revienta, se queda a null")
    void juegoSinFechaNiPortada() {
        IgdbCatalogoAdapter adaptador = adaptadorCon("mi-id", "mi-secret");
        esperaToken("token-abc", 5000000);
        servidor.expect(once(), requestTo(URL_API + "/games"))
                .andRespond(withSuccess("""
                        [{"id":7,"name":"Anunciado y nada mas","summary":""}]
                        """, MediaType.APPLICATION_JSON));

        Titulo titulo = adaptador.obtener("7", TipoContenido.JUEGO);

        assertThat(titulo.getAnio()).isNull();
        assertThat(titulo.getImagenUrl()).isNull();
        assertThat(titulo.getSinopsis()).isNull();
        assertThat(titulo.getGeneros()).isNull();
    }

    @Test
    @DisplayName("las comillas del texto buscado se escapan y no rompen la consulta")
    void lasComillasSeEscapan() {
        IgdbCatalogoAdapter adaptador = adaptadorCon("mi-id", "mi-secret");
        esperaToken("token-abc", 5000000);
        servidor.expect(once(), requestTo(URL_API + "/games"))
                .andExpect(content().string(containsString("search \"el \\\"juego\\\"\"")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        adaptador.buscar("el \"juego\"", TipoContenido.JUEGO);

        servidor.verify();
    }

    @Test
    @DisplayName("si IGDB falla se lanza ExternalServiceException, que es un 502")
    void falloDeIgdbEsExternalServiceException() {
        IgdbCatalogoAdapter adaptador = adaptadorCon("mi-id", "mi-secret");
        esperaToken("token-abc", 5000000);
        servidor.expect(once(), requestTo(URL_API + "/games")).andRespond(withServerError());

        assertThatThrownBy(() -> adaptador.buscar("zelda", TipoContenido.JUEGO))
                .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    @DisplayName("si Twitch rechaza las credenciales tambien es 502, no 500")
    void falloDeAutenticacionEsExternalServiceException() {
        IgdbCatalogoAdapter adaptador = adaptadorCon("mi-id", "secret-malo");
        servidor.expect(once(), requestTo(startsWith(URL_TOKEN)))
                .andRespond(withUnauthorizedRequest());

        assertThatThrownBy(() -> adaptador.buscar("zelda", TipoContenido.JUEGO))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Twitch");
    }

    @Test
    @DisplayName("sin credenciales se avisa con ValidationException y no se llama a nadie")
    void sinCredencialesNoSeLlamaANadie() {
        IgdbCatalogoAdapter adaptador = adaptadorCon("", "");

        assertThatThrownBy(() -> adaptador.buscar("zelda", TipoContenido.JUEGO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("POLARIS_IGDB_CLIENT_ID");

        servidor.verify();
    }

    @Test
    @DisplayName("un id que no es numerico es 400, no un 502 contra IGDB")
    void idNoNumericoEsValidationException() {
        IgdbCatalogoAdapter adaptador = adaptadorCon("mi-id", "mi-secret");
        esperaToken("token-abc", 5000000);

        assertThatThrownBy(() -> adaptador.obtener("zelda", TipoContenido.JUEGO))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("solo soporta juegos")
    void soportaSoloJuegos() {
        IgdbCatalogoAdapter adaptador = adaptadorCon("mi-id", "mi-secret");

        assertThat(adaptador.soporta(TipoContenido.JUEGO)).isTrue();
        assertThat(adaptador.soporta(TipoContenido.PELICULA)).isFalse();
        assertThat(adaptador.soporta(TipoContenido.LIBRO)).isFalse();
        assertThat(adaptador.soporta(TipoContenido.SERIE)).isFalse();
    }
}
