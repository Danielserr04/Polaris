package com.polaris.fusion.infrastructure.persistence.dto.in;

import com.polaris.fusion.domain.model.NivelActividad;
import com.polaris.fusion.domain.model.TipoObjetivo;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Query params del calculo. Solo {@code tipo} es obligatorio; los otros dos
 * sustituyen al dato del perfil o al ultimo peso.
 */
public record CalculoObjetivoFilterListDto(
        @Parameter(description = "DEFINICION (-500 kcal), MANTENIMIENTO o VOLUMEN (+300 kcal)", required = true)
        @NotNull TipoObjetivo tipo,
        @Parameter(description = "Opcional: SEDENTARIO, LIGERO, MODERADO, ALTO o MUY_ALTO. Por defecto, el del perfil")
        NivelActividad nivelActividad,
        @Parameter(description = "Opcional: peso en kg. Por defecto, el ultimo registrado", example = "78.5")
        @DecimalMin("20") @DecimalMax("400") @Digits(integer = 3, fraction = 2) BigDecimal pesoKg
) {
}
