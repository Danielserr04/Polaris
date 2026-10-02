package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.in.ListLogrosInterface;
import com.polaris.atlas.application.out.EstadisticasEntrenoPort;
import com.polaris.atlas.domain.model.EstadisticasEntreno;
import com.polaris.atlas.domain.model.MetricaLogro;
import com.polaris.shared.logro.CalculoLogros;
import com.polaris.shared.logro.Hito;
import com.polaris.shared.logro.Logro;
import com.polaris.shared.logro.NivelLogro;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static com.polaris.shared.logro.Logro.definicion;

/**
 * Logros de entreno calculados al vuelo sobre un catalogo fijo. Sin tabla: el
 * progreso y el dia en que se consiguio salen de las sesiones y las series,
 * asi que borrar una sesion puede "desconseguir" un logro, y es coherente. Ver
 * docs/decisiones/043-logros-calculados-y-metas.md y
 * docs/decisiones/044-logros-globales-con-fecha-calculada.md.
 */
@Service("atlasLogroService")
@RequiredArgsConstructor
public class LogroService implements ListLogrosInterface {

    private static final BigDecimal TONELADA = BigDecimal.valueOf(1000);

    /** El catalogo, en el orden en que se muestra. */
    static final List<Definicion> CATALOGO = List.of(
            new Definicion(MetricaLogro.SESIONES, definicion("PRIMER_ENTRENO", "Primer entreno",
                    "Registra tu primera sesión", "dumbbell", NivelLogro.BRONCE, "sesiones", 1)),
            new Definicion(MetricaLogro.SESIONES, definicion("ENTRENOS_10", "10 entrenos",
                    "Registra 10 sesiones", "dumbbell", NivelLogro.BRONCE, "sesiones", 10)),
            new Definicion(MetricaLogro.SESIONES, definicion("ENTRENOS_50", "50 entrenos",
                    "Registra 50 sesiones", "dumbbell", NivelLogro.PLATA, "sesiones", 50)),
            new Definicion(MetricaLogro.SESIONES, definicion("ENTRENOS_100", "100 entrenos",
                    "Registra 100 sesiones", "dumbbell", NivelLogro.ORO, "sesiones", 100)),
            new Definicion(MetricaLogro.SESIONES, definicion("ENTRENOS_250", "Veterano del hierro",
                    "Registra 250 sesiones", "dumbbell", NivelLogro.PLATINO, "sesiones", 250)),
            new Definicion(MetricaLogro.EJERCICIOS, definicion("EJERCICIOS_10", "Variado",
                    "Trabaja 10 ejercicios distintos", "compass", NivelLogro.PLATA, "ejercicios", 10)),
            new Definicion(MetricaLogro.EJERCICIOS, definicion("EJERCICIOS_25", "Repertorio completo",
                    "Trabaja 25 ejercicios distintos", "compass", NivelLogro.ORO, "ejercicios", 25)),
            new Definicion(MetricaLogro.TONELADAS, definicion("TONELADAS_10", "10 toneladas",
                    "Levanta 10 t de volumen en total", "trophy", NivelLogro.BRONCE, "t", 10)),
            new Definicion(MetricaLogro.TONELADAS, definicion("TONELADAS_100", "100 toneladas",
                    "Levanta 100 t de volumen en total", "trophy", NivelLogro.PLATA, "t", 100)),
            new Definicion(MetricaLogro.TONELADAS, definicion("TONELADAS_1000", "Mil toneladas",
                    "Levanta 1.000 t de volumen en total", "trophy", NivelLogro.ORO, "t", 1000)),
            new Definicion(MetricaLogro.RACHA_SEMANAS, definicion("RACHA_4", "Un mes sin fallar",
                    "Entrena 4 semanas seguidas", "flame", NivelLogro.PLATA, "semanas", 4)),
            new Definicion(MetricaLogro.RACHA_SEMANAS, definicion("RACHA_12", "Constancia",
                    "Entrena 12 semanas seguidas", "flame", NivelLogro.ORO, "semanas", 12)),
            new Definicion(MetricaLogro.RACHA_SEMANAS, definicion("RACHA_26", "Medio año imparable",
                    "Entrena 26 semanas seguidas", "flame", NivelLogro.PLATINO, "semanas", 26)));

    private final EstadisticasEntrenoPort estadisticasPort;

    @Override
    public List<Logro> list(Long usuarioId) {
        EstadisticasEntreno e = estadisticasPort.find(usuarioId);
        Map<MetricaLogro, List<Hito>> hitos = Map.of(
                MetricaLogro.SESIONES, CalculoLogros.unoPorFecha(e.getFechasSesion()),
                MetricaLogro.EJERCICIOS, CalculoLogros.unoPorFecha(e.getPrimerUsoEjercicios()),
                MetricaLogro.TONELADAS, CalculoLogros.unidadesEnteras(e.getVolumenPorSesion(), TONELADA),
                MetricaLogro.RACHA_SEMANAS, CalculoLogros.rachaMasLarga(e.getFechasSesion(), ChronoUnit.WEEKS));
        return CATALOGO.stream()
                .map(d -> CalculoLogros.evaluar(d.logro(), hitos.get(d.metrica())))
                .toList();
    }

    /** Un logro del catalogo con lo que mide. */
    record Definicion(MetricaLogro metrica, Logro logro) {
    }
}
