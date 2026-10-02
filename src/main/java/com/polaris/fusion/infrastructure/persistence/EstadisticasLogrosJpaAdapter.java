package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.out.EstadisticasLogrosPort;
import com.polaris.fusion.domain.model.EstadisticasLogros;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Fechas de comidas y recuentos de recetas y planes para los logros, sin cargar ninguno entero. */
@Component("fusionEstadisticasLogrosJpaAdapter")
@RequiredArgsConstructor
public class EstadisticasLogrosJpaAdapter implements EstadisticasLogrosPort {

    private final ComidaRepository comidaRepository;
    private final RecetaRepository recetaRepository;
    private final PlanComidaRepository planComidaRepository;
    private final ObjetivoNutricionalRepository objetivoRepository;

    @Override
    public EstadisticasLogros find(Long usuarioId) {
        return EstadisticasLogros.builder()
                .fechasComida(comidaRepository.findFechas(usuarioId))
                .numeroRecetas(recetaRepository.countByUsuarioId(usuarioId))
                .numeroPlanes(planComidaRepository.countByUsuarioId(usuarioId))
                .primerObjetivo(objetivoRepository.findPrimerVigenteDesde(usuarioId))
                .build();
    }
}
