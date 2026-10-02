package com.polaris.kuiper.infrastructure.persistence.dto.out;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * La ficha completa que devuelve el detalle, el alta, la edicion y cada
 * aportacion. importeActual, porcentaje, restante, completada, diasRestantes
 * y ahorroMensualNecesario son calculados, no columnas.
 */
public record MetaAhorroFormDto(
        Long id,
        String nombre,
        BigDecimal importeObjetivo,
        LocalDate fechaLimite,
        String color,
        String icono,
        Instant creadaEn,
        BigDecimal importeActual,
        BigDecimal porcentaje,
        BigDecimal restante,
        boolean completada,
        Long diasRestantes,
        BigDecimal ahorroMensualNecesario
) {
}
