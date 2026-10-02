package com.polaris.atlas.infrastructure.persistence.dto.out;

import com.polaris.atlas.domain.model.TipoMetaEntreno;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La version para el listado. Lleva lo calculado, que es lo que se pinta, y
 * deja fuera el punto de partida y la fecha de creacion.
 */
public record MetaEntrenoListDto(
        Long id,
        TipoMetaEntreno tipo,
        Long ejercicioId,
        String ejercicioNombre,
        BigDecimal valorObjetivo,
        BigDecimal valorActual,
        int progresoPct,
        boolean conseguida,
        LocalDate fechaLimite
) {
}
