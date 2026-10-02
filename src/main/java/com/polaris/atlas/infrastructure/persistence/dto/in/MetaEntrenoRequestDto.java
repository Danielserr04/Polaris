package com.polaris.atlas.infrastructure.persistence.dto.in;

import com.polaris.atlas.domain.model.TipoMetaEntreno;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lo que llega en un POST o PUT. Sin usuarioId ni punto de partida: los pone
 * el servicio. El limite de digitos es el de la columna, DECIMAL(6,2).
 */
public record MetaEntrenoRequestDto(
        @Schema(description = "Que mide la meta")
        @NotNull TipoMetaEntreno tipo,
        @Schema(description = "Obligatorio en MARCA_EJERCICIO; se ignora en el resto")
        Long ejercicioId,
        @Schema(description = "kg en PESO_CORPORAL y MARCA_EJERCICIO; sesiones (1 a 7) en SESIONES_SEMANA",
                example = "100")
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 4, fraction = 2)
        BigDecimal valorObjetivo,
        @Schema(description = "Plazo opcional; no puede ser pasado", example = "2026-12-31")
        LocalDate fechaLimite
) {
}
