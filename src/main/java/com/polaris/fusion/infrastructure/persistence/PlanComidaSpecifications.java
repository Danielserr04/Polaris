package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.domain.model.PlanComidaFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, PlanComidaFilter) a JPA Specifications. El filtro por
 * usuarioId va siempre: es el aislamiento entre usuarios.
 */
public final class PlanComidaSpecifications {

    private PlanComidaSpecifications() {
    }

    public static Specification<PlanComidaEntity> from(Long usuarioId, PlanComidaFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), porActivo(filter));
    }

    private static Specification<PlanComidaEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<PlanComidaEntity> porActivo(PlanComidaFilter filter) {
        if (filter == null || filter.getActivo() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("activo"), filter.getActivo());
    }
}
