package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.application.out.EstadisticasLogrosPort;
import com.polaris.odisea.domain.model.EstadisticasLogros;
import com.polaris.odisea.domain.model.EstadisticasLogros.Terminado;
import com.polaris.odisea.domain.model.EstadoEntrada;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Tipo y fecha de fin de lo terminado, y fechas de lo valorado, para los logros de Odisea. */
@Component("odiseaEstadisticasLogrosJpaAdapter")
@RequiredArgsConstructor
public class EstadisticasLogrosJpaAdapter implements EstadisticasLogrosPort {

    private final EntradaRepository entradaRepository;

    @Override
    public EstadisticasLogros find(Long usuarioId) {
        return EstadisticasLogros.builder()
                .terminados(entradaRepository.findTipoYFechaFin(usuarioId, EstadoEntrada.TERMINADO).stream()
                        .map(f -> new Terminado(f.getTipo(), f.getFechaFin()))
                        .toList())
                .fechasValoradas(entradaRepository.findFechaFinValoradas(usuarioId))
                .build();
    }
}
