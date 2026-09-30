package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.domain.model.AlimentoFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce AlimentoFilter a JPA Specifications. Catalogo compartido: no hay
 * filtro por usuario.
 */
public final class AlimentoSpecifications {

    private AlimentoSpecifications() {
    }

    public static Specification<AlimentoEntity> from(AlimentoFilter filter) {
        return Specification.allOf(porTexto(filter));
    }

    private static Specification<AlimentoEntity> porTexto(AlimentoFilter filter) {
        if (filter == null || filter.getTexto() == null || filter.getTexto().isBlank()) {
            return null;
        }
        String patron = "%" + filter.getTexto().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), patron),
                cb.like(cb.lower(root.get("marca")), patron));
    }
}
