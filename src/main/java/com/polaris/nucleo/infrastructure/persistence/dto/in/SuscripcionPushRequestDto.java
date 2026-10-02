package com.polaris.nucleo.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Lo que devuelve PushSubscription.toJSON() en el navegador, aplanado:
 * endpoint y las dos claves de keys.
 */
public record SuscripcionPushRequestDto(
        @Schema(description = "URL del servicio de push del dispositivo",
                example = "https://fcm.googleapis.com/fcm/send/abc123")
        @NotBlank @Size(max = 500) @Pattern(regexp = "^https://.+") String endpoint,
        @Schema(description = "Clave publica del dispositivo (keys.p256dh), base64url")
        @NotBlank @Size(max = 120) String p256dh,
        @Schema(description = "Secreto de autenticacion (keys.auth), base64url")
        @NotBlank @Size(max = 50) String auth
) {
}
