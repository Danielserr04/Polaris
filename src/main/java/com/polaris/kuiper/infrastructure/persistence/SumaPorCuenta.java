package com.polaris.kuiper.infrastructure.persistence;

import java.math.BigDecimal;

/**
 * Proyeccion de Spring Data para los SUM agrupados por cuenta de
 * MovimientoRepository y TransferenciaRepository: una fila por cuenta.
 */
public interface SumaPorCuenta {

    Long getCuentaId();

    BigDecimal getTotal();
}
