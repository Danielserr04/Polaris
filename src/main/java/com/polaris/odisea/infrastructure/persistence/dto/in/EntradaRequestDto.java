package com.polaris.odisea.infrastructure.persistence.dto.in;

import com.polaris.odisea.domain.model.EstadoEntrada;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Lo que llega en un POST o PUT. Sin usuarioId: lo pone el servicio a partir
 * del JWT, nunca del body.
 */
public record EntradaRequestDto(
        @NotNull Long tituloId,
        @NotNull EstadoEntrada estado,
        @Min(0) @Max(10) Integer valoracion,
        String notas,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean favorito,
        Integer progreso
) {
}
