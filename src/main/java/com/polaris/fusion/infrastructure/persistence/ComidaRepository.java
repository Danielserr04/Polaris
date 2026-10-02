package com.polaris.fusion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data. La usan ComidaJpaAdapter y EstadisticasLogrosJpaAdapter (logros).
 */
public interface ComidaRepository extends JpaRepository<ComidaEntity, Long>,
        JpaSpecificationExecutor<ComidaEntity> {

    /** La fecha de cada comida, repetidas incluidas: cuenta comidas, dias y rachas de los logros. */
    @Query("select c.fecha from ComidaEntity c where c.usuarioId = :usuarioId")
    List<LocalDate> findFechas(@Param("usuarioId") Long usuarioId);
}
