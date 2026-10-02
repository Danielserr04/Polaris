package com.polaris.atlas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Spring Data. La usan SesionJpaAdapter y EstadisticasEntrenoJpaAdapter.
 */
public interface SesionRepository extends JpaRepository<SesionEntity, Long>,
        JpaSpecificationExecutor<SesionEntity> {

    boolean existsByRutinaId(Long rutinaId);

    long countByUsuarioId(Long usuarioId);

    /** Dias distintos con sesion, para la racha de los logros. */
    @Query("select distinct s.fecha from SesionEntity s where s.usuarioId = :usuarioId")
    List<LocalDate> findFechasDistintas(@Param("usuarioId") Long usuarioId);

    /**
     * Solo id y nombre de las rutinas: una relacion a RutinaEntity arrastraria
     * sus lineas en cada lectura de sesion.
     */
    @Query("select r.id as id, r.nombre as nombre from RutinaEntity r where r.id in :ids")
    List<NombreRutina> findNombresRutina(@Param("ids") Collection<Long> ids);

    interface NombreRutina {
        Long getId();

        String getNombre();
    }
}
