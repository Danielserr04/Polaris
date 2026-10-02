package com.polaris.kuiper.infrastructure.persistence.dto.out;

import com.polaris.kuiper.domain.model.TipoCuenta;

import java.math.BigDecimal;

/**
 * La version ligera para el listado: lo que pinta la tarjeta de la cuenta,
 * con su saldo actual, pero sin el saldo inicial.
 */
public record CuentaListDto(
        Long id,
        String nombre,
        TipoCuenta tipo,
        BigDecimal saldoActual,
        String color,
        String icono,
        String banco,
        boolean archivada
) {
}
