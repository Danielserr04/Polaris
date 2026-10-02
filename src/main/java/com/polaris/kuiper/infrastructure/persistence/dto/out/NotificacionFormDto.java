package com.polaris.kuiper.infrastructure.persistence.dto.out;

import com.polaris.kuiper.domain.model.TipoNotificacion;

import java.time.LocalDateTime;

/**
 * La ficha completa que devuelve el detalle y el PUT. Lleva la clave por si
 * el frontend quiere agrupar avisos del mismo hecho.
 */
public record NotificacionFormDto(
        Long id,
        TipoNotificacion tipo,
        String clave,
        String titulo,
        String texto,
        String enlace,
        boolean leida,
        LocalDateTime creadaEn
) {
}
