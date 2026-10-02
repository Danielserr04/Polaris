package com.polaris.kuiper.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data. Solo la usa NotificacionJpaAdapter.
 */
public interface NotificacionRepository extends JpaRepository<NotificacionEntity, Long>,
        JpaSpecificationExecutor<NotificacionEntity> {

    /** Entra por el unique uk_notificacion_usuario_clave. */
    boolean existsByUsuarioIdAndClave(Long usuarioId, String clave);

    long countByUsuarioIdAndLeidaFalse(Long usuarioId);

    @Transactional
    @Modifying
    @Query("update NotificacionEntity n set n.leida = true where n.usuarioId = :usuarioId and n.leida = false")
    int marcarTodasLeidas(@Param("usuarioId") Long usuarioId);

    @Transactional
    @Modifying
    @Query("delete from NotificacionEntity n where n.usuarioId = :usuarioId and n.leida = true")
    int deleteLeidas(@Param("usuarioId") Long usuarioId);
}
