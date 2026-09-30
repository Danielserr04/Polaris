package com.polaris.atlas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data. Solo la usa EjercicioJpaAdapter.
 */
public interface EjercicioRepository extends JpaRepository<EjercicioEntity, Long>,
        JpaSpecificationExecutor<EjercicioEntity> {

    /** El nombre se compara con la collation de la columna: sin mayusculas ni tildes. */
    @Query("select e from EjercicioEntity e "
            + "where e.nombre = :nombre and (e.usuarioId is null or e.usuarioId = :usuarioId)")
    List<EjercicioEntity> findVisiblesByNombre(@Param("usuarioId") Long usuarioId, @Param("nombre") String nombre);
}
