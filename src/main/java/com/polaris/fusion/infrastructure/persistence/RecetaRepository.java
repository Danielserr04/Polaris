package com.polaris.fusion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data. Solo la usa RecetaJpaAdapter.
 */
public interface RecetaRepository extends JpaRepository<RecetaEntity, Long>,
        JpaSpecificationExecutor<RecetaEntity> {
}
