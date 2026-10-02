package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.NotificacionFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, NotificacionFilter) a JPA Specifications. El filtro por
 * usuarioId va siempre: es el aislamiento entre usuarios.
 */
public final class NotificacionSpecifications {

    private NotificacionSpecifications() {
    }

    public static Specification<NotificacionEntity> from(Long usuarioId, NotificacionFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), soloNoLeidas(filter));
    }

    private static Specification<NotificacionEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<NotificacionEntity> soloNoLeidas(NotificacionFilter filter) {
        if (filter == null || !Boolean.TRUE.equals(filter.getSoloNoLeidas())) {
            return null;
        }
        return (root, query, cb) -> cb.isFalse(root.get("leida"));
    }
}
