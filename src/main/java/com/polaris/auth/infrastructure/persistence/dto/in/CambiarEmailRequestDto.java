package com.polaris.auth.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * password es opcional por lo mismo que en CambiarPasswordRequestDto: las
 * cuentas solo-Google no tienen.
 */
public record CambiarEmailRequestDto(
        @NotBlank(message = "no puede estar vacio")
        @Email(message = "no es un email valido")
        @Size(max = 255, message = "maximo 255 caracteres")
        String email,

        @Schema(description = "Obligatoria si la cuenta ya tiene contrasena; se omite en cuentas solo de Google")
        String password
) {
}
