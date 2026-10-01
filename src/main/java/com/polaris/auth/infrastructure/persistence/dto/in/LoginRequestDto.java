package com.polaris.auth.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Login por username o email, indistinto.
 */
public record LoginRequestDto(
        @Schema(description = "Username o email, indistinto")
        @NotBlank String usernameOEmail,
        @NotBlank String password
) {
}
