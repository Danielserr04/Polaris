package com.polaris.nucleo.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lo que llega en un POST o PUT. Sin usuarioId: lo pone el servicio a partir
 * del JWT, nunca del body. Los limites de digitos y de tamano son los de la
 * columna (DECIMAL(4,1) y TEXT, que son 65535) para que un valor
 * que no cabe sea un 400 y no un error de MySQL.
 */
public record MedidaCorporalRequestDto(
        @Schema(description = "Dia de la medicion (yyyy-MM-dd); no puede ser futuro", example = "2026-09-30")
        @NotNull @PastOrPresent LocalDate fecha,
        @Schema(description = "Cuello en cm, mayor que 0, hasta 3 enteros y 1 decimal; opcional", example = "80.5")
        @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 3, fraction = 1) BigDecimal cuelloCm,
        @Schema(description = "Pecho en cm, mayor que 0, hasta 3 enteros y 1 decimal; opcional", example = "80.5")
        @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 3, fraction = 1) BigDecimal pechoCm,
        @Schema(description = "Cintura, a la altura del ombligo en cm, mayor que 0, hasta 3 enteros y 1 decimal; opcional", example = "80.5")
        @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 3, fraction = 1) BigDecimal cinturaCm,
        @Schema(description = "Cadera en cm, mayor que 0, hasta 3 enteros y 1 decimal; opcional", example = "80.5")
        @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 3, fraction = 1) BigDecimal caderaCm,
        @Schema(description = "Brazo izquierdo, contraido en cm, mayor que 0, hasta 3 enteros y 1 decimal; opcional", example = "80.5")
        @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 3, fraction = 1) BigDecimal brazoIzqCm,
        @Schema(description = "Brazo derecho, contraido en cm, mayor que 0, hasta 3 enteros y 1 decimal; opcional", example = "80.5")
        @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 3, fraction = 1) BigDecimal brazoDchoCm,
        @Schema(description = "Muslo izquierdo en cm, mayor que 0, hasta 3 enteros y 1 decimal; opcional", example = "80.5")
        @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 3, fraction = 1) BigDecimal musloIzqCm,
        @Schema(description = "Muslo derecho en cm, mayor que 0, hasta 3 enteros y 1 decimal; opcional", example = "80.5")
        @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 3, fraction = 1) BigDecimal musloDchoCm,
        @Size(max = 65535, message = "maximo 65535 caracteres") String notas
) {
}
