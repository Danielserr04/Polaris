package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    /** Ejercicios distintos de la sesion. */
    public int getNumeroEjercicios() {
        return series == null ? 0 : (int) series.stream().map(SerieRegistro::getEjercicioId)
                .filter(Objects::nonNull).distinct().count();
    }

    /**
     * Volumen total: suma de repeticiones por peso de todas las series, con 2 decimales
     * (HALF_UP). El peso corporal (0 kg) aporta 0, como en la progresion
     * (docs/decisiones/026-progresion-y-records-por-volumen.md). Nunca se guarda.
     */
    public BigDecimal getVolumen() {
        BigDecimal total = BigDecimal.ZERO;
        if (series != null) {
            for (SerieRegistro s : series) {
                if (s.getReps() != null && s.getPesoKg() != null) {
                    total = total.add(s.getPesoKg().multiply(BigDecimal.valueOf(s.getReps())));
                }
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
