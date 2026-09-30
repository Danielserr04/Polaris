package com.polaris.nucleo.infrastructure.persistence.dto.out;

import com.polaris.nucleo.domain.model.NivelActividad;
import com.polaris.nucleo.domain.model.Sexo;

import java.time.LocalDate;

/**
 * La ficha completa que devuelve el GET y el PUT.
 */
public record PerfilFormDto(
        Long id,
        Integer alturaCm,
        LocalDate fechaNacimiento,
        Sexo sexo,
        NivelActividad nivelActividad
) {
}
