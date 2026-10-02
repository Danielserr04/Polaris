package com.polaris.nucleo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Modelo puro. Un dispositivo (navegador o app instalada) que ha pedido
 * recibir avisos: lo que devuelve PushManager.subscribe() en el navegador.
 *
 * <p>endpoint es la URL del servicio de push del fabricante (Google, Apple,
 * Mozilla) para ese dispositivo; p256dh y auth son sus claves para cifrar el
 * mensaje, en base64url. Ver docs/decisiones/044-recordatorios.md.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuscripcionPush {

    private Long id;
    private Long usuarioId;
    private String endpoint;
    private String p256dh;
    private String auth;
    private LocalDateTime creadaEn;
}
