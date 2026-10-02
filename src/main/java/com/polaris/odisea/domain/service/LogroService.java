package com.polaris.odisea.domain.service;

import com.polaris.odisea.application.in.ListLogrosInterface;
import com.polaris.odisea.application.out.EstadisticasLogrosPort;
import com.polaris.odisea.domain.model.EstadisticasLogros;
import com.polaris.odisea.domain.model.EstadisticasLogros.Terminado;
import com.polaris.odisea.domain.model.MetricaLogro;
import com.polaris.odisea.domain.model.TipoContenido;
import com.polaris.shared.logro.CalculoLogros;
import com.polaris.shared.logro.Hito;
import com.polaris.shared.logro.Logro;
import com.polaris.shared.logro.NivelLogro;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.polaris.shared.logro.Logro.definicion;

/**
 * Logros de ocio, calculados al vuelo. La fecha es la de fin de la entrada;
 * un titulo terminado sin fecha de fin cuenta, pero su logro puede quedar sin
 * dia. Ver docs/decisiones/044-logros-globales-con-fecha-calculada.md.
 */
@Service("odiseaLogroService")
@RequiredArgsConstructor
public class LogroService implements ListLogrosInterface {

    /** El catalogo, en el orden en que se muestra. */
    static final List<Definicion> CATALOGO = List.of(
            new Definicion(MetricaLogro.TERMINADOS, definicion("PRIMER_TERMINADO", "Primer final",
                    "Termina tu primer título", "check", NivelLogro.BRONCE, "títulos", 1)),
            new Definicion(MetricaLogro.TERMINADOS, definicion("TERMINADOS_10", "Diez historias",
                    "Termina 10 títulos", "star", NivelLogro.PLATA, "títulos", 10)),
            new Definicion(MetricaLogro.TERMINADOS, definicion("TERMINADOS_50", "Coleccionista",
                    "Termina 50 títulos", "star", NivelLogro.ORO, "títulos", 50)),
            new Definicion(MetricaLogro.TERMINADOS, definicion("TERMINADOS_100", "Leyenda de la Odisea",
                    "Termina 100 títulos", "trophy", NivelLogro.PLATINO, "títulos", 100)),
            new Definicion(MetricaLogro.PELICULAS, definicion("PELICULAS_10", "Cinéfilo",
                    "Termina 10 películas", "clapperboard", NivelLogro.PLATA, "películas", 10)),
            new Definicion(MetricaLogro.SERIES, definicion("SERIES_5", "Maratoniano",
                    "Termina 5 series", "tv", NivelLogro.PLATA, "series", 5)),
            new Definicion(MetricaLogro.LIBROS, definicion("LIBROS_10", "Ratón de biblioteca",
                    "Termina 10 libros", "book-open", NivelLogro.PLATA, "libros", 10)),
            new Definicion(MetricaLogro.JUEGOS, definicion("JUEGOS_5", "Jugón",
                    "Termina 5 juegos", "gamepad-2", NivelLogro.PLATA, "juegos", 5)),
            new Definicion(MetricaLogro.TIPOS, definicion("TODOTERRENO", "Todoterreno",
                    "Termina una película, una serie, un libro y un juego", "orbit", NivelLogro.ORO, "tipos", 4)),
            new Definicion(MetricaLogro.VALORADAS, definicion("VALORADAS_10", "Crítico",
                    "Valora 10 títulos", "sparkles", NivelLogro.BRONCE, "títulos", 10)));

    private final EstadisticasLogrosPort estadisticasPort;

    @Override
    public List<Logro> list(Long usuarioId) {
        EstadisticasLogros e = estadisticasPort.find(usuarioId);
        List<Terminado> terminados = e.getTerminados();
        Map<MetricaLogro, List<Hito>> hitos = Map.of(
                MetricaLogro.TERMINADOS, CalculoLogros.unoPorFecha(fechas(terminados, null)),
                MetricaLogro.PELICULAS, CalculoLogros.unoPorFecha(fechas(terminados, TipoContenido.PELICULA)),
                MetricaLogro.SERIES, CalculoLogros.unoPorFecha(fechas(terminados, TipoContenido.SERIE)),
                MetricaLogro.LIBROS, CalculoLogros.unoPorFecha(fechas(terminados, TipoContenido.LIBRO)),
                MetricaLogro.JUEGOS, CalculoLogros.unoPorFecha(fechas(terminados, TipoContenido.JUEGO)),
                MetricaLogro.TIPOS, tiposDistintos(terminados),
                MetricaLogro.VALORADAS, CalculoLogros.unoPorFecha(e.getFechasValoradas()));
        return CATALOGO.stream()
                .map(d -> CalculoLogros.evaluar(d.logro(), hitos.get(d.metrica())))
                .toList();
    }

    /** Las fechas de fin de los terminados de un tipo, o de todos si es nulo. */
    private static List<LocalDate> fechas(List<Terminado> terminados, TipoContenido tipo) {
        return terminados.stream()
                .filter(t -> tipo == null || t.tipo() == tipo)
                .map(Terminado::fechaFin)
                .toList();
    }

    /** Un hito por tipo con algo terminado, fechado con el primer final de ese tipo. */
    static List<Hito> tiposDistintos(List<Terminado> terminados) {
        Comparator<LocalDate> nulosAlFinal = Comparator.nullsLast(Comparator.naturalOrder());
        return terminados.stream()
                .collect(Collectors.groupingBy(Terminado::tipo))
                .values().stream()
                .map(delTipo -> new Hito(delTipo.stream().map(Terminado::fechaFin).min(nulosAlFinal).orElse(null), 1))
                .toList();
    }

    /** Un logro del catalogo con lo que mide. */
    record Definicion(MetricaLogro metrica, Logro logro) {
    }
}
