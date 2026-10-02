package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.domain.model.DiaSemana;
import com.polaris.fusion.domain.model.MomentoComida;
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
 * Mapeo JPA de una linea de plan. Lleva usuario_id (regla dura 5). O alimento
 * con cantidad_g, o receta con raciones: el resto va a null (lo garantiza el
 * servicio y un CHECK en V31). {@code alimento} y {@code receta} son EAGER
 * porque el detalle y la lista de la compra los leen fuera de la transaccion;
 * los ingredientes de la receta vienen con ella (EAGER + SUBSELECT en RecetaEntity).
 */
@Entity
@Table(name = "plan_comida_linea")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanComidaLineaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanComidaEntity plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DiaSemana diaSemana;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MomentoComida momento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "alimento_id")
    private AlimentoEntity alimento;

    @Column(name = "cantidad_g", precision = 7, scale = 2)
    private BigDecimal cantidadG;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receta_id")
    private RecetaEntity receta;

    @Column(precision = 5, scale = 2)
    private BigDecimal raciones;
}
