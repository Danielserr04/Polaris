package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.domain.model.TituloFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce TituloFilter a JPA Specifications. No listada como fichero propio en
 * docs/plantilla-modulo.md, pero es la clase que su propio ejemplo de
 * TituloJpaAdapter da por hecha (TituloSpecifications.from(filter)). Se anade
 * aqui y se corrige la plantilla para que las dos cosas dejen de contradecirse.
 */
public final class TituloSpecifications {

    private TituloSpecifications() {
    }

    public static Specification<TituloEntity> from(TituloFilter filter) {
        return Specification.allOf(porTipo(filter), porTexto(filter));
    }

    private static Specification<TituloEntity> porTipo(TituloFilter filter) {
        if (filter == null || filter.getTipo() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("tipo"), filter.getTipo());
    }

    private static Specification<TituloEntity> porTexto(TituloFilter filter) {
        if (filter == null || filter.getTexto() == null || filter.getTexto().isBlank()) {
            return null;
        }
        String patron = "%" + filter.getTexto().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("titulo")), patron),
                cb.like(cb.lower(root.get("tituloOriginal")), patron));
    }
}
