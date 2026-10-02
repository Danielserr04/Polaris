package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.GetResumenMensualInterface;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.EstadoPresupuesto;
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
import java.util.Objects;

/**
 * Agrega en Java los movimientos del mes, en vez de con SUM/GROUP BY: un mes
 * de un usuario son decenas o pocos cientos de filas y el indice
 * (usuario_id, fecha) ya las acota. Si algun dia pesa, este es el unico
 * sitio que cambia. Ver docs/decisiones/014-resumen-mensual-agregado-en-servicio.md.
 *
 * <p>Cada fila lleva su porcentaje consumido y su estado (OK, AVISO,
 * EXCEDIDO) segun el umbral de alerta del presupuesto. Ver
 * docs/decisiones/035-presupuesto-umbral-de-alerta.md.
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
        List<GastoCategoria> filas = gastoPorCategoria(usuarioId, movimientos);

        return ResumenMensual.builder()
                .periodo(periodo)
                .ingresos(ingresos)
                .gastos(gastos)
                .balance(ingresos.subtract(gastos))
                .gastoPorCategoria(filas)
                .presupuestoTotal(filas.stream()
                        .map(GastoCategoria::getLimiteMensual)
                        .filter(Objects::nonNull)
                        .reduce(CERO, BigDecimal::add))
                .categoriasEnAviso(contar(filas, EstadoPresupuesto.AVISO))
                .categoriasExcedidas(contar(filas, EstadoPresupuesto.EXCEDIDO))
                .build();
    }

    private int contar(List<GastoCategoria> filas, EstadoPresupuesto estado) {
        return (int) filas.stream().filter(f -> f.getEstado() == estado).count();
    }

    private BigDecimal total(List<Movimiento> movimientos, TipoMovimiento tipo) {
        return movimientos.stream()
                .filter(m -> m.getTipo() == tipo)
                .map(Movimiento::getImporte)
                .reduce(CERO, BigDecimal::add);
    }

    /**
     * Una fila por categoria con gasto en el mes o con presupuesto mensual, de
     * mayor a menor gasto (y por nombre, sin distinguir mayusculas, en los empates).
     */
    private List<GastoCategoria> gastoPorCategoria(Long usuarioId, List<Movimiento> movimientos) {
        Map<Long, Categoria> categorias = new LinkedHashMap<>();
        Map<Long, BigDecimal> gastado = new LinkedHashMap<>();
        Map<Long, Presupuesto> limites = new LinkedHashMap<>();

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
            limites.put(p.getCategoriaId(), p);
        }

        return categorias.entrySet().stream()
                .map(e -> fila(e.getValue(), gastado.getOrDefault(e.getKey(), CERO), limites.get(e.getKey())))
                .sorted(Comparator.comparing(GastoCategoria::getGastado).reversed()
                        .thenComparing(g -> g.getCategoria().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private GastoCategoria fila(Categoria categoria, BigDecimal gastado, Presupuesto presupuesto) {
        BigDecimal limite = presupuesto == null ? null : presupuesto.getImporteLimite();
        Integer alerta = presupuesto == null ? null : presupuesto.getPorcentajeAlerta();
        return GastoCategoria.builder()
                .categoria(categoria)
                .gastado(gastado)
                .limiteMensual(limite)
                .restante(limite == null ? null : limite.subtract(gastado))
                .porcentaje(EstadoPresupuesto.porcentaje(gastado, limite))
                .porcentajeAlerta(alerta)
                .estado(EstadoPresupuesto.de(gastado, limite, alerta))
                .build();
    }
}
