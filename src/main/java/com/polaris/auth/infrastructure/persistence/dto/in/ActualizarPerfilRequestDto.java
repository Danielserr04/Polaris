package com.polaris.auth.infrastructure.persistence.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Lo unico editable del perfil. El username no se toca: es la mitad de tus
 * credenciales. El email tiene su propio endpoint porque obliga a reverificar.
 */
public record ActualizarPerfilRequestDto(
        @NotBlank(message = "no puede estar vacio")
        @Size(max = 255, message = "maximo 255 caracteres")
        String nombre,

        @Size(max = 255, message = "maximo 255 caracteres")
        String avatarUrl
) {
}
