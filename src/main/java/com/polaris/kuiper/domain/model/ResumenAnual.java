package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Los presupuestos ANUALES frente a lo gastado en el anio, calculado al vuelo
 * como el resumen mensual. Ver docs/decisiones/035-presupuesto-umbral-de-alerta.md.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenAnual {

    private int anio;
    /** Una fila por presupuesto anual, del mas consumido al menos. */
    private List<GastoAnualCategoria> presupuestos;
}
