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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 *
 * <p>Medidas en BigDecimal / DECIMAL, nunca double. El unique
 * (usuario_id, fecha) vive en V23__nucleo_medida_corporal.sql.
 */
@Entity
@Table(name = "medida_corporal")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedidaCorporalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "cuello_cm", precision = 4, scale = 1)
    private BigDecimal cuelloCm;

    @Column(name = "pecho_cm", precision = 4, scale = 1)
    private BigDecimal pechoCm;

    @Column(name = "cintura_cm", precision = 4, scale = 1)
    private BigDecimal cinturaCm;

    @Column(name = "cadera_cm", precision = 4, scale = 1)
    private BigDecimal caderaCm;

    @Column(name = "brazo_izq_cm", precision = 4, scale = 1)
    private BigDecimal brazoIzqCm;

    @Column(name = "brazo_dcho_cm", precision = 4, scale = 1)
    private BigDecimal brazoDchoCm;

    @Column(name = "muslo_izq_cm", precision = 4, scale = 1)
    private BigDecimal musloIzqCm;

    @Column(name = "muslo_dcho_cm", precision = 4, scale = 1)
    private BigDecimal musloDchoCm;

    @Column(columnDefinition = "TEXT")
    private String notas;
}
