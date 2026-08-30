package com.polaris.auth.infrastructure.persistence.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * passwordActual es opcional a proposito: una cuenta que entro solo con Google
 * todavia no tiene contrasena, y ahi no hay actual que pedir. Si la cuenta si
 * tiene, el servicio la exige y responde 401 sin ella.
 */
public record CambiarPasswordRequestDto(
        String passwordActual,

        @NotBlank(message = "no puede estar vacia")
        @Size(min = 8, max = 100, message = "entre 8 y 100 caracteres")
        String passwordNueva
) {
}
