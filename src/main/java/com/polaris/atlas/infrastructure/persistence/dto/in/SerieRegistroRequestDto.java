package com.polaris.atlas.infrastructure.persistence.dto.in;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Una serie dentro de SesionRequestDto. numeroSerie desde 1 (que no se repita
 * dentro del ejercicio lo valida el servicio); reps de 1 a 999; pesoKg de 0 a
 * 1000 con 2 decimales como mucho (0 para el peso corporal); rpe opcional, de 1
 * a 10 (que sea en pasos de 0.5 lo valida el servicio). El servicio repite estos
 * rangos: aqui solo se corta pronto lo obvio.
 */
public record SerieRegistroRequestDto(
        @NotNull Long ejercicioId,
        @NotNull @Min(1) @Max(999) Integer numeroSerie,
        @NotNull @Min(1) @Max(999) Integer reps,
        @NotNull @DecimalMin("0.0") @DecimalMax("1000.0") @Digits(integer = 4, fraction = 2) BigDecimal pesoKg,
        @DecimalMin("1.0") @DecimalMax("10.0") @Digits(integer = 2, fraction = 1) BigDecimal rpe
) {
}
