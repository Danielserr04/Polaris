package com.polaris.fusion.infrastructure.persistence.dto.in;

import com.polaris.fusion.domain.model.MomentoComida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Lo que llega en un POST o PUT: la comida con sus lineas anidadas. Sin
 * usuarioId: lo pone el servicio a partir del JWT, nunca del body. De 1 a 50
 * lineas; el PUT reemplaza el conjunto entero.
 */
public record ComidaRequestDto(
        @Schema(description = "Dia de la comida (yyyy-MM-dd); no puede ser futuro", example = "2026-09-30")
        @NotNull @PastOrPresent LocalDate fecha,
        @NotNull MomentoComida momento,
        @Schema(description = "De 1 a 50 lineas; en el PUT sustituyen a las anteriores")
        @NotNull @Size(min = 1, max = 50) List<@NotNull @Valid ComidaLineaRequestDto> lineas
) {
}
