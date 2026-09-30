package com.polaris.fusion.infrastructure.persistence.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Para importar solo hace falta senalar el producto: su codigo de barras en Open Food Facts. */
public record ImportarAlimentoRequestDto(
        @NotBlank(message = "es obligatorio")
        @Size(max = 20, message = "no puede superar los 20 caracteres")
        String idExterno
) {
}
