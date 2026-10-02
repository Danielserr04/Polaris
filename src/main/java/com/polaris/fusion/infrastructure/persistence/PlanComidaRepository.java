package com.polaris.fusion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data. Solo la usa PlanComidaJpaAdapter.
 */
public interface PlanComidaRepository extends JpaRepository<PlanComidaEntity, Long>,
        JpaSpecificationExecutor<PlanComidaEntity> {

    /** Un solo UPDATE: {@code id} queda activo y el resto de planes del usuario no. */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update PlanComidaEntity p set p.activo = case when p.id = :id then true else false end "
            + "where p.usuarioId = :usuarioId")
    int activarUnico(@Param("usuarioId") Long usuarioId, @Param("id") Long id);
}
