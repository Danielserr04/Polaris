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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * IGDB para juegos. Se eligio sobre RAWG por calidad de datos: generos
 * normalizados, fechas por plataforma y caratulas en condiciones. El precio son
 * las dos rarezas que se resuelven aqui dentro.
 *
 * <p><b>Una:</b> la credencial no es una clave fija. Se registra una aplicacion
 * en Twitch (no en IGDB) y con su client id y secret se pide un token que
 * caduca, asi que hay que renovarlo. Se cachea y se pide de nuevo cuando queda
 * poco (ver MARGEN_RENOVACION).
 *
 * <p><b>Dos:</b> no es una API REST al uso. Se consulta con POST y un lenguaje
 * propio (APIcalypse) en el cuerpo: {@code search "zelda"; fields name; limit 20;}.
 *
 * <p>Nada de eso sale del adaptador: CatalogoService pide "busca esto de tipo
 * JUEGO" igual que se lo pide a TMDB.
 */
@Component
public class IgdbCatalogoAdapter implements CatalogoExternoPort {

    private static final int MAX_RESULTADOS = 20;

    /** Se renueva antes de que caduque de verdad, para no cortar una peticion en curso. */
    private static final Duration MARGEN_RENOVACION = Duration.ofMinutes(5);

    /** Los campos que se piden. En IGDB no hay "dame todo": se enumeran. */
    private static final String CAMPOS =
            "fields name,summary,first_release_date,cover.image_id,genres.name";

    private final RestClient cliente;
    private final String urlApi;
    private final String urlToken;
    private final String clientId;
    private final String clientSecret;
    private final String imagenBase;

    private String tokenCacheado;
    private Instant tokenCaducaEn = Instant.EPOCH;

    public IgdbCatalogoAdapter(
            RestClient.Builder builder,
            @Value("${polaris.igdb.url-api}") String urlApi,
            @Value("${polaris.igdb.url-token}") String urlToken,
            @Value("${polaris.igdb.client-id:}") String clientId,
            @Value("${polaris.igdb.client-secret:}") String clientSecret,
            @Value("${polaris.igdb.imagen-base}") String imagenBase) {
        this.cliente = builder.build();
        this.urlApi = urlApi;
        this.urlToken = urlToken;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.imagenBase = imagenBase;
    }

    @Override
    public boolean soporta(TipoContenido tipo) {
        return tipo == TipoContenido.JUEGO;
    }

    @Override
    public FuenteExterna fuente() {
        return FuenteExterna.IGDB;
    }

    @Override
    public List<ResultadoCatalogo> buscar(String texto, TipoContenido tipo) {
        // Las comillas del texto se escapan: sin esto, un titulo con comillas
        // rompe la consulta de APIcalypse.
        String consulta = "search \"%s\"; %s; limit %d;"
                .formatted(escapar(texto), CAMPOS, MAX_RESULTADOS);

        return consultar(consulta).stream()
                .map(this::aResultado)
                .toList();
    }

    @Override
    public Titulo obtener(String idExterno, TipoContenido tipo) {
        long id = aId(idExterno);
        String consulta = "%s; where id = %d;".formatted(CAMPOS, id);

        JuegoIgdb juego = consultar(consulta).stream()
                .findFirst()
                .orElseThrow(() -> new ExternalServiceException(
                        "IGDB no devolvio la ficha " + idExterno));

        return Titulo.builder()
                .tipo(TipoContenido.JUEGO)
                .titulo(juego.name())
                .tituloOriginal(null)
                .anio(anio(juego.firstReleaseDate()))
                .sinopsis(vacioANull(juego.summary()))
                .imagenUrl(imagen(juego.cover()))
                .generos(generos(juego))
                // Un juego no tiene duracion fija. duracion_min son minutos en
                // una pelicula y paginas en un libro; aqui no aplica.
                .duracionMin(null)
                .fuenteExterna(FuenteExterna.IGDB)
                .idExterno(idExterno)
                .build();
    }

