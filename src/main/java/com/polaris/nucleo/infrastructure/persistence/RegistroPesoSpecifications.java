package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.domain.model.RegistroPesoFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, RegistroPesoFilter) a JPA Specifications.
 *
 * <p>El filtro por usuarioId no es opcional como los demas: va siempre, es el
 * aislamiento entre usuarios. El rango de fechas es inclusivo.
 */
public final class RegistroPesoSpecifications {

    private RegistroPesoSpecifications() {
    }

    public static Specification<RegistroPesoEntity> from(Long usuarioId, RegistroPesoFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), desde(filter), hasta(filter));
    }

    private static Specification<RegistroPesoEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<RegistroPesoEntity> desde(RegistroPesoFilter filter) {
        if (filter == null || filter.getDesde() == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), filter.getDesde());
    }

    private static Specification<RegistroPesoEntity> hasta(RegistroPesoFilter filter) {
        if (filter == null || filter.getHasta() == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("fecha"), filter.getHasta());
    }
}
