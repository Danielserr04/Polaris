package com.polaris.nucleo.infrastructure.persistence.dto.out;

/**
 * La clave publica VAPID, en base64url, que el navegador necesita para
 * suscribirse (applicationServerKey).
 */
public record PushClaveDto(String clavePublica) {
}