    private ResultadoCatalogo aResultado(JuegoIgdb juego) {
        return ResultadoCatalogo.builder()
                .fuenteExterna(FuenteExterna.IGDB)
                .idExterno(String.valueOf(juego.id()))
                .tipo(TipoContenido.JUEGO)
                .titulo(juego.name())
                .anio(anio(juego.firstReleaseDate()))
                .sinopsis(vacioANull(juego.summary()))
                .imagenUrl(imagen(juego.cover()))
                .build();
    }

    private List<JuegoIgdb> consultar(String consulta) {
        String token = token();

        try {
            JuegoIgdb[] juegos = cliente.post()
                    .uri(urlApi + "/games")
                    .header("Client-ID", clientId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(consulta)
                    .retrieve()
                    .body(JuegoIgdb[].class);

            return juegos == null ? List.of() : List.of(juegos);
        } catch (RestClientException e) {
            throw new ExternalServiceException("IGDB no ha respondido correctamente", e);
        }
    }

    /**
     * Devuelve el token cacheado mientras siga siendo valido, y pide uno nuevo
     * cuando no. Es un client credentials de Twitch: no hay refresh token, se
     * vuelve a pedir uno entero.
     */
    private synchronized String token() {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new ValidationException(
                    "Faltan POLARIS_IGDB_CLIENT_ID y POLARIS_IGDB_CLIENT_SECRET: "
                            + "la busqueda de juegos no esta configurada");
        }

        if (tokenCacheado != null && Instant.now().isBefore(tokenCaducaEn.minus(MARGEN_RENOVACION))) {
            return tokenCacheado;
        }

        try {
            RespuestaToken respuesta = cliente.post()
                    .uri(urlToken, uri -> uri
                            .queryParam("client_id", clientId)
                            .queryParam("client_secret", clientSecret)
                            .queryParam("grant_type", "client_credentials")
                            .build())
                    .retrieve()
                    .body(RespuestaToken.class);

            if (respuesta == null || respuesta.accessToken() == null) {
                throw new ExternalServiceException("Twitch no devolvio un token para IGDB");
            }

            tokenCacheado = respuesta.accessToken();
            tokenCaducaEn = Instant.now().plusSeconds(
                    respuesta.expiresIn() == null ? 0L : respuesta.expiresIn());

            return tokenCacheado;
        } catch (RestClientException e) {
            // Ni el secret ni el token se meten en el mensaje.
            throw new ExternalServiceException("No se pudo autenticar contra Twitch para usar IGDB", e);
        }
    }

    /** IGDB identifica los juegos por un entero. Cualquier otra cosa es un 400, no un 502. */
    private long aId(String idExterno) {
        try {
            return Long.parseLong(idExterno);
        } catch (NumberFormatException e) {
            throw new ValidationException("El id de IGDB debe ser un numero: " + idExterno);
        }
    }

    /** first_release_date viene como segundos desde epoch, no como fecha. */
    private Integer anio(Long firstReleaseDate) {
        if (firstReleaseDate == null) {
            return null;
        }

        return Instant.ofEpochSecond(firstReleaseDate).atZone(ZoneOffset.UTC).getYear();
    }

    /**
     * La portada llega como un image_id suelto; la URL se compone con el tamano
     * deseado. t_cover_big es el que fija imagen-base en application.yml.
     */
    private String imagen(Cover cover) {
        if (cover == null || cover.imageId() == null || cover.imageId().isBlank()) {
            return null;
        }

        return imagenBase + cover.imageId() + ".jpg";
    }

    private String generos(JuegoIgdb juego) {
        if (juego.genres() == null || juego.genres().isEmpty()) {
            return null;
        }

        return juego.genres().stream()
                .map(Genero::name)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    private String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String vacioANull(String texto) {
        return texto == null || texto.isBlank() ? null : texto;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RespuestaToken(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Long expiresIn) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record JuegoIgdb(
            Long id,
            String name,
            String summary,
            @JsonProperty("first_release_date") Long firstReleaseDate,
            Cover cover,
            List<Genero> genres) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Cover(@JsonProperty("image_id") String imageId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Genero(String name) {
    }
}
