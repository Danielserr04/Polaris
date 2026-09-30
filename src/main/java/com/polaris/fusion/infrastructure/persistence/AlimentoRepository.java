package com.polaris.fusion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data. Solo la usa AlimentoJpaAdapter.
 */
public interface AlimentoRepository extends JpaRepository<AlimentoEntity, Long>,
        JpaSpecificationExecutor<AlimentoEntity> {
}
