package com.polaris.fusion.infrastructure.persistence.dto.out;

import java.time.LocalDate;

/**
 * La ficha completa. Tambien es la fila del historico: no hay ListDto porque
 * el objetivo son cinco enteros y una fecha, no hay nada pesado que aligerar.
 */
public record ObjetivoNutricionalFormDto(
        Long id,
        Integer kcalDiarias,
        Integer proteinasObj,
        Integer carbosObj,
        Integer grasasObj,
        LocalDate vigenteDesde
) {
}
