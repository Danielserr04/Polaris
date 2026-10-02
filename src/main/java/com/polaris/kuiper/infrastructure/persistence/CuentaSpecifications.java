package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.CuentaFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, CuentaFilter) a JPA Specifications. El filtro por
 * usuarioId va siempre: es el aislamiento entre usuarios.
 */
public final class CuentaSpecifications {

    private CuentaSpecifications() {
    }

    public static Specification<CuentaEntity> from(Long usuarioId, CuentaFilter filter) {
        return Specification.allOf(porUsuario(usuarioId), porArchivada(filter), porTipo(filter));
    }

    private static Specification<CuentaEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<CuentaEntity> porArchivada(CuentaFilter filter) {
        if (filter == null || filter.getArchivada() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("archivada"), filter.getArchivada());
    }

    private static Specification<CuentaEntity> porTipo(CuentaFilter filter) {
        if (filter == null || filter.getTipo() == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("tipo"), filter.getTipo());
    }
}
