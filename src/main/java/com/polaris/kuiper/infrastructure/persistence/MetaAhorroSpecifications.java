package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.MetaAhorroFilter;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Traduce (usuarioId, MetaAhorroFilter) a JPA Specifications. El filtro por
 * usuarioId va siempre: es el aislamiento entre usuarios.
 *
 * <p>{@code completada} compara el objetivo con una subconsulta que suma las
 * aportaciones de la meta (0 si no tiene), la misma regla que
 * MetaAhorro.isCompletada.
 */
public final class MetaAhorroSpecifications {

    private MetaAhorroSpecifications() {
    }

    public static Specification<MetaAhorroEntity> from(Long usuarioId, MetaAhorroFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), porCompletada(filter));
    }

    private static Specification<MetaAhorroEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<MetaAhorroEntity> porCompletada(MetaAhorroFilter filter) {
        if (filter == null || filter.getCompletada() == null) {
            return null;
        }
        return (root, query, cb) -> {
            Subquery<BigDecimal> suma = query.subquery(BigDecimal.class);
            Root<AportacionMetaEntity> aportacion = suma.from(AportacionMetaEntity.class);
            suma.select(cb.coalesce(cb.sum(aportacion.<BigDecimal>get("importe")), BigDecimal.ZERO))
                    .where(cb.equal(aportacion.get("metaId"), root.get("id")));
            return filter.getCompletada()
                    ? cb.greaterThanOrEqualTo(suma, root.<BigDecimal>get("importeObjetivo"))
                    : cb.lessThan(suma, root.<BigDecimal>get("importeObjetivo"));
        };
    }
}
