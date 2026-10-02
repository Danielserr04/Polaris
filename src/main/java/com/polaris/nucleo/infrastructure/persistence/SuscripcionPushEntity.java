package com.polaris.nucleo.infrastructure.persistence;

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

import java.time.LocalDateTime;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui. Ver
 * V36__nucleo_suscripcion_push.sql.
 */
@Entity
@Table(name = "suscripcion_push")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuscripcionPushEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 500)
    private String endpoint;

    @Column(nullable = false, length = 120)
    private String p256dh;

    @Column(nullable = false, length = 50)
    private String auth;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;
}
