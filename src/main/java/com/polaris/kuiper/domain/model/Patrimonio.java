package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Suma del saldo actual de todas las cuentas del usuario, archivadas
 * incluidas: archivar una cuenta no hace desaparecer su dinero.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patrimonio {

    private BigDecimal total;
    private int numeroCuentas;
}
