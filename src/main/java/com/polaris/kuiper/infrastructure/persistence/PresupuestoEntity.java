package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 *
 * <p>{@code @ManyToOne} EAGER a CategoriaEntity por la misma razon que en
 * MovimientoEntity: con open-in-view: false un LAZY leido fuera de la
 * transaccion del adapter reventaria, y en un *-a-uno es un solo JOIN.
 * El unique (usuario_id, categoria_id, periodo) vive en V7__kuiper_presupuesto.sql.
 */
@Entity
@Table(name = "presupuesto")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodoPresupuesto periodo;

    @Column(name = "importe_limite", nullable = false, precision = 10, scale = 2)
    private BigDecimal importeLimite;
}
