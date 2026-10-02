package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.domain.model.RecordatorioFilter;
import com.polaris.nucleo.domain.model.TipoRecordatorio;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce (usuarioId, RecordatorioFilter) a JPA Specifications.
 *
 * <p>El usuario no es opcional: es el aislamiento entre usuarios. El filtro
 * esta vacio porque el listado son siempre todos los tipos del usuario.
 */
public final class RecordatorioSpecifications {

    private RecordatorioSpecifications() {
    }

    public static Specification<RecordatorioEntity> from(Long usuarioId, RecordatorioFilter filter) {
        return Specification.allOf(porUsuario(usuarioId));
    }

    public static Specification<RecordatorioEntity> deTipo(Long usuarioId, TipoRecordatorio tipo) {
        return Specification.allOf(porUsuario(usuarioId), porTipo(tipo));
    }

    private static Specification<RecordatorioEntity> porUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<RecordatorioEntity> porTipo(TipoRecordatorio tipo) {
        return (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }
}
