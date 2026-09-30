package com.polaris.fusion.infrastructure.persistence;

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

import java.time.LocalDate;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 *
 * <p>El unique (usuario_id, vigente_desde) vive en V9__fusion_objetivo_nutricional.sql.
 */
@Entity
@Table(name = "objetivo_nutricional")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjetivoNutricionalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "kcal_diarias", nullable = false)
    private Integer kcalDiarias;

    @Column(name = "proteinas_obj", nullable = false)
    private Integer proteinasObj;

    @Column(name = "carbos_obj", nullable = false)
    private Integer carbosObj;

    @Column(name = "grasas_obj", nullable = false)
    private Integer grasasObj;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;
}
