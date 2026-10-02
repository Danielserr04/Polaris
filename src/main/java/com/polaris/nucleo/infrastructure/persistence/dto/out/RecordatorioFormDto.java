package com.polaris.nucleo.infrastructure.persistence.dto.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.polaris.nucleo.domain.model.TipoRecordatorio;

import java.time.LocalTime;
import java.util.List;

/**
 * La ficha que devuelve el GET de un tipo y el PUT. dias de 1 (lunes) a 7
 * (domingo), ordenados.
 */
public record RecordatorioFormDto(
        TipoRecordatorio tipo,
        boolean activo,
        @JsonFormat(pattern = "HH:mm") LocalTime hora,
        List<Integer> dias
) {
}
