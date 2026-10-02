package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.domain.model.MedidaCorporalFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, MedidaCorporalFilter) a JPA Specifications.
 *
 * <p>El filtro por usuarioId no es opcional como los demas: va siempre, es el
 * aislamiento entre usuarios. El rango de fechas es inclusivo.
 */
public final class MedidaCorporalSpecifications {

    private MedidaCorporalSpecifications() {
    }

    public static Specification<MedidaCorporalEntity> from(Long usuarioId, MedidaCorporalFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), desde(filter), hasta(filter));
    }

    private static Specification<MedidaCorporalEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<MedidaCorporalEntity> desde(MedidaCorporalFilter filter) {
        if (filter == null || filter.getDesde() == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), filter.getDesde());
    }

    private static Specification<MedidaCorporalEntity> hasta(MedidaCorporalFilter filter) {
        if (filter == null || filter.getHasta() == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("fecha"), filter.getHasta());
    }
}
