package com.polaris.kuiper.infrastructure.persistence.dto.in;

import com.polaris.kuiper.domain.model.TipoCuenta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Lo que llega en un POST o PUT. Sin usuarioId ni saldoActual: el primero lo
 * pone el servicio a partir del JWT y el segundo se calcula. Los limites son
 * los de las columnas: saldoInicial cabe en DECIMAL(12,2) y puede ser negativo.
 */
public record CuentaRequestDto(
        @NotBlank @Size(max = 100) String nombre,
        @NotNull TipoCuenta tipo,
        @Schema(description = "Saldo al dar de alta la cuenta; puede ser negativo (tarjeta de credito)",
                example = "1250.00")
        @NotNull @Digits(integer = 10, fraction = 2) BigDecimal saldoInicial,
        @Schema(description = "Color en hexadecimal #RRGGBB; opcional", example = "#2E86AB")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "debe ser un color hexadecimal #RRGGBB") String color,
        @Schema(description = "Nombre del icono; opcional")
        @Size(max = 50) String icono,
        @Schema(description = "Entidad bancaria; opcional", example = "ING")
        @Size(max = 100) String banco,
        @Schema(description = "true para archivarla: deja de ofrecerse, pero conserva historico y saldo")
        boolean archivada
) {
}
