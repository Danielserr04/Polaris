package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.PresupuestoFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, PresupuestoFilter) a JPA Specifications.
 *
 * <p>El filtro por usuarioId no es opcional como los demas: va siempre, es el
 * aislamiento entre usuarios.
 */
public final class PresupuestoSpecifications {

    private PresupuestoSpecifications() {
    }

    public static Specification<PresupuestoEntity> from(Long usuarioId, PresupuestoFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), porPeriodo(filter), porCategoria(filter));
    }

    private static Specification<PresupuestoEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<PresupuestoEntity> porPeriodo(PresupuestoFilter filter) {
        if (filter == null || filter.getPeriodo() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("periodo"), filter.getPeriodo());
    }

    private static Specification<PresupuestoEntity> porCategoria(PresupuestoFilter filter) {
        if (filter == null || filter.getCategoriaId() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("categoria").get("id"), filter.getCategoriaId());
    }
}
