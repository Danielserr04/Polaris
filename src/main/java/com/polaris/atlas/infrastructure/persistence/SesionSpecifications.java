package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.domain.model.SesionFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, SesionFilter) a JPA Specifications.
 *
 * <p>El filtro por usuarioId no es opcional como los demas: va siempre, es el
 * aislamiento entre usuarios. El rango de fechas es inclusivo.
 */
public final class SesionSpecifications {

    private SesionSpecifications() {
    }

    public static Specification<SesionEntity> from(Long usuarioId, SesionFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), desde(filter), hasta(filter), porRutina(filter));
    }

    private static Specification<SesionEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<SesionEntity> desde(SesionFilter filter) {
        if (filter == null || filter.getDesde() == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), filter.getDesde());
    }

    private static Specification<SesionEntity> hasta(SesionFilter filter) {
        if (filter == null || filter.getHasta() == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("fecha"), filter.getHasta());
    }

    private static Specification<SesionEntity> porRutina(SesionFilter filter) {
        if (filter == null || filter.getRutinaId() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("rutinaId"), filter.getRutinaId());
    }
}
