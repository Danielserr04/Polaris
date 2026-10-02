package com.polaris.nucleo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data. La usan MedidaCorporalJpaAdapter y EstadisticasLogrosJpaAdapter (fechas para los logros).
 */
public interface MedidaCorporalRepository extends JpaRepository<MedidaCorporalEntity, Long>,
        JpaSpecificationExecutor<MedidaCorporalEntity> {

    Optional<MedidaCorporalEntity> findByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha);

    /** Solo las fechas del usuario, para los logros. */
    @Query("select r.fecha from MedidaCorporalEntity r where r.usuarioId = :usuarioId")
    List<LocalDate> findFechas(@Param("usuarioId") Long usuarioId);
}
