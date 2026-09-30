package com.polaris.atlas.infrastructure.persistence;

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
 * Mapeo JPA de una serie. Lleva usuario_id (regla dura 5 de CLAUDE.md). Peso y
 * RPE en BigDecimal / DECIMAL, nunca double.
 *
 * <p>{@code ejercicio} es EAGER: el detalle muestra nombre y grupo muscular
 * fuera de la transaccion del adapter. {@code sesion} es LAZY: solo es la parte
 * propietaria de la relacion y nadie la lee al mapear a dominio. Sin
 * toString/equals de Lombok, para no recorrer el ciclo sesion-serie.
 */
@Entity
@Table(name = "serie_registro")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerieRegistroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionEntity sesion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private EjercicioEntity ejercicio;

    @Column(name = "numero_serie", nullable = false)
    private Integer numeroSerie;

    @Column(nullable = false)
    private Integer reps;

    @Column(name = "peso_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal pesoKg;

    @Column(precision = 3, scale = 1)
    private BigDecimal rpe;
}
