package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.TipoCuenta;
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
 * El unique (usuario_id, nombre) vive en V20__kuiper_cuenta.sql.
 *
 * <p>Sin saldo actual: se calcula en CuentaService, nunca se guarda.
 * saldoInicial en BigDecimal / DECIMAL(12,2), con signo.
 */
@Entity
@Table(name = "cuenta")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCuenta tipo;

    @Column(name = "saldo_inicial", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoInicial;

    @Column(length = 7)
    private String color;

    @Column(length = 50)
    private String icono;

    @Column(length = 100)
    private String banco;

    @Column(nullable = false)
    private boolean archivada;
}
