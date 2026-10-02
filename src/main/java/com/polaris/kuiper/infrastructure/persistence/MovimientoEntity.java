package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.TipoMovimiento;
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
import java.time.LocalDate;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 *
 * <p>{@code @ManyToOne} a CategoriaEntity, no un id suelto: es lo que permite
 * a MovimientoListDto mostrar nombre y color de la categoria sin una segunda
 * consulta. FetchType.EAGER explicito por lo mismo que en EntradaEntity: con
 * open-in-view: false, un LAZY leido fuera de la transaccion del adapter
 * reventaria, y en un *-a-uno EAGER es un solo JOIN.
 *
 * <p>Importe en BigDecimal / DECIMAL, nunca double.
 */
@Entity
@Table(name = "movimiento")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    private String concepto;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Column(nullable = false)
    private boolean recurrente;

    /** Opcional: nula es "sin cuenta". EAGER por lo mismo que la categoria. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cuenta_id")
    private CuentaEntity cuenta;
}
