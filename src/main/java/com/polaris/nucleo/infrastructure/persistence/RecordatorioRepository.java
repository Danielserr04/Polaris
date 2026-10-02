package com.polaris.nucleo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data. Solo la usa RecordatorioJpaAdapter.
 */
public interface RecordatorioRepository extends JpaRepository<RecordatorioEntity, Long>,
        JpaSpecificationExecutor<RecordatorioEntity> {
}
