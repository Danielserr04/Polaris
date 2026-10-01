package com.polaris.nucleo.infrastructure.persistence.dto.in;

import com.polaris.nucleo.domain.model.NivelActividad;
import com.polaris.nucleo.domain.model.Sexo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

/**
 * Lo que llega en el PUT. Todo opcional: se puede guardar un perfil a medias.
 * Sin usuarioId: lo pone el servicio a partir del JWT, nunca del body.
 */
public record PerfilRequestDto(
        @Schema(description = "Altura en centimetros, de 50 a 272", example = "178")
        @Min(50) @Max(272) Integer alturaCm,
        @Schema(description = "Fecha de nacimiento (yyyy-MM-dd); tiene que ser pasada", example = "1995-04-12")
        @Past LocalDate fechaNacimiento,
        Sexo sexo,
        NivelActividad nivelActividad
) {
}
