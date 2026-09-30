package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.domain.model.FuenteAlimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Spring Data. Solo la usa AlimentoJpaAdapter.
 */
public interface AlimentoRepository extends JpaRepository<AlimentoEntity, Long>,
        JpaSpecificationExecutor<AlimentoEntity> {

    Optional<AlimentoEntity> findByFuenteExternaAndIdExterno(FuenteAlimento fuenteExterna, String idExterno);
}
