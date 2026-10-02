package com.polaris.nucleo.infrastructure.persistence.dto.out;

import com.polaris.nucleo.domain.model.TipoRecordatorio;

/**
 * Un recordatorio de la campana: ya escrito y con la ruta a la que lleva.
 */
public record RecordatorioPendienteDto(
        TipoRecordatorio tipo,
        String titulo,
        String texto,
        String enlace
) {
}
