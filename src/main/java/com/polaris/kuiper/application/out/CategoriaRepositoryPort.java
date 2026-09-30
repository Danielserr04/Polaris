package com.polaris.kuiper.application.out;

import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaFilter;
import com.polaris.kuiper.domain.model.TipoMovimiento;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Categoria, nunca de CategoriaEntity.
 */
public interface CategoriaRepositoryPort {

    Categoria save(Categoria categoria);

    Optional<Categoria> findById(Long id);

    /** Ordenadas por nombre. */
    List<Categoria> findAll(Long usuarioId, CategoriaFilter filter);

    void deleteById(Long id);

    /** Para aplicar el unique (usuario_id, nombre, tipo) con un 409 legible. */
    Optional<Categoria> findByUsuarioIdAndNombreAndTipo(Long usuarioId, String nombre, TipoMovimiento tipo);
}
