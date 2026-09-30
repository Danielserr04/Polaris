package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.CategoriaFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, CategoriaFilter) a JPA Specifications.
 *
 * <p>El filtro por usuarioId no es opcional como los demas: va siempre, es el
 * aislamiento entre usuarios.
 */
public final class CategoriaSpecifications {

    private CategoriaSpecifications() {
    }

    public static Specification<CategoriaEntity> from(Long usuarioId, CategoriaFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), porTipo(filter));
    }

    private static Specification<CategoriaEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<CategoriaEntity> porTipo(CategoriaFilter filter) {
        if (filter == null || filter.getTipo() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("tipo"), filter.getTipo());
    }
}
