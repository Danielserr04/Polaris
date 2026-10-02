package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Rango inclusivo de fechas de sesion; los dos extremos son opcionales.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrabajoMuscularFilter {

    private LocalDate desde;
    private LocalDate hasta;
}
