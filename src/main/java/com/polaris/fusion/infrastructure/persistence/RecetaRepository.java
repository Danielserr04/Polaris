package com.polaris.fusion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data. La usan RecetaJpaAdapter y EstadisticasLogrosJpaAdapter (logros).
 */
public interface RecetaRepository extends JpaRepository<RecetaEntity, Long>,
        JpaSpecificationExecutor<RecetaEntity> {

    long countByUsuarioId(Long usuarioId);
}
