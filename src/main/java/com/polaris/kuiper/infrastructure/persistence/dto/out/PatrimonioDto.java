package com.polaris.kuiper.infrastructure.persistence.dto.out;

import java.math.BigDecimal;

/**
 * Suma del saldo actual de todas tus cuentas, archivadas incluidas.
 */
public record PatrimonioDto(
        BigDecimal total,
        int numeroCuentas
) {
}
