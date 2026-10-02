package com.polaris.kuiper.application.out;

import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.TipoMovimiento;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Movimiento, nunca de MovimientoEntity.
 */
public interface MovimientoRepositoryPort {

    Movimiento save(Movimiento movimiento);

    Optional<Movimiento> findById(Long id);

    /** Mas reciente primero. */
    List<Movimiento> findAll(Long usuarioId, MovimientoFilter filter);

    void deleteById(Long id);

    /** Para que CategoriaService pueda impedir borrar o cambiar de tipo una categoria en uso. */
    boolean existsByCategoriaId(Long categoriaId);

    /** Para que CuentaService pueda impedir borrar una cuenta con movimientos. */
    boolean existsByCuentaId(Long cuentaId);

    /**
     * Total de los movimientos de {@code tipo} de cada cuenta del usuario (SUM
     * agrupado por cuenta). Los movimientos sin cuenta y las cuentas sin
     * movimientos de ese tipo no aparecen.
     */
    Map<Long, BigDecimal> sumarPorCuenta(Long usuarioId, TipoMovimiento tipo);
}
