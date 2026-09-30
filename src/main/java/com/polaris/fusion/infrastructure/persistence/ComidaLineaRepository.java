package com.polaris.fusion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data. Solo la usa ComidaLineaJpaAdapter, para la comprobacion de uso
 * de un alimento; las lineas se escriben siempre a traves de ComidaEntity.
 */
public interface ComidaLineaRepository extends JpaRepository<ComidaLineaEntity, Long> {

    boolean existsByAlimento_Id(Long alimentoId);
}
