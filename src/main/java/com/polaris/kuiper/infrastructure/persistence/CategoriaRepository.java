package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Spring Data. Solo la usa CategoriaJpaAdapter.
 */
public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long>,
        JpaSpecificationExecutor<CategoriaEntity> {

    Optional<CategoriaEntity> findByUsuarioIdAndNombreAndTipo(Long usuarioId, String nombre, TipoMovimiento tipo);
}
