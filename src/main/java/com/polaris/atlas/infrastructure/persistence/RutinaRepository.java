package com.polaris.atlas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Spring Data. Solo la usa RutinaJpaAdapter.
 */
public interface RutinaRepository extends JpaRepository<RutinaEntity, Long>,
        JpaSpecificationExecutor<RutinaEntity> {

    /** El nombre se compara con la collation de la columna: sin mayusculas ni tildes. */
    Optional<RutinaEntity> findByUsuarioIdAndNombre(Long usuarioId, String nombre);
}
