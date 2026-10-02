package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.TransferenciaFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, TransferenciaFilter) a JPA Specifications.
 *
 * <p>El filtro por usuarioId va siempre: es el aislamiento entre usuarios. El
 * rango de fechas es inclusivo y la cuenta casa con el origen o el destino.
 */
public final class TransferenciaSpecifications {

    private TransferenciaSpecifications() {
    }

    public static Specification<TransferenciaEntity> from(Long usuarioId, TransferenciaFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), desde(filter), hasta(filter), porCuenta(filter));
    }

    private static Specification<TransferenciaEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<TransferenciaEntity> desde(TransferenciaFilter filter) {
        if (filter == null || filter.getDesde() == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), filter.getDesde());
    }

    private static Specification<TransferenciaEntity> hasta(TransferenciaFilter filter) {
        if (filter == null || filter.getHasta() == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("fecha"), filter.getHasta());
    }

    private static Specification<TransferenciaEntity> porCuenta(TransferenciaFilter filter) {
        if (filter == null || filter.getCuentaId() == null) {
            return null;
        }
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("cuentaOrigen").get("id"), filter.getCuentaId()),
                cb.equal(root.get("cuentaDestino").get("id"), filter.getCuentaId()));
    }
}
