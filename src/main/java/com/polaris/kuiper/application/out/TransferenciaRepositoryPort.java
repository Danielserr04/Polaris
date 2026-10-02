package com.polaris.kuiper.application.out;

import com.polaris.kuiper.domain.model.Transferencia;
import com.polaris.kuiper.domain.model.TransferenciaFilter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Transferencia, nunca de TransferenciaEntity.
 */
public interface TransferenciaRepositoryPort {

    Transferencia save(Transferencia transferencia);

    Optional<Transferencia> findById(Long id);

    /** Mas reciente primero. */
    List<Transferencia> findAll(Long usuarioId, TransferenciaFilter filter);

    void deleteById(Long id);

    /** Total recibido por cada cuenta del usuario (SUM agrupado). Las cuentas sin transferencias no aparecen. */
    Map<Long, BigDecimal> sumarEntrantesPorCuenta(Long usuarioId);

    /** Total enviado desde cada cuenta del usuario (SUM agrupado). Las cuentas sin transferencias no aparecen. */
    Map<Long, BigDecimal> sumarSalientesPorCuenta(Long usuarioId);

    /** Para que CuentaService pueda impedir borrar una cuenta con transferencias. */
    boolean existsByCuentaId(Long cuentaId);
}
