package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.domain.model.FuenteAlimento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 *
 * <p>Sin usuario_id: catalogo compartido. Macros en BigDecimal / DECIMAL,
 * nunca double. El unique (fuente_externa, id_externo) vive en
 * V8__fusion_alimento.sql.
 */
@Entity
@Table(name = "alimento")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 100)
    private String marca;

    @Column(name = "kcal_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal kcal100g;

    @Column(name = "proteinas_100g", nullable = false, precision = 5, scale = 2)
    private BigDecimal proteinas100g;

    @Column(name = "carbohidratos_100g", nullable = false, precision = 5, scale = 2)
    private BigDecimal carbohidratos100g;

    @Column(name = "grasas_100g", nullable = false, precision = 5, scale = 2)
    private BigDecimal grasas100g;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuente_externa", nullable = false)
    private FuenteAlimento fuenteExterna;

    @Column(name = "id_externo")
    private String idExterno;
}
