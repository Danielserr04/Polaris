package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.domain.model.TipoMetaEntreno;
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
import java.time.LocalDate;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui. Solo la
 * definicion: lo calculado (valor actual, progreso) no tiene columna.
 */
@Entity
@Table(name = "meta_entreno")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaEntrenoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMetaEntreno tipo;

    @Column(name = "ejercicio_id")
    private Long ejercicioId;

    @Column(name = "valor_objetivo", nullable = false, precision = 6, scale = 2)
    private BigDecimal valorObjetivo;

    @Column(name = "valor_inicial", precision = 6, scale = 2)
    private BigDecimal valorInicial;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @Column(name = "creada_en", nullable = false)
    private LocalDate creadaEn;
}
