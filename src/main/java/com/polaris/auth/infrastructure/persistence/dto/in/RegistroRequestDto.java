package com.polaris.auth.infrastructure.persistence.dto.in;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Lo que llega al registrarse. La contrasena viaja en claro solo hasta aqui:
 * UsuarioService la hashea antes de guardar nada.
 */
public record RegistroRequestDto(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password
) {
}
