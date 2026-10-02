package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.domain.model.RecetaFilter;
import com.polaris.shared.persistence.PatronLike;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, RecetaFilter) a JPA Specifications.
 *
 * <p>El filtro por usuarioId no es opcional: va siempre, es el aislamiento
 * entre usuarios. {@code q} busca en el nombre, sin distinguir mayusculas.
 */
public final class RecetaSpecifications {

    private RecetaSpecifications() {
    }

    public static Specification<RecetaEntity> from(Long usuarioId, RecetaFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), porTexto(filter));
    }

    private static Specification<RecetaEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<RecetaEntity> porTexto(RecetaFilter filter) {
        if (filter == null || filter.getQ() == null || filter.getQ().isBlank()) {
            return null;
        }
        String patron = PatronLike.contieneMinusculas(filter.getQ());
        return (root, query, cb) -> cb.like(cb.lower(root.get("nombre")), patron, PatronLike.ESCAPE);
    }
}
