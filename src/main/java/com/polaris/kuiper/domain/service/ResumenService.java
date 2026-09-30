package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.GetResumenMensualInterface;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.GastoCategoria;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.domain.model.PresupuestoFilter;
import com.polaris.kuiper.domain.model.ResumenMensual;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agrega en Java los movimientos del mes, en vez de con SUM/GROUP BY: un mes
 * de un usuario son decenas o pocos cientos de filas y el indice
 * (usuario_id, fecha) ya las acota. Si algun dia pesa, este es el unico
 * sitio que cambia. Ver docs/decisiones/014-resumen-mensual-agregado-en-servicio.md.
 */
@Service
@RequiredArgsConstructor
public class ResumenService implements GetResumenMensualInterface {

    /** Con escala 2 para que los totales salgan siempre como 0.00 y no como 0. */
    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2);

    private final MovimientoRepositoryPort movimientoRepository;
    private final PresupuestoRepositoryPort presupuestoRepository;

    @Override
    public ResumenMensual get(Long usuarioId, YearMonth periodo) {
        List<Movimiento> movimientos = movimientoRepository.findAll(usuarioId, MovimientoFilter.builder()
                .desde(periodo.atDay(1))
                .hasta(periodo.atEndOfMonth())
                .build());

        BigDecimal ingresos = total(movimientos, TipoMovimiento.INGRESO);
        BigDecimal gastos = total(movimientos, TipoMovimiento.GASTO);

        return ResumenMensual.builder()
                .periodo(periodo)
                .ingresos(ingresos)
                .gastos(gastos)
                .balance(ingresos.subtract(gastos))
                .gastoPorCategoria(gastoPorCategoria(usuarioId, movimientos))
                .build();
    }

    private BigDecimal total(List<Movimiento> movimientos, TipoMovimiento tipo) {
        return movimientos.stream()
                .filter(m -> m.getTipo() == tipo)
                .map(Movimiento::getImporte)
                .reduce(CERO, BigDecimal::add);
    }

    /**
     * Una fila por categoria con gasto en el mes o con presupuesto mensual, de
     * mayor a menor gasto (y por nombre en los empates).
     */
    private List<GastoCategoria> gastoPorCategoria(Long usuarioId, List<Movimiento> movimientos) {
        Map<Long, Categoria> categorias = new LinkedHashMap<>();
        Map<Long, BigDecimal> gastado = new LinkedHashMap<>();
        Map<Long, BigDecimal> limites = new LinkedHashMap<>();

        for (Movimiento m : movimientos) {
            if (m.getTipo() == TipoMovimiento.GASTO) {
                categorias.putIfAbsent(m.getCategoriaId(), m.getCategoria());
                gastado.merge(m.getCategoriaId(), m.getImporte(), BigDecimal::add);
            }
        }

        List<Presupuesto> presupuestos = presupuestoRepository.findAll(usuarioId,
                PresupuestoFilter.builder().periodo(PeriodoPresupuesto.MENSUAL).build());
        for (Presupuesto p : presupuestos) {
            categorias.putIfAbsent(p.getCategoriaId(), p.getCategoria());
            limites.put(p.getCategoriaId(), p.getImporteLimite());
        }

        return categorias.entrySet().stream()
                .map(e -> fila(e.getValue(), gastado.getOrDefault(e.getKey(), CERO), limites.get(e.getKey())))
                .sorted(Comparator.comparing(GastoCategoria::getGastado).reversed()
                        .thenComparing(g -> g.getCategoria().getNombre()))
                .toList();
    }

    private GastoCategoria fila(Categoria categoria, BigDecimal gastado, BigDecimal limite) {
        return GastoCategoria.builder()
                .categoria(categoria)
                .gastado(gastado)
                .limiteMensual(limite)
                .restante(limite == null ? null : limite.subtract(gastado))
                .build();
    }
}
