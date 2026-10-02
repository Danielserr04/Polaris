package com.polaris.kuiper.application.out;

import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;

import java.time.LocalDate;
import java.util.List;
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

    /** Usuarios con algun movimiento entre las dos fechas, inclusive. Para el resumen mensual del job de avisos. */
    List<Long> findUsuarioIdsConMovimientos(LocalDate desde, LocalDate hasta);
}
