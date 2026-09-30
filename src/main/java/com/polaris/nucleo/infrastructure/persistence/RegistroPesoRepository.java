package com.polaris.nucleo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Spring Data. Solo la usa RegistroPesoJpaAdapter.
 */
public interface RegistroPesoRepository extends JpaRepository<RegistroPesoEntity, Long>,
        JpaSpecificationExecutor<RegistroPesoEntity> {

    Optional<RegistroPesoEntity> findByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha);
}
