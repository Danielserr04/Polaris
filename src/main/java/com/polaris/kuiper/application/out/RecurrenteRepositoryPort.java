package com.polaris.kuiper.application.out;

import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.domain.model.RecurrenteFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Recurrente, nunca de RecurrenteEntity.
 */
public interface RecurrenteRepositoryPort {

    Recurrente save(Recurrente recurrente);

    Optional<Recurrente> findById(Long id);

    /** Del proximo cargo mas cercano al mas lejano. */
    List<Recurrente> findAll(Long usuarioId, RecurrenteFilter filter);

    void deleteById(Long id);

    /** Activos con un cargo pendiente en {@code hoy} o antes, de cualquier usuario. Para el job. */
    List<Recurrente> findPendientes(LocalDate hoy);

    /** Para que CategoriaService pueda impedir borrar o cambiar de tipo una categoria en uso. */
    boolean existsByCategoriaId(Long categoriaId);

    /** Para que CuentaService pueda impedir borrar una cuenta con recurrentes. */
    boolean existsByCuentaId(Long cuentaId);
}
