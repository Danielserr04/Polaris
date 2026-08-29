package com.polaris.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en UsuarioEntity.
 *
 * <p>Es la unica entidad de la aplicacion que no lleva {@code usuarioId}, porque
 * es el usuario. El resto de tablas de datos personales si lo llevan.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Long id;
    private String email;
    private String nombre;
    private String googleId;
    private String avatarUrl;
    private Instant creadoEn;
}
