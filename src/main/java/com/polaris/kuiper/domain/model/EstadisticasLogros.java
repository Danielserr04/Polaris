package com.polaris.kuiper.domain.model;

import com.polaris.shared.logro.CalculoLogros.FechaImporte;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Lo que necesitan los logros de Kuiper, sin orden garantizado en las listas. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasLogros {

    /** La fecha de cada movimiento fuera de la papelera. */
    private List<LocalDate> fechasMovimiento;
    /** Ingresos menos gastos de cada dia con movimientos. */
    private List<FechaImporte> balancePorDia;
    /** Cada meta de ahorro con sus aportaciones. */
    private List<MetaLogro> metas;
    private long numeroPresupuestos;

    /** Una meta de ahorro reducida a lo que miran los logros. */
    public record MetaLogro(BigDecimal importeObjetivo, List<FechaImporte> aportaciones) {
    }
}
