package com.polaris.auth.infrastructure.persistence.dto.out;

import java.time.Instant;

/**
 * La ficha del usuario autenticado. El Controller solo habla DTOs.
 *
 * <p>Los tres ultimos campos son lo que la pantalla de perfil necesita para
 * saber que ensenar: si pedir la contrasena actual o dejar poner una nueva, si
 * ofrecer desvincular Google, y si avisar de que el email esta sin verificar.
 *
 * <p>Son <b>booleanos</b> a proposito. Ni el googleId ni el hash de la
 * contrasena salen nunca de la aplicacion: la pantalla no necesita su valor,
 * solo saber si existen.
 */
public record UsuarioFormDto(
        Long id,
        String username,
        String email,
        String nombre,
        String avatarUrl,
        Instant creadoEn,
        boolean emailVerificado,
        boolean tieneGoogle,
        boolean tienePassword
) {
}
