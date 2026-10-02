package com.polaris.nucleo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Spring Data. Solo la usa MedidaCorporalJpaAdapter.
 */
public interface MedidaCorporalRepository extends JpaRepository<MedidaCorporalEntity, Long>,
        JpaSpecificationExecutor<MedidaCorporalEntity> {

    Optional<MedidaCorporalEntity> findByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha);
}
