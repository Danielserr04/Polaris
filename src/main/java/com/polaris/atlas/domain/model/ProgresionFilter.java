package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Criterios de la progresion como datos. {@code ejercicioId} es obligatorio;
 * {@code desde} y {@code hasta} son un rango inclusivo de fechas de sesion,
 * ambos opcionales.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgresionFilter {

    private Long ejercicioId;
    private LocalDate desde;
    private LocalDate hasta;
}
