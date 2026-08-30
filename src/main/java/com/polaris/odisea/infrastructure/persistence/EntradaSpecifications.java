package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.domain.model.EntradaFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, EntradaFilter) a JPA Specifications.
 *
 * <p>El filtro por usuarioId no es opcional como los demas: va siempre, es el
 * aislamiento entre usuarios. tipo filtra por el titulo referenciado
 * (root.get("titulo").get("tipo")), lo que solo es posible porque la relacion
 * es @ManyToOne y no un id suelto.
 */
public final class EntradaSpecifications {

    private EntradaSpecifications() {
    }

    public static Specification<EntradaEntity> from(Long usuarioId, EntradaFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), porTipo(filter), porEstado(filter));
    }

    private static Specification<EntradaEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<EntradaEntity> porTipo(EntradaFilter filter) {
        if (filter == null || filter.getTipo() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("titulo").get("tipo"), filter.getTipo());
    }

    private static Specification<EntradaEntity> porEstado(EntradaFilter filter) {
        if (filter == null || filter.getEstado() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("estado"), filter.getEstado());
    }
}
