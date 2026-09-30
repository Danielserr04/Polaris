package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Un entreno real con sus series: un solo agregado. Modelo puro, sin
 * anotaciones de persistencia (el mapeo vive en SesionEntity). Ver
 * docs/decisiones/025-sesion-agregado-con-series.md.
 *
 * <p>{@code rutinaId} es nulo en un entreno libre. {@code rutinaNombre} es la
 * ficha de la rutina, enriquecida solo en lecturas (SesionJpaAdapter); al
 * crear o actualizar llega nula.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sesion {

    private Long id;
    private Long usuarioId;
    /** Nulo en un entreno libre. */
    private Long rutinaId;
    private String rutinaNombre;
    private LocalDate fecha;
    /** Opcional. */
    private Integer duracionMin;
    /** Opcional. */
    private String notas;
    /** Agrupadas por ejercicio (en el orden en que aparecen) y por numeroSerie dentro de cada uno. */
    @Builder.Default
    private List<SerieRegistro> series = new ArrayList<>();

    public int getNumeroSeries() {
        return series == null ? 0 : series.size();
    }
}
