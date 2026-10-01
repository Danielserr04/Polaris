package com.polaris.odisea.infrastructure.persistence.dto.in;

import com.polaris.odisea.domain.model.EstadoEntrada;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Lo que llega en un POST o PUT. Sin usuarioId: lo pone el servicio a partir
 * del JWT, nunca del body.
 */
public record EntradaRequestDto(
        @Schema(description = "Id de un titulo del catalogo de Polaris")
        @NotNull Long tituloId,
        @NotNull EstadoEntrada estado,
        @Schema(description = "Nota personal de 0 a 10; opcional")
        @Min(0) @Max(10) Integer valoracion,
        String notas,
        @Schema(description = "Dia en que empezaste (yyyy-MM-dd); opcional", example = "2026-09-01")
        LocalDate fechaInicio,
        @Schema(description = "Dia en que terminaste (yyyy-MM-dd); opcional", example = "2026-09-20")
        LocalDate fechaFin,
        boolean favorito,
        @Schema(description = "Numero de episodio por el que vas, en series; opcional")
        Integer progreso
) {
}
