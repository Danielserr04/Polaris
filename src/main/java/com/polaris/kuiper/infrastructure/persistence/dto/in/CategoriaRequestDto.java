package com.polaris.kuiper.infrastructure.persistence.dto.in;

import com.polaris.kuiper.domain.model.TipoMovimiento;
import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "Color en hexadecimal #RRGGBB; opcional", example = "#E4572E")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "debe ser un color hexadecimal #RRGGBB") String color,
        @Schema(description = "Nombre del icono; opcional")
        @Size(max = 50) String icono,
        @NotNull TipoMovimiento tipo
) {
}
