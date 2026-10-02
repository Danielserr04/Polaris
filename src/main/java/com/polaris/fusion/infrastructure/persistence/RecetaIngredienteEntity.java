package com.polaris.fusion.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Mapeo JPA de un ingrediente. Lleva usuario_id (regla dura 5 de CLAUDE.md).
 * Igual que ComidaLineaEntity: {@code alimento} EAGER, {@code receta} LAZY
 * (solo es la parte propietaria) y sin toString/equals de Lombok.
 */
@Entity
@Table(name = "receta_ingrediente")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecetaIngredienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    private RecetaEntity receta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "alimento_id", nullable = false)
    private AlimentoEntity alimento;

    @Column(name = "cantidad_g", nullable = false, precision = 7, scale = 2)
    private BigDecimal cantidadG;
}
