package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.domain.model.FuenteExterna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data. Solo la usa TituloJpaAdapter.
 */
public interface TituloRepository extends JpaRepository<TituloEntity, Long>,
        JpaSpecificationExecutor<TituloEntity> {

    boolean existsByIdExternoAndFuenteExterna(String idExterno, FuenteExterna fuenteExterna);
}
