package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.domain.model.PerfilFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, PerfilFilter) a JPA Specifications.
 *
 * <p>Hoy solo filtra por usuario, que no es opcional: es el aislamiento entre
 * usuarios. PerfilFilter esta vacio porque Perfil no tiene listado. Ver
 * docs/decisiones/009-perfil-unico-por-usuario.md.
 */
public final class PerfilSpecifications {

    private PerfilSpecifications() {
    }

    public static Specification<PerfilEntity> from(Long usuarioId, PerfilFilter filter) {
        return Specification.allOf(porUsuario(usuarioId));
    }

    private static Specification<PerfilEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }
}
