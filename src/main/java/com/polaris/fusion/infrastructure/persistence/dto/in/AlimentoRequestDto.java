package com.polaris.fusion.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Lo que llega en un POST o PUT: macros por 100 g. Sin fuente ni idExterno: un
 * alimento hecho a mano es siempre MANUAL. Los rangos son fisicos (100 g de un
 * alimento no pueden tener mas de 100 g de un macro ni mas de 900 kcal, que es
 * grasa pura) y los digitos, los de las columnas DECIMAL, para que un valor
 * que no cabe sea un 400 y no un error de MySQL.
 */
public record AlimentoRequestDto(
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 100) String marca,
        @Schema(description = "kcal por cada 100 g, de 0 a 900", example = "130.00")
        @NotNull @DecimalMin("0.0") @DecimalMax("900.0") @Digits(integer = 3, fraction = 2) BigDecimal kcal100g,
        @Schema(description = "Gramos de proteina por cada 100 g, de 0 a 100")
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") @Digits(integer = 3, fraction = 2) BigDecimal proteinas100g,
        @Schema(description = "Gramos de carbohidratos por cada 100 g, de 0 a 100")
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") @Digits(integer = 3, fraction = 2) BigDecimal carbohidratos100g,
        @Schema(description = "Gramos de grasa por cada 100 g, de 0 a 100")
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") @Digits(integer = 3, fraction = 2) BigDecimal grasas100g
) {
}
