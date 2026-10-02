package com.polaris.kuiper.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Spring Data. Solo la usa CuentaJpaAdapter (y los adaptadores de Movimiento,
 * Recurrente y Transferencia para cargar la Entity al guardar).
 */
public interface CuentaRepository extends JpaRepository<CuentaEntity, Long>,
        JpaSpecificationExecutor<CuentaEntity> {

    Optional<CuentaEntity> findByUsuarioIdAndNombre(Long usuarioId, String nombre);
}
