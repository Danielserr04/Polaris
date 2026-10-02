package com.polaris.kuiper.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Lo que llega en el PUT /{id}/leida. Las notificaciones no se crean ni se
 * editan por HTTP: lo unico que cambia el usuario es si esta leida. Sin
 * cuerpo, o sin leida, se marca como leida.
 */
public record NotificacionRequestDto(
        @Schema(description = "true para marcarla leida, false para volver a dejarla sin leer",
                defaultValue = "true")
        Boolean leida
) {
}
