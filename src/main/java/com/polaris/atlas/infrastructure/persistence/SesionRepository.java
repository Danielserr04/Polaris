package com.polaris.atlas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * Spring Data. Solo la usa SesionJpaAdapter.
 */
public interface SesionRepository extends JpaRepository<SesionEntity, Long>,
        JpaSpecificationExecutor<SesionEntity> {

    boolean existsByRutinaId(Long rutinaId);

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
