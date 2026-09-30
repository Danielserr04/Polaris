package com.polaris.kuiper.infrastructure.persistence.dto.in;

import com.polaris.kuiper.domain.model.TipoMovimiento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Lo que llega en un POST o PUT. Sin usuarioId: lo pone el servicio a partir
 * del JWT, nunca del body. Los limites de tamano son los de la columna.
 */
public record CategoriaRequestDto(
        @NotBlank @Size(max = 100) String nombre,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "debe ser un color hexadecimal #RRGGBB") String color,
        @Size(max = 50) String icono,
        @NotNull TipoMovimiento tipo
) {
}
