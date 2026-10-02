package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.out.EstadisticasEntrenoPort;
import com.polaris.atlas.domain.model.EstadisticasEntreno;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Los totales de entreno, con tres consultas agregadas sobre sesion y serie_registro. */
@Component
@RequiredArgsConstructor
public class EstadisticasEntrenoJpaAdapter implements EstadisticasEntrenoPort {

    private final SesionRepository sesionRepository;
    private final SerieRegistroRepository serieRepository;

    @Override
    public EstadisticasEntreno find(Long usuarioId) {
        SerieRegistroRepository.TotalesSeriesFila series = serieRepository.findTotales(usuarioId);
        return EstadisticasEntreno.builder()
                .numeroSesiones(sesionRepository.countByUsuarioId(usuarioId))
                .ejerciciosDistintos(series.getEjerciciosDistintos() != null ? series.getEjerciciosDistintos() : 0)
                .volumenTotal(series.getVolumen() != null ? series.getVolumen() : BigDecimal.ZERO)
                .fechasSesion(sesionRepository.findFechasDistintas(usuarioId))
                .build();
    }
}
