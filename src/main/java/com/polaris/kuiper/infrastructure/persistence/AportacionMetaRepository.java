package com.polaris.kuiper.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * Spring Data. La usan MetaAhorroJpaAdapter y, para los logros,
 * EstadisticasLogrosJpaAdapter. Las sumas entran por
 * idx_aportacion_meta_meta_fecha.
 */
public interface AportacionMetaRepository extends JpaRepository<AportacionMetaEntity, Long> {

    List<AportacionMetaEntity> findByMetaIdOrderByFechaDescIdDesc(Long metaId);

    @Query("select coalesce(sum(a.importe), 0) from AportacionMetaEntity a where a.metaId = :metaId")
    BigDecimal sumaPorMeta(@Param("metaId") Long metaId);

    /** Una fila por meta con aportaciones: [metaId, suma]. Para el listado sin N+1. */
    @Query("select a.metaId, sum(a.importe) from AportacionMetaEntity a where a.metaId in :metaIds group by a.metaId")
    List<Object[]> sumasPorMeta(@Param("metaIds") Collection<Long> metaIds);

    /** Todas las aportaciones del usuario, sin cargar sus metas, para los logros. */
    List<AportacionMetaEntity> findByUsuarioId(Long usuarioId);
}
