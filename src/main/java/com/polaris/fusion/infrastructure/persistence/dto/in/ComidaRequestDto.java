package com.polaris.fusion.infrastructure.persistence.dto.in;

import com.polaris.fusion.domain.model.MomentoComida;
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
        @NotNull @PastOrPresent LocalDate fecha,
        @NotNull MomentoComida momento,
        @NotNull @Size(min = 1, max = 50) List<@NotNull @Valid ComidaLineaRequestDto> lineas
) {
}
