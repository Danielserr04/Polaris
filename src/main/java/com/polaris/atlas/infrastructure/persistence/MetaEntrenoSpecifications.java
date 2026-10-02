package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.domain.model.MetaEntrenoFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, MetaEntrenoFilter) a JPA Specifications. El filtro por
 * usuarioId va siempre: es el aislamiento entre usuarios.
 */
public final class MetaEntrenoSpecifications {

    private MetaEntrenoSpecifications() {
    }

    public static Specification<MetaEntrenoEntity> from(Long usuarioId, MetaEntrenoFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), tipo(filter));
    }

    private static Specification<MetaEntrenoEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<MetaEntrenoEntity> tipo(MetaEntrenoFilter filter) {
        if (filter == null || filter.getTipo() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("tipo"), filter.getTipo());
    }
}
