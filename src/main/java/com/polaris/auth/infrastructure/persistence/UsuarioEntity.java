package com.polaris.auth.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 *
 * <p>Tabla en singular y snake_case, como dice docs/convenciones.md.
 * El esquema lo genera Hibernate con ddl-auto: update hasta cerrar B2.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nombre;

    /** Identidad estable de Google (el claim "sub"). El email puede cambiar; esto no. */
    @Column(name = "google_id", nullable = false, unique = true)
    private String googleId;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;
}
