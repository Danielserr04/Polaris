package com.polaris.atlas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data. Solo la usa MetaEntrenoJpaAdapter.
 */
public interface MetaEntrenoRepository extends JpaRepository<MetaEntrenoEntity, Long>,
        JpaSpecificationExecutor<MetaEntrenoEntity> {
}
