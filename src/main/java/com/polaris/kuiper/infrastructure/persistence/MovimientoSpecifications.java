package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.MovimientoFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, MovimientoFilter) a JPA Specifications.
 *
 * <p>El filtro por usuarioId no es opcional como los demas: va siempre, es el
 * aislamiento entre usuarios. El rango de fechas es inclusivo.
 */
public final class MovimientoSpecifications {

    private MovimientoSpecifications() {
    }

    public static Specification<MovimientoEntity> from(Long usuarioId, MovimientoFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), desde(filter), hasta(filter),
                porCategoria(filter), porTipo(filter), porCuenta(filter));
    }

    private static Specification<MovimientoEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<MovimientoEntity> desde(MovimientoFilter filter) {
        if (filter == null || filter.getDesde() == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), filter.getDesde());
    }

    private static Specification<MovimientoEntity> hasta(MovimientoFilter filter) {
        if (filter == null || filter.getHasta() == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("fecha"), filter.getHasta());
    }

    private static Specification<MovimientoEntity> porCategoria(MovimientoFilter filter) {
        if (filter == null || filter.getCategoriaId() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("categoria").get("id"), filter.getCategoriaId());
    }

    private static Specification<MovimientoEntity> porTipo(MovimientoFilter filter) {
        if (filter == null || filter.getTipo() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("tipo"), filter.getTipo());
    }

    private static Specification<MovimientoEntity> porCuenta(MovimientoFilter filter) {
        if (filter == null || filter.getCuentaId() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("cuenta").get("id"), filter.getCuentaId());
    }
}
