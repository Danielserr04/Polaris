package com.polaris.nucleo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Spring Data. Solo la usa SuscripcionPushJpaAdapter.
 */
public interface SuscripcionPushRepository extends JpaRepository<SuscripcionPushEntity, Long>,
        JpaSpecificationExecutor<SuscripcionPushEntity> {

    @Query("select distinct s.usuarioId from SuscripcionPushEntity s order by s.usuarioId")
    List<Long> findUsuarioIds();
}
