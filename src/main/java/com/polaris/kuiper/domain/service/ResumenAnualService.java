package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.GetResumenAnualInterface;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.domain.model.EstadoPresupuesto;
import com.polaris.kuiper.domain.model.GastoAnualCategoria;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.domain.model.PresupuestoFilter;
import com.polaris.kuiper.domain.model.ResumenAnual;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compara cada presupuesto ANUAL con lo gastado en esa categoria del 1 de
 * enero al 31 de diciembre. Agrega en Java por la misma razon que el resumen
 * mensual (docs/decisiones/014-resumen-mensual-agregado-en-servicio.md): un
 * anio de un usuario son pocos miles de filas como mucho. Si no hay
 * presupuestos anuales no pide los movimientos.
 * Ver docs/decisiones/035-presupuesto-umbral-de-alerta.md.
 */
@Service
@RequiredArgsConstructor
public class ResumenAnualService implements GetResumenAnualInterface {

    /** Con escala 2 para que lo gastado salga siempre como 0.00 y no como 0. */
    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2);

    private final MovimientoRepositoryPort movimientoRepository;
    private final PresupuestoRepositoryPort presupuestoRepository;

    @Override
    public ResumenAnual get(Long usuarioId, int anio) {
        List<Presupuesto> presupuestos = presupuestoRepository.findAll(usuarioId,
                PresupuestoFilter.builder().periodo(PeriodoPresupuesto.ANUAL).build());
        if (presupuestos.isEmpty()) {
            return ResumenAnual.builder().anio(anio).presupuestos(List.of()).build();
        }

        List<Movimiento> movimientos = movimientoRepository.findAll(usuarioId, MovimientoFilter.builder()
                .tipo(TipoMovimiento.GASTO)
                .desde(LocalDate.of(anio, 1, 1))
                .hasta(LocalDate.of(anio, 12, 31))
                .build());

        Map<Long, BigDecimal> gastado = new HashMap<>();
        for (Movimiento m : movimientos) {
            // El filtro ya pide solo gastos; se comprueba igual, como en ResumenService.
            if (m.getTipo() == TipoMovimiento.GASTO) {
                gastado.merge(m.getCategoriaId(), m.getImporte(), BigDecimal::add);
            }
        }

        List<GastoAnualCategoria> filas = presupuestos.stream()
                .map(p -> fila(p, gastado.getOrDefault(p.getCategoriaId(), CERO)))
                .sorted(Comparator.comparing(GastoAnualCategoria::getPorcentaje).reversed()
                        .thenComparing(g -> g.getCategoria().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        return ResumenAnual.builder().anio(anio).presupuestos(filas).build();
    }

    private GastoAnualCategoria fila(Presupuesto presupuesto, BigDecimal gastado) {
        BigDecimal limite = presupuesto.getImporteLimite();
        Integer alerta = presupuesto.getPorcentajeAlerta();
        return GastoAnualCategoria.builder()
                .categoria(presupuesto.getCategoria())
                .gastado(gastado)
                .limite(limite)
                .restante(limite.subtract(gastado))
                .porcentaje(EstadoPresupuesto.porcentaje(gastado, limite))
                .porcentajeAlerta(alerta)
                .estado(EstadoPresupuesto.de(gastado, limite, alerta))
                .build();
    }
}
