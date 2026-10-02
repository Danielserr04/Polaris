package com.polaris.nucleo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data. La usan RegistroPesoJpaAdapter y EstadisticasLogrosJpaAdapter (fechas para los logros).
 */
public interface RegistroPesoRepository extends JpaRepository<RegistroPesoEntity, Long>,
        JpaSpecificationExecutor<RegistroPesoEntity> {

    Optional<RegistroPesoEntity> findByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha);

    /** Solo las fechas del usuario, para los logros. */
    @Query("select r.fecha from RegistroPesoEntity r where r.usuarioId = :usuarioId")
    List<LocalDate> findFechas(@Param("usuarioId") Long usuarioId);
}
