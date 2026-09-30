package com.polaris.atlas.infrastructure.persistence.dto.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Lo que llega en un POST o PUT: la sesion con sus series anidadas. Sin
 * usuarioId: lo pone el servicio a partir del JWT, nunca del body. rutinaId es
 * opcional (un entreno libre es valido). De 1 a 200 series; el PUT reemplaza el
 * conjunto entero. La fecha no puede ser futura. notas es TEXT en la base: se
 * limita a 2000 por sensatez.
 */
public record SesionRequestDto(
        Long rutinaId,
        @NotNull @PastOrPresent LocalDate fecha,
        @Min(1) @Max(1440) Integer duracionMin,
        @Size(max = 2000) String notas,
        @NotNull @Size(min = 1, max = 200) List<@NotNull @Valid SerieRegistroRequestDto> series
) {
}
