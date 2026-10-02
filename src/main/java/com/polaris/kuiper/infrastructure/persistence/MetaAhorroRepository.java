package com.polaris.kuiper.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data. La usan MetaAhorroJpaAdapter y EstadisticasLogrosJpaAdapter (logros).
 */
public interface MetaAhorroRepository extends JpaRepository<MetaAhorroEntity, Long>,
        JpaSpecificationExecutor<MetaAhorroEntity> {

    /** Entra por uk_meta_ahorro_usuario_nombre; compara con la collation (sin mayusculas ni tildes). */
    Optional<MetaAhorroEntity> findByUsuarioIdAndNombre(Long usuarioId, String nombre);

    List<MetaAhorroEntity> findByUsuarioId(Long usuarioId);
}
