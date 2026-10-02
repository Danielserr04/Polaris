package com.polaris.kuiper.infrastructure.persistence.dto.out;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La version ligera para el listado: sin creadaEn. Lleva los calculados
 * porque son lo que pinta cada tarjeta.
 */
public record MetaAhorroListDto(
        Long id,
        String nombre,
        BigDecimal importeObjetivo,
        LocalDate fechaLimite,
        String color,
        String icono,
        BigDecimal importeActual,
        BigDecimal porcentaje,
        BigDecimal restante,
        boolean completada,
        Long diasRestantes,
        BigDecimal ahorroMensualNecesario
) {
}
