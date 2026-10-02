package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.domain.model.SuscripcionPushFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, SuscripcionPushFilter) a JPA Specifications. El filtro
 * esta vacio: las suscripciones solo se buscan por usuario o por endpoint.
 */
public final class SuscripcionPushSpecifications {

    private SuscripcionPushSpecifications() {
    }

    public static Specification<SuscripcionPushEntity> from(Long usuarioId, SuscripcionPushFilter filter) {
        return Specification.allOf(porUsuario(usuarioId));
    }

    public static Specification<SuscripcionPushEntity> porEndpoint(String endpoint) {
        return (root, query, cb) -> cb.equal(root.get("endpoint"), endpoint);
    }

    private static Specification<SuscripcionPushEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }
}
