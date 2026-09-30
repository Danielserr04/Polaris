package com.polaris.kuiper.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data. Solo la usa MovimientoJpaAdapter.
 */
public interface MovimientoRepository extends JpaRepository<MovimientoEntity, Long>,
        JpaSpecificationExecutor<MovimientoEntity> {

    boolean existsByCategoria_Id(Long categoriaId);
}
