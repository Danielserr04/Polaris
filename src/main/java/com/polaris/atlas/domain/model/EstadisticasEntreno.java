package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Totales historicos de entreno de un usuario, calculados en la base. Lo que
 * necesitan los logros.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasEntreno {

    private long numeroSesiones;
    private long ejerciciosDistintos;
    /** Suma de reps por peso de todas las series, en kg. */
    private BigDecimal volumenTotal;
    /** Fechas distintas con sesion, sin orden garantizado. */
    private List<LocalDate> fechasSesion;
}
