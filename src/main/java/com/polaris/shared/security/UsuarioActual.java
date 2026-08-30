package com.polaris.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * El {@code usuarioId} accesible desde cualquier servicio, que es el entregable
 * de B1 en docs/roadmap.md.
 *
 * <p>Vive en shared/ y no en auth/ a proposito: lo van a consumir nucleo, odisea,
 * kuiper, fusion y atlas para filtrar sus datos, y ninguno de ellos puede importar
 * una clase del modulo auth. Devuelve un Long, no un Usuario, asi que shared/
 * sigue sin conocer a ningun modulo.
 */
@Component
public class UsuarioActual {

    /**
     * @return id del usuario autenticado
     * @throws IllegalStateException si no hay nadie autenticado, que en un endpoint
     *         protegido significa que el filtro de seguridad esta mal configurado
     */
    public Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof Long usuarioId)) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto de seguridad");
        }

        return usuarioId;
    }
}
