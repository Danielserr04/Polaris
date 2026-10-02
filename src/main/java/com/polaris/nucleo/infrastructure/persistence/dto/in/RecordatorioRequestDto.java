package com.polaris.nucleo.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

/**
 * Lo que llega en el PUT. Sin usuarioId (sale del JWT) ni tipo (va en la ruta).
 */
public record RecordatorioRequestDto(
        @Schema(description = "Si el recordatorio esta encendido", example = "true")
        @NotNull Boolean activo,
        @Schema(description = "Hora a la que avisa (HH:mm), en hora de Madrid", example = "21:00")
        @NotNull LocalTime hora,
        @Schema(description = "Dias de la semana en que avisa, de 1 (lunes) a 7 (domingo)", example = "[1, 3, 5]")
        @NotEmpty List<@NotNull @Min(1) @Max(7) Integer> dias
) {
}
