package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.in.ListLogrosInterface;
import com.polaris.fusion.application.out.EstadisticasLogrosPort;
import com.polaris.fusion.domain.model.EstadisticasLogros;
import com.polaris.fusion.domain.model.MetricaLogro;
import com.polaris.shared.logro.CalculoLogros;
import com.polaris.shared.logro.Hito;
import com.polaris.shared.logro.Logro;
import com.polaris.shared.logro.NivelLogro;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static com.polaris.shared.logro.Logro.definicion;

/**
 * Logros de nutricion, calculados al vuelo. Recetas y planes no tienen fecha,
 * asi que sus logros salen conseguidos sin dia. Ver
 * docs/decisiones/044-logros-globales-con-fecha-calculada.md.
 */
@Service("fusionLogroService")
@RequiredArgsConstructor
public class LogroService implements ListLogrosInterface {

    /** El catalogo, en el orden en que se muestra. */
    static final List<Definicion> CATALOGO = List.of(
            new Definicion(MetricaLogro.COMIDAS, definicion("PRIMERA_COMIDA", "Primer bocado",
                    "Apunta tu primera comida", "utensils", NivelLogro.BRONCE, "comidas", 1)),
            new Definicion(MetricaLogro.COMIDAS, definicion("COMIDAS_100", "100 comidas",
                    "Apunta 100 comidas", "utensils", NivelLogro.PLATA, "comidas", 100)),
            new Definicion(MetricaLogro.COMIDAS, definicion("COMIDAS_1000", "Mil comidas",
                    "Apunta 1.000 comidas", "utensils", NivelLogro.ORO, "comidas", 1000)),
            new Definicion(MetricaLogro.DIAS, definicion("DIAS_30", "Un mes a la vista",
                    "Apunta lo que comes 30 días", "calendar", NivelLogro.PLATA, "días", 30)),
            new Definicion(MetricaLogro.DIAS, definicion("DIAS_365", "Un año a la vista",
                    "Apunta lo que comes 365 días", "calendar", NivelLogro.PLATINO, "días", 365)),
            new Definicion(MetricaLogro.RACHA_DIAS, definicion("RACHA_7", "Semana completa",
                    "Apunta lo que comes 7 días seguidos", "flame", NivelLogro.BRONCE, "días", 7)),
            new Definicion(MetricaLogro.RACHA_DIAS, definicion("RACHA_30", "Sin saltarte ni uno",
                    "Apunta lo que comes 30 días seguidos", "flame", NivelLogro.ORO, "días", 30)),
            new Definicion(MetricaLogro.OBJETIVO, definicion("OBJETIVO", "Con rumbo",
                    "Fija tu objetivo nutricional", "compass", NivelLogro.BRONCE, "objetivos", 1)),
            new Definicion(MetricaLogro.RECETAS, definicion("PRIMERA_RECETA", "Manos a la obra",
                    "Crea tu primera receta", "book-open", NivelLogro.BRONCE, "recetas", 1)),
            new Definicion(MetricaLogro.RECETAS, definicion("RECETAS_10", "Recetario propio",
                    "Crea 10 recetas", "book-open", NivelLogro.PLATA, "recetas", 10)),
            new Definicion(MetricaLogro.PLANES, definicion("PRIMER_PLAN", "Planificador",
                    "Crea un plan de comidas", "shopping-basket", NivelLogro.BRONCE, "planes", 1)));

    private final EstadisticasLogrosPort estadisticasPort;

    @Override
    public List<Logro> list(Long usuarioId) {
        EstadisticasLogros e = estadisticasPort.find(usuarioId);
        Map<MetricaLogro, List<Hito>> hitos = Map.of(
                MetricaLogro.COMIDAS, CalculoLogros.unoPorFecha(e.getFechasComida()),
                MetricaLogro.DIAS, CalculoLogros.diasDistintos(e.getFechasComida()),
                MetricaLogro.RACHA_DIAS, CalculoLogros.rachaMasLarga(e.getFechasComida(), ChronoUnit.DAYS),
                MetricaLogro.RECETAS, sinFecha(e.getNumeroRecetas()),
                MetricaLogro.PLANES, sinFecha(e.getNumeroPlanes()),
                MetricaLogro.OBJETIVO, e.getPrimerObjetivo() != null
                        ? List.of(new Hito(e.getPrimerObjetivo(), 1)) : List.of());
        return CATALOGO.stream()
                .map(d -> CalculoLogros.evaluar(d.logro(), hitos.get(d.metrica())))
                .toList();
    }

    private static List<Hito> sinFecha(long cantidad) {
        return cantidad > 0 ? List.of(new Hito(null, cantidad)) : List.of();
    }

    /** Un logro del catalogo con lo que mide. */
    record Definicion(MetricaLogro metrica, Logro logro) {
    }
}
