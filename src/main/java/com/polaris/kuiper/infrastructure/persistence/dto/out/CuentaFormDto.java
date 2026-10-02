package com.polaris.kuiper.infrastructure.persistence.dto.out;

import com.polaris.kuiper.domain.model.TipoCuenta;

import java.math.BigDecimal;

/**
 * La ficha completa que devuelve el detalle, con el saldo actual calculado.
 */
public record CuentaFormDto(
        Long id,
        String nombre,
        TipoCuenta tipo,
        BigDecimal saldoInicial,
        BigDecimal saldoActual,
        String color,
        String icono,
        String banco,
        boolean archivada
) {
}
