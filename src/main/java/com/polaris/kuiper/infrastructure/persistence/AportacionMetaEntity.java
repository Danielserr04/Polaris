package com.polaris.kuiper.infrastructure.persistence;

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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Mapeo JPA. meta_id como columna simple, sin @ManyToOne: nunca hace falta
 * la meta desde la aportacion, y la FK (con ON DELETE CASCADE) esta en V18.
 */
@Entity
@Table(name = "aportacion_meta")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AportacionMetaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "meta_id", nullable = false)
    private Long metaId;

    @Column(nullable = false)
    private LocalDate fecha;

    /** Con signo: positivo aporta, negativo retira. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    @Column(length = 255)
    private String nota;
}
