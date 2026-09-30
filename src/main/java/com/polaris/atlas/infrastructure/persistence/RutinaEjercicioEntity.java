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

/**
 * Mapeo JPA de una linea. Lleva usuario_id (regla dura 5 de CLAUDE.md).
 *
 * <p>{@code ejercicio} es EAGER: el detalle muestra nombre y grupo muscular
 * fuera de la transaccion del adapter. {@code rutina} es LAZY: solo es la parte
 * propietaria de la relacion y nadie la lee al mapear a dominio. Sin
 * toString/equals de Lombok, para no recorrer el ciclo rutina-linea.
 */
@Entity
@Table(name = "rutina_ejercicio")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutinaEjercicioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_id", nullable = false)
    private RutinaEntity rutina;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private EjercicioEntity ejercicio;

    @Column(nullable = false)
    private Integer orden;

    @Column(name = "series_objetivo", nullable = false)
    private Integer seriesObjetivo;

    @Column(name = "reps_objetivo", nullable = false, length = 20)
    private String repsObjetivo;
}
