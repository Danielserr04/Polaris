package com.polaris.fusion.infrastructure.persistence.dto.out;

import com.polaris.fusion.domain.model.NivelActividad;
import com.polaris.fusion.domain.model.Sexo;
import com.polaris.fusion.domain.model.TipoObjetivo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * El objetivo propuesto y los datos con los que se ha calculado. No se guarda.
 */
public record CalculoObjetivoDto(
        TipoObjetivo tipo,
        NivelActividad nivelActividad,
        Sexo sexo,
        Integer edad,
        Integer alturaCm,
        BigDecimal pesoKg,
        @Schema(description = "Dia del peso usado; se omite si el peso llego en la peticion")
        LocalDate pesoFecha,
        @Schema(description = "Gasto basal en kcal (Mifflin-St Jeor)")
        Integer tmb,
        @Schema(description = "Gasto total diario en kcal: basal por el factor de actividad")
        Integer gastoTotal,
        Integer kcalDiarias,
        Integer proteinasObj,
        Integer carbosObj,
        Integer grasasObj
) {
}
