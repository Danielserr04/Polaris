package com.polaris.kuiper.infrastructure.persistence.dto.out;

import com.polaris.kuiper.domain.model.TipoNotificacion;

import java.time.LocalDateTime;

/**
 * La version para el panel de la campana: sin la clave, que es interna.
 */
public record NotificacionListDto(
        Long id,
        TipoNotificacion tipo,
        String titulo,
        String texto,
        String enlace,
        boolean leida,
        LocalDateTime creadaEn
) {
}
