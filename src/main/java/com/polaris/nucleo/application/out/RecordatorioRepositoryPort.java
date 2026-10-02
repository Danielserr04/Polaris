package com.polaris.nucleo.application.out;

import com.polaris.nucleo.domain.model.Recordatorio;
import com.polaris.nucleo.domain.model.TipoRecordatorio;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Recordatorio, nunca de RecordatorioEntity.
 *
 * <p>Se busca por usuario y tipo, no por id: hay uno por cada par.
 */
public interface RecordatorioRepositoryPort {

    Recordatorio save(Recordatorio recordatorio);

    Optional<Recordatorio> findByUsuarioIdAndTipo(Long usuarioId, TipoRecordatorio tipo);

    List<Recordatorio> findAllByUsuarioId(Long usuarioId);
}
