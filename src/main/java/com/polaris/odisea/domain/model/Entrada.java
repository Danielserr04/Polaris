package com.polaris.odisea.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en EntradaEntity.
 *
 * <p>{@code tituloId} es el FK y siempre esta presente. {@code titulo} es la
 * ficha completa, enriquecida solo en lecturas (EntradaJpaAdapter la rellena
 * al mapear desde la Entity); al crear o actualizar llega nula, porque el
 * cliente solo manda el id del titulo, no su ficha entera.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entrada {

    private Long id;
    private Long usuarioId;
    private Long tituloId;
    private Titulo titulo;
    private EstadoEntrada estado;
    private Integer valoracion;
    private String notas;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean favorito;
    /** Episodio, pagina u horas, segun el tipo del titulo. */
    private Integer progreso;
}
