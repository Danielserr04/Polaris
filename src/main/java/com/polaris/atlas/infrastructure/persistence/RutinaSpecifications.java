package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.domain.model.RutinaFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, RutinaFilter) a JPA Specifications.
 *
 * <p>El filtro por usuarioId no es opcional como los demas: va siempre, es el
 * aislamiento entre usuarios.
 */
public final class RutinaSpecifications {

    private RutinaSpecifications() {
    }

    public static Specification<RutinaEntity> from(Long usuarioId, RutinaFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), porActiva(filter));
    }

    private static Specification<RutinaEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<RutinaEntity> porActiva(RutinaFilter filter) {
        if (filter == null || filter.getActiva() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("activa"), filter.getActiva());
    }
}
