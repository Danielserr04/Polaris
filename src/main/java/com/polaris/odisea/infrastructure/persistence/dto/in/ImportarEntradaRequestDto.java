package com.polaris.odisea.infrastructure.persistence.dto.in;

import com.polaris.odisea.domain.model.TipoContenido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Para importar solo hace falta senalar la ficha de la fuente externa. El
 * usuarioId no viene aqui: lo pone el servicio desde el JWT.
 */
public record ImportarEntradaRequestDto(
        @NotBlank(message = "es obligatorio")
        @Schema(description = "Id de la ficha en la fuente externa, tal como lo devuelve la busqueda",
                example = "438631")
        String idExterno,

        @NotNull(message = "es obligatorio")
        TipoContenido tipo
) {
}
