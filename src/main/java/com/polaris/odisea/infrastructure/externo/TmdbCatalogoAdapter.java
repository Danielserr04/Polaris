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
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TMDB para peliculas y series. Es la unica fuente decidida; juegos y libros
 * siguen abiertos, ver docs/modulos/odisea.md.
 *
 * <p>Autentica con el "API Read Access Token" (v4) en la cabecera Authorization,
 * no con la api_key de v3 en la query: una clave en la URL acaba en los logs de
 * cualquier proxy por el que pase.
 *
 * <p>Peliculas y series son endpoints distintos en TMDB y sus campos no se
 * llaman igual (title/name, release_date/first_air_date). Esa diferencia se
 * traduce aqui y no sale del adaptador.
 */
@Component
public class TmdbCatalogoAdapter implements CatalogoExternoPort {

    private static final int MAX_RESULTADOS = 20;

    private final RestClient cliente;
    private final String token;
    private final String imagenBase;
    private final String idioma;

    public TmdbCatalogoAdapter(
            RestClient.Builder builder,
            @Value("${polaris.tmdb.url-base}") String urlBase,
            @Value("${polaris.tmdb.token:}") String token,
            @Value("${polaris.tmdb.imagen-base}") String imagenBase,
            @Value("${polaris.tmdb.idioma}") String idioma) {
        this.cliente = builder.baseUrl(urlBase).build();
        this.token = token;
        this.imagenBase = imagenBase;
        this.idioma = idioma;
    }

    @Override
    public boolean soporta(TipoContenido tipo) {
        return tipo == TipoContenido.PELICULA || tipo == TipoContenido.SERIE;
    }

    @Override
    public FuenteExterna fuente() {
        return FuenteExterna.TMDB;
    }

    @Override
    public List<ResultadoCatalogo> buscar(String texto, TipoContenido tipo) {
        String ruta = tipo == TipoContenido.PELICULA ? "/search/movie" : "/search/tv";

        RespuestaBusqueda respuesta = get(uri -> uri
                .path(ruta)
                .queryParam("query", texto)
                .queryParam("language", idioma)
                .queryParam("include_adult", false)
                .build(), RespuestaBusqueda.class);

        if (respuesta == null || respuesta.results() == null) {
            return List.of();
        }

        return respuesta.results().stream()
                .limit(MAX_RESULTADOS)
                .map(ficha -> aResultado(ficha, tipo))
                .toList();
    }

    @Override
    public Titulo obtener(String idExterno, TipoContenido tipo) {
        String ruta = (tipo == TipoContenido.PELICULA ? "/movie/" : "/tv/") + idExterno;

        FichaTmdb ficha = get(uri -> uri
                .path(ruta)
                .queryParam("language", idioma)
                .build(), FichaTmdb.class);

        if (ficha == null) {
            throw new ExternalServiceException("TMDB no devolvio la ficha " + idExterno);
        }

        return Titulo.builder()
                .tipo(tipo)
                .titulo(nombre(ficha))
                .tituloOriginal(nombreOriginal(ficha))
                .anio(anio(ficha))
                .sinopsis(vacioANull(ficha.overview()))
                .imagenUrl(imagen(ficha.posterPath()))
                .generos(generos(ficha))
                .duracionMin(duracion(ficha))
                .fuenteExterna(FuenteExterna.TMDB)
                .idExterno(idExterno)
                .build();
    }

    private ResultadoCatalogo aResultado(FichaTmdb ficha, TipoContenido tipo) {
        return ResultadoCatalogo.builder()
                .fuenteExterna(FuenteExterna.TMDB)
                .idExterno(String.valueOf(ficha.id()))
                .tipo(tipo)
                .titulo(nombre(ficha))
                .tituloOriginal(nombreOriginal(ficha))
                .anio(anio(ficha))
                .sinopsis(vacioANull(ficha.overview()))
                .imagenUrl(imagen(ficha.posterPath()))
                .build();
    }

    /**
     * Un token vacio se detecta aqui y no en el arranque: la aplicacion tiene
     * que poder levantarse sin credenciales de TMDB, igual que sin las de
     * Google. Lo que no puede es fingir que la busqueda funciona.
     */
    private <T> T get(java.util.function.Function<org.springframework.web.util.UriBuilder,
            java.net.URI> uri, Class<T> tipoRespuesta) {
        if (token == null || token.isBlank()) {
            throw new ValidationException(
                    "Falta POLARIS_TMDB_TOKEN: la busqueda en TMDB no esta configurada");
        }

        try {
            return cliente.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(tipoRespuesta);
        } catch (RestClientException e) {
            // El token no se toca al construir el mensaje: va en una cabecera y
            // se queda ahi.
            throw new ExternalServiceException("TMDB no ha respondido correctamente", e);
        }
    }

    /** Peliculas traen title; series traen name. */
    private String nombre(FichaTmdb ficha) {
        return ficha.title() != null ? ficha.title() : ficha.name();
    }

    private String nombreOriginal(FichaTmdb ficha) {
        return ficha.originalTitle() != null ? ficha.originalTitle() : ficha.originalName();
    }

    /** release_date en peliculas, first_air_date en series. "" cuando no se sabe. */
    private Integer anio(FichaTmdb ficha) {
        String fecha = ficha.releaseDate() != null ? ficha.releaseDate() : ficha.firstAirDate();

        if (fecha == null || fecha.length() < 4) {
            return null;
        }

        try {
            return Integer.valueOf(fecha.substring(0, 4));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** poster_path viene relativo: "/abc.jpg". Sin el prefijo no es una imagen. */
    private String imagen(String posterPath) {
        return posterPath == null || posterPath.isBlank() ? null : imagenBase + posterPath;
    }

    private String generos(FichaTmdb ficha) {
        if (ficha.genres() == null || ficha.genres().isEmpty()) {
            return null;
        }

        return ficha.genres().stream()
                .map(Genero::name)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    /**
     * Minutos de la pelicula, o de un episodio de la serie. TMDB devuelve
     * episode_run_time como lista porque una serie puede tener episodios de
     * duraciones distintas; se coge el primero.
     */
    private Integer duracion(FichaTmdb ficha) {
        if (ficha.runtime() != null) {
            return ficha.runtime();
        }

        if (ficha.episodeRunTime() != null && !ficha.episodeRunTime().isEmpty()) {
            return ficha.episodeRunTime().getFirst();
        }

        return null;
    }

    /** TMDB manda "" en vez de null cuando no hay sinopsis traducida. */
    private String vacioANull(String texto) {
        return texto == null || texto.isBlank() ? null : texto;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RespuestaBusqueda(List<FichaTmdb> results) {
    }

    /**
     * Un solo record para busqueda y detalle, y para pelicula y serie: los
     * campos que no aplican llegan a null. Cuatro records casi iguales serian
     * peor de leer.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record FichaTmdb(
            Integer id,
            String title,
            String name,
            @JsonProperty("original_title") String originalTitle,
            @JsonProperty("original_name") String originalName,
            String overview,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("release_date") String releaseDate,
            @JsonProperty("first_air_date") String firstAirDate,
            List<Genero> genres,
            Integer runtime,
            @JsonProperty("episode_run_time") List<Integer> episodeRunTime) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Genero(String name) {
    }
}
