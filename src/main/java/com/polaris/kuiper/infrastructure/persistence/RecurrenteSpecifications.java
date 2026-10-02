package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.RecurrenteFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, RecurrenteFilter) a JPA Specifications. El filtro por
 * usuarioId va siempre: es el aislamiento entre usuarios.
 */
public final class RecurrenteSpecifications {

    private RecurrenteSpecifications() {
    }

    public static Specification<RecurrenteEntity> from(Long usuarioId, RecurrenteFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), porActivo(filter), porTipo(filter), porCategoria(filter));
    }

    private static Specification<RecurrenteEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<RecurrenteEntity> porActivo(RecurrenteFilter filter) {
        if (filter == null || filter.getActivo() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("activo"), filter.getActivo());
    }

    private static Specification<RecurrenteEntity> porTipo(RecurrenteFilter filter) {
        if (filter == null || filter.getTipo() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("tipo"), filter.getTipo());
    }

    private static Specification<RecurrenteEntity> porCategoria(RecurrenteFilter filter) {
        if (filter == null || filter.getCategoriaId() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("categoria").get("id"), filter.getCategoriaId());
    }
}
