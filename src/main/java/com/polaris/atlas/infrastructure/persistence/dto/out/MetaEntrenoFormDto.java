package com.polaris.atlas.infrastructure.persistence.dto.out;

import com.polaris.atlas.domain.model.TipoMetaEntreno;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La ficha completa, con lo calculado al leer.
 */
public record MetaEntrenoFormDto(
        Long id,
        TipoMetaEntreno tipo,
        Long ejercicioId,
        String ejercicioNombre,
        BigDecimal valorObjetivo,
        @Schema(description = "Peso o record el dia de crearla; nulo en SESIONES_SEMANA o si no habia peso")
        BigDecimal valorInicial,
        @Schema(description = "Ultimo peso, record actual o sesiones de esta semana; nulo si no hay peso")
        BigDecimal valorActual,
        @Schema(description = "De 0 a 100")
        int progresoPct,
        boolean conseguida,
        LocalDate fechaLimite,
        LocalDate creadaEn
) {
}
