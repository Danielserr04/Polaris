package com.polaris.nucleo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data. Solo la usa PerfilJpaAdapter.
 */
public interface PerfilRepository extends JpaRepository<PerfilEntity, Long>,
        JpaSpecificationExecutor<PerfilEntity> {
}
