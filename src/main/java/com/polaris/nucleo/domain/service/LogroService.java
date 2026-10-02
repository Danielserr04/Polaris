package com.polaris.nucleo.domain.service;

import com.polaris.nucleo.application.in.ListLogrosInterface;
import com.polaris.nucleo.application.out.EstadisticasLogrosPort;
import com.polaris.nucleo.domain.model.EstadisticasLogros;
import com.polaris.nucleo.domain.model.MetricaLogro;
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
 * Logros del cuerpo y el perfil, calculados al vuelo. El perfil no tiene
 * fecha: su logro sale conseguido sin dia. Ver
 * docs/decisiones/044-logros-globales-con-fecha-calculada.md.
 */
@Service("nucleoLogroService")
@RequiredArgsConstructor
public class LogroService implements ListLogrosInterface {

    /** El catalogo, en el orden en que se muestra. */
    static final List<Definicion> CATALOGO = List.of(
            new Definicion(MetricaLogro.PERFIL, definicion("PERFIL_COMPLETO", "Preséntate",
                    "Rellena altura, nacimiento, sexo y actividad en tu perfil", "user-round", NivelLogro.BRONCE,
                    "datos", 4)),
            new Definicion(MetricaLogro.PESAJES, definicion("PRIMER_PESAJE", "Primer pesaje",
                    "Apunta tu peso corporal", "scale", NivelLogro.BRONCE, "días", 1)),
            new Definicion(MetricaLogro.PESAJES, definicion("PESAJES_30", "30 pesajes",
                    "Apunta tu peso 30 días", "scale", NivelLogro.PLATA, "días", 30)),
            new Definicion(MetricaLogro.PESAJES, definicion("PESAJES_100", "100 pesajes",
                    "Apunta tu peso 100 días", "scale", NivelLogro.ORO, "días", 100)),
            new Definicion(MetricaLogro.PESAJES, definicion("PESAJES_365", "Un año en la báscula",
                    "Apunta tu peso 365 días", "scale", NivelLogro.PLATINO, "días", 365)),
            new Definicion(MetricaLogro.RACHA_PESAJES, definicion("RACHA_PESAJE_7", "Semana en la báscula",
                    "Apunta tu peso 7 días seguidos", "flame", NivelLogro.PLATA, "días", 7)),
            new Definicion(MetricaLogro.RACHA_PESAJES, definicion("RACHA_PESAJE_30", "Disciplina de hierro",
                    "Apunta tu peso 30 días seguidos", "flame", NivelLogro.ORO, "días", 30)),
            new Definicion(MetricaLogro.MEDIDAS, definicion("PRIMERA_MEDIDA", "Cinta métrica",
                    "Apunta tus medidas corporales", "orbit", NivelLogro.BRONCE, "días", 1)),
            new Definicion(MetricaLogro.MEDIDAS, definicion("MEDIDAS_12", "Seguimiento fino",
                    "Apunta tus medidas 12 días", "orbit", NivelLogro.PLATA, "días", 12)));

    private final EstadisticasLogrosPort estadisticasPort;

    @Override
    public List<Logro> list(Long usuarioId) {
        EstadisticasLogros e = estadisticasPort.find(usuarioId);
        Map<MetricaLogro, List<Hito>> hitos = Map.of(
                MetricaLogro.PESAJES, CalculoLogros.diasDistintos(e.getFechasPeso()),
                MetricaLogro.RACHA_PESAJES, CalculoLogros.rachaMasLarga(e.getFechasPeso(), ChronoUnit.DAYS),
                MetricaLogro.MEDIDAS, CalculoLogros.diasDistintos(e.getFechasMedida()),
                MetricaLogro.PERFIL, e.getCamposPerfil() > 0 ? List.of(new Hito(null, e.getCamposPerfil())) : List.of());
        return CATALOGO.stream()
                .map(d -> CalculoLogros.evaluar(d.logro(), hitos.get(d.metrica())))
                .toList();
    }

    /** Un logro del catalogo con lo que mide. */
    record Definicion(MetricaLogro metrica, Logro logro) {
    }
}
