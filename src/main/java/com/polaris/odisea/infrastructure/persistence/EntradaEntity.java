package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.domain.model.EstadoEntrada;
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

import java.time.LocalDate;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 *
 * <p>{@code @ManyToOne} a TituloEntity, no un id suelto: es lo que permite
 * filtrar por tipo en TituloSpecifications sin una segunda consulta, y lo
 * que necesita EntradaListDto para mostrar titulo y caratula.
 *
 * <p>FetchType.EAGER explicito (aunque es el valor por defecto de @ManyToOne):
 * con open-in-view: false, un LAZY que se lee fuera de la transaccion de
 * EntradaJpaAdapter reventaria con LazyInitializationException. EAGER en un
 * *-a-uno es un solo JOIN, no el problema N+1 de una coleccion.
 */
@Entity
@Table(name = "entrada")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntradaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "titulo_id", nullable = false)
    private TituloEntity titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEntrada estado;

    private Integer valoracion;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(nullable = false)
    private boolean favorito;

    /** Episodio, pagina u horas, segun el tipo del titulo. */
    private Integer progreso;
}
