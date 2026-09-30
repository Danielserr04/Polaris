package com.polaris.fusion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data. Solo la usa ComidaJpaAdapter.
 */
public interface ComidaRepository extends JpaRepository<ComidaEntity, Long>,
        JpaSpecificationExecutor<ComidaEntity> {
}
