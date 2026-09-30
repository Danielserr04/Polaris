package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.domain.model.EjercicioFilter;
import com.polaris.shared.persistence.PatronLike;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, EjercicioFilter) a JPA Specifications.
 *
 * <p>La visibilidad va siempre, no es un filtro opcional: el usuario ve el
 * catalogo (usuario_id nulo) y sus ejercicios, nunca los de otro.
 */
public final class EjercicioSpecifications {

    private EjercicioSpecifications() {
    }

    public static Specification<EjercicioEntity> from(Long usuarioId, EjercicioFilter filter) {
        return Specification.allOf(visiblePara(usuarioId), porGrupoMuscular(filter), porTexto(filter));
    }

    private static Specification<EjercicioEntity> visiblePara(Long usuarioId) {
        return (root, query, cb) -> cb.or(
                cb.isNull(root.get("usuarioId")),
                cb.equal(root.get("usuarioId"), usuarioId));
    }

    /** Igualdad exacta: la collation de la columna ya ignora mayusculas y tildes. */
    private static Specification<EjercicioEntity> porGrupoMuscular(EjercicioFilter filter) {
        if (filter == null || filter.getGrupoMuscular() == null || filter.getGrupoMuscular().isBlank()) {
            return null;
        }
        String grupo = filter.getGrupoMuscular().trim();
        return (root, query, cb) -> cb.equal(root.get("grupoMuscular"), grupo);
    }

    private static Specification<EjercicioEntity> porTexto(EjercicioFilter filter) {
        if (filter == null || filter.getTexto() == null || filter.getTexto().isBlank()) {
            return null;
        }
        String patron = PatronLike.contieneMinusculas(filter.getTexto());
        return (root, query, cb) -> cb.like(cb.lower(root.get("nombre")), patron, PatronLike.ESCAPE);
    }
}
