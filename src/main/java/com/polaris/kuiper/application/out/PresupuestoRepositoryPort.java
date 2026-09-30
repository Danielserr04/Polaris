package com.polaris.kuiper.application.out;

import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.domain.model.PresupuestoFilter;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Presupuesto, nunca de PresupuestoEntity.
 */
public interface PresupuestoRepositoryPort {

    Presupuesto save(Presupuesto presupuesto);

    Optional<Presupuesto> findById(Long id);

    /** Ordenados por nombre de categoria. */
    List<Presupuesto> findAll(Long usuarioId, PresupuestoFilter filter);

    void deleteById(Long id);

    /** Para aplicar el unique (usuario_id, categoria_id, periodo) con un 409 legible. */
    Optional<Presupuesto> findByUsuarioIdAndCategoriaIdAndPeriodo(Long usuarioId, Long categoriaId,
                                                                   PeriodoPresupuesto periodo);

    /** Para que CategoriaService pueda impedir borrar o cambiar de tipo una categoria en uso. */
    boolean existsByCategoriaId(Long categoriaId);
}
