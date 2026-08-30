package com.polaris.auth.application.out;

/**
 * Lo que el dominio necesita para tratar contrasenas, sin saber que existe
 * BCrypt. La regla 1 de CLAUDE.md prohibe traer Spring Security a domain/.
 */
public interface PasswordHasherPort {

    String hash(String contrasenaEnClaro);

    boolean coincide(String contrasenaEnClaro, String hash);
}
