package com.polaris.fusion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data. Solo la usa RecetaIngredienteJpaAdapter, para la comprobacion de
 * uso de un alimento; los ingredientes se escriben siempre a traves de RecetaEntity.
 */
public interface RecetaIngredienteRepository extends JpaRepository<RecetaIngredienteEntity, Long> {

    boolean existsByAlimento_Id(Long alimentoId);
}
