package com.polaris.nucleo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Spring Data. La usan PerfilJpaAdapter y EstadisticasLogrosJpaAdapter (perfil completo, para los logros).
 */
public interface PerfilRepository extends JpaRepository<PerfilEntity, Long>,
        JpaSpecificationExecutor<PerfilEntity> {

    Optional<PerfilEntity> findByUsuarioId(Long usuarioId);
}
