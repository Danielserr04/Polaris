package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.domain.model.FuenteExterna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Spring Data. Solo la usa TituloJpaAdapter.
 */
public interface TituloRepository extends JpaRepository<TituloEntity, Long>,
        JpaSpecificationExecutor<TituloEntity> {

    boolean existsByIdExternoAndFuenteExterna(String idExterno, FuenteExterna fuenteExterna);

    Optional<TituloEntity> findByIdExternoAndFuenteExterna(String idExterno, FuenteExterna fuenteExterna);
}
