package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.domain.model.ComidaFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, ComidaFilter) a JPA Specifications.
 *
 * <p>El filtro por usuarioId no es opcional como los demas: va siempre, es el
 * aislamiento entre usuarios. El rango de fechas es inclusivo.
 */
public final class ComidaSpecifications {

    private ComidaSpecifications() {
    }

    public static Specification<ComidaEntity> from(Long usuarioId, ComidaFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), porFecha(filter), desde(filter), hasta(filter),
                porMomento(filter));
    }

    private static Specification<ComidaEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<ComidaEntity> porFecha(ComidaFilter filter) {
        if (filter == null || filter.getFecha() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("fecha"), filter.getFecha());
    }

    private static Specification<ComidaEntity> desde(ComidaFilter filter) {
        if (filter == null || filter.getDesde() == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), filter.getDesde());
    }

    private static Specification<ComidaEntity> hasta(ComidaFilter filter) {
        if (filter == null || filter.getHasta() == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("fecha"), filter.getHasta());
    }

    private static Specification<ComidaEntity> porMomento(ComidaFilter filter) {
        if (filter == null || filter.getMomento() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("momento"), filter.getMomento());
    }
}
