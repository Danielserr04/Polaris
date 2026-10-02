package com.polaris.fusion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data. Solo la usa PlanComidaLineaJpaAdapter, para las comprobaciones
 * de uso; las lineas se escriben siempre a traves de PlanComidaEntity.
 */
public interface PlanComidaLineaRepository extends JpaRepository<PlanComidaLineaEntity, Long> {

    boolean existsByAlimento_Id(Long alimentoId);

    boolean existsByReceta_Id(Long recetaId);
}
