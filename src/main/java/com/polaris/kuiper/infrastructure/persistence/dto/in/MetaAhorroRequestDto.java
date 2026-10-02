package com.polaris.kuiper.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lo que llega en un POST o PUT. Sin usuarioId, creadaEn ni lo ahorrado: lo
 * ahorrado solo cambia con aportaciones. Los limites son los de la columna.
 */
public record MetaAhorroRequestDto(
        @Schema(description = "Unico entre tus metas", example = "Viaje a Japon")
        @NotBlank @Size(max = 100) String nombre,
        @Schema(description = "Cantidad a juntar, mayor que 0", example = "3000.00")
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 8, fraction = 2)
        BigDecimal importeObjetivo,
        @Schema(description = "Fecha para la que se quiere tener (yyyy-MM-dd); opcional", example = "2027-06-30")
        LocalDate fechaLimite,
        @Schema(description = "Color en hexadecimal #RRGGBB; opcional", example = "#5bb3a0")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "debe ser un color hexadecimal #RRGGBB") String color,
        @Schema(description = "Nombre del icono; opcional")
        @Size(max = 50) String icono
) {
}
