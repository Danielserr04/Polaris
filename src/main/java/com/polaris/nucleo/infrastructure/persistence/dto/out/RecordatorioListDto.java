package com.polaris.nucleo.infrastructure.persistence.dto.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.polaris.nucleo.domain.model.TipoRecordatorio;

import java.time.LocalTime;
import java.util.List;

/**
 * Una fila del listado. Es igual que la ficha: un recordatorio son cuatro
 * datos y el frontend los pinta todos.
 */
public record RecordatorioListDto(
        TipoRecordatorio tipo,
        boolean activo,
        @JsonFormat(pattern = "HH:mm") LocalTime hora,
        List<Integer> dias
) {
}
