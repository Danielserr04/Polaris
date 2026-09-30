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
 * Mapeo JPA de una linea. Lleva usuario_id (regla dura 5 de CLAUDE.md).
 *
 * <p>{@code alimento} es EAGER por lo mismo que en MovimientoEntity: el
 * detalle y el listado muestran nombre y macros del alimento fuera de la
 * transaccion del adapter. {@code comida} es LAZY: solo es la parte propietaria
 * de la relacion y nadie la lee al mapear a dominio. Sin toString/equals de
 * Lombok, para no recorrer el ciclo comida-linea.
 */
@Entity
@Table(name = "comida_linea")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComidaLineaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comida_id", nullable = false)
    private ComidaEntity comida;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "alimento_id", nullable = false)
    private AlimentoEntity alimento;

    @Column(name = "cantidad_g", nullable = false, precision = 7, scale = 2)
    private BigDecimal cantidadG;
}
