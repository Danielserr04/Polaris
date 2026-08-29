package com.polaris.auth.infrastructure.persistence.dto.in;

import jakarta.validation.constraints.NotBlank;

/**
 * Login por username o email, indistinto.
 */
public record LoginRequestDto(
        @NotBlank String usernameOEmail,
        @NotBlank String password
) {
}
