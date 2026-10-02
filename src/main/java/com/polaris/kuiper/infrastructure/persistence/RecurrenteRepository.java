package com.polaris.kuiper.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data. Solo la usa RecurrenteJpaAdapter.
 */
public interface RecurrenteRepository extends JpaRepository<RecurrenteEntity, Long>,
        JpaSpecificationExecutor<RecurrenteEntity> {

    /** Entra por idx_recurrente_activo_proxima. */
    List<RecurrenteEntity> findByActivoTrueAndProximaFechaLessThanEqualOrderByIdAsc(LocalDate hoy);

    boolean existsByCategoria_Id(Long categoriaId);
}
