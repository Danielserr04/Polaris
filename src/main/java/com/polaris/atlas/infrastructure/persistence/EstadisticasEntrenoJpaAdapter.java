package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.out.EstadisticasEntrenoPort;
import com.polaris.atlas.domain.model.EstadisticasEntreno;
import com.polaris.shared.logro.CalculoLogros.FechaImporte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** La historia de entreno para los logros, con tres consultas agregadas sobre sesion y serie_registro. */
@Component
@RequiredArgsConstructor
public class EstadisticasEntrenoJpaAdapter implements EstadisticasEntrenoPort {

    private final SesionRepository sesionRepository;
    private final SerieRegistroRepository serieRepository;

    @Override
    public EstadisticasEntreno find(Long usuarioId) {
        return EstadisticasEntreno.builder()
                .fechasSesion(sesionRepository.findFechas(usuarioId))
                .primerUsoEjercicios(serieRepository.findPrimerUsoPorEjercicio(usuarioId))
                .volumenPorSesion(serieRepository.findVolumenPorSesion(usuarioId).stream()
                        .map(f -> new FechaImporte(f.getFecha(),
                                f.getVolumen() != null ? f.getVolumen() : BigDecimal.ZERO))
                        .toList())
                .build();
    }
}
