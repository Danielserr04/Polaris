package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.out.EstadisticasLogrosPort;
import com.polaris.kuiper.domain.model.EstadisticasLogros;
import com.polaris.kuiper.domain.model.EstadisticasLogros.MetaLogro;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.shared.logro.CalculoLogros.FechaImporte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lo que necesitan los logros de Kuiper: fechas de movimientos, balance por
 * dia agregado en la base y las metas con sus aportaciones.
 */
@Component("kuiperEstadisticasLogrosJpaAdapter")
@RequiredArgsConstructor
public class EstadisticasLogrosJpaAdapter implements EstadisticasLogrosPort {

    private final MovimientoRepository movimientoRepository;
    private final MetaAhorroRepository metaRepository;
    private final AportacionMetaRepository aportacionRepository;
    private final PresupuestoRepository presupuestoRepository;

    @Override
    public EstadisticasLogros find(Long usuarioId) {
        Map<Long, List<FechaImporte>> aportaciones = aportacionRepository.findByUsuarioId(usuarioId).stream()
                .collect(Collectors.groupingBy(AportacionMetaEntity::getMetaId,
                        Collectors.mapping(a -> new FechaImporte(a.getFecha(), a.getImporte()), Collectors.toList())));
        return EstadisticasLogros.builder()
                .fechasMovimiento(movimientoRepository.findFechasLogros(usuarioId))
                .balancePorDia(movimientoRepository.findImportesPorDia(usuarioId).stream()
                        .map(f -> new FechaImporte(f.getFecha(),
                                f.getTipo() == TipoMovimiento.GASTO ? f.getImporte().negate() : f.getImporte()))
                        .toList())
                .metas(metaRepository.findByUsuarioId(usuarioId).stream()
                        .map(m -> new MetaLogro(m.getImporteObjetivo(), aportaciones.getOrDefault(m.getId(), List.of())))
                        .toList())
                .numeroPresupuestos(presupuestoRepository.countByUsuarioId(usuarioId))
                .build();
    }
}
