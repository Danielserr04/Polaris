package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.in.ListLogrosInterface;
import com.polaris.atlas.application.out.EstadisticasEntrenoPort;
import com.polaris.atlas.application.out.PesoCorporalPort;
import com.polaris.atlas.domain.model.EstadisticasEntreno;
import com.polaris.atlas.domain.model.Logro;
import com.polaris.atlas.domain.model.MetricaLogro;
import com.polaris.atlas.domain.model.PesoCorporalFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

/**
 * Logros calculados al vuelo sobre un catalogo fijo, traidos de FitCore. Sin
 * tabla: el progreso sale de las sesiones, las series y el peso corporal, asi
 * que borrar una sesion puede "desconseguir" un logro, y es coherente. Ver
 * docs/decisiones/043-logros-calculados-y-metas.md.
 */
@Service
@RequiredArgsConstructor
public class LogroService implements ListLogrosInterface {

    private static final BigDecimal MIL = BigDecimal.valueOf(1000);

    /** El catalogo, en el orden en que se muestra. */
    static final List<Logro> CATALOGO = List.of(
            definicion("PRIMER_ENTRENO", "Primer entreno", "Registra tu primera sesión", "dumbbell", MetricaLogro.SESIONES, 1),
            definicion("ENTRENOS_10", "10 entrenos", "Registra 10 sesiones", "dumbbell", MetricaLogro.SESIONES, 10),
            definicion("ENTRENOS_50", "50 entrenos", "Registra 50 sesiones", "dumbbell", MetricaLogro.SESIONES, 50),
            definicion("ENTRENOS_100", "100 entrenos", "Registra 100 sesiones", "dumbbell", MetricaLogro.SESIONES, 100),
            definicion("EJERCICIOS_10", "Variado", "Trabaja 10 ejercicios distintos", "compass", MetricaLogro.EJERCICIOS, 10),
            definicion("TONELADAS_10", "10 toneladas", "Levanta 10 t de volumen en total", "trophy", MetricaLogro.TONELADAS, 10),
            definicion("TONELADAS_100", "100 toneladas", "Levanta 100 t de volumen en total", "trophy", MetricaLogro.TONELADAS, 100),
            definicion("TONELADAS_1000", "Mil toneladas", "Levanta 1.000 t de volumen en total", "trophy", MetricaLogro.TONELADAS, 1000),
            definicion("RACHA_4", "Un mes sin fallar", "Entrena 4 semanas seguidas", "flame", MetricaLogro.RACHA_SEMANAS, 4),
            definicion("RACHA_12", "Constancia", "Entrena 12 semanas seguidas", "flame", MetricaLogro.RACHA_SEMANAS, 12),
            definicion("PRIMER_PESAJE", "Primer pesaje", "Apunta tu peso corporal", "scale", MetricaLogro.PESAJES, 1),
            definicion("PESAJES_30", "30 pesajes", "Apunta tu peso 30 días", "scale", MetricaLogro.PESAJES, 30));

    private final EstadisticasEntrenoPort estadisticasPort;
    private final PesoCorporalPort pesoCorporalPort;

    @Override
    public List<Logro> list(Long usuarioId) {
        EstadisticasEntreno e = estadisticasPort.find(usuarioId);
        Map<MetricaLogro, Long> progreso = Map.of(
                MetricaLogro.SESIONES, e.getNumeroSesiones(),
                MetricaLogro.EJERCICIOS, e.getEjerciciosDistintos(),
                MetricaLogro.TONELADAS, e.getVolumenTotal().divideToIntegralValue(MIL).longValue(),
                MetricaLogro.PESAJES, (long) pesoCorporalPort.findAll(usuarioId, new PesoCorporalFilter()).size(),
                MetricaLogro.RACHA_SEMANAS, rachaMasLarga(e.getFechasSesion()));

        return CATALOGO.stream()
                .map(l -> Logro.builder()
                        .codigo(l.getCodigo())
                        .nombre(l.getNombre())
                        .descripcion(l.getDescripcion())
                        .icono(l.getIcono())
                        .metrica(l.getMetrica())
                        .objetivo(l.getObjetivo())
                        .progreso(progreso.get(l.getMetrica()))
                        .build())
                .toList();
    }

    /** Mayor numero de semanas (de lunes a domingo) seguidas con al menos una sesion. */
    static long rachaMasLarga(List<LocalDate> fechas) {
        List<LocalDate> lunes = fechas.stream()
                .map(f -> f.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
                .distinct()
                .sorted()
                .toList();
        long mejor = 0;
        long actual = 0;
        LocalDate anterior = null;
        for (LocalDate l : lunes) {
            actual = anterior != null && ChronoUnit.WEEKS.between(anterior, l) == 1 ? actual + 1 : 1;
            mejor = Math.max(mejor, actual);
            anterior = l;
        }
        return mejor;
    }

    private static Logro definicion(String codigo, String nombre, String descripcion, String icono,
                                    MetricaLogro metrica, long objetivo) {
        return Logro.builder().codigo(codigo).nombre(nombre).descripcion(descripcion).icono(icono)
                .metrica(metrica).objetivo(objetivo).build();
    }
}
