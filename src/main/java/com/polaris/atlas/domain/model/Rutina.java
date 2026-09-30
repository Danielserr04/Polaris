package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Una plantilla de entrenamiento con sus lineas: un solo agregado. Modelo puro,
 * sin anotaciones de persistencia (el mapeo vive en RutinaEntity). Ver
 * docs/decisiones/024-rutina-agregado-con-lineas.md.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rutina {

    private Long id;
    private Long usuarioId;
    private String nombre;
    /** Opcional. */
    private String descripcion;
    private boolean activa;
    /** Ordenadas por {@code orden}. */
    @Builder.Default
    private List<RutinaEjercicio> lineas = new ArrayList<>();

    public int getNumeroEjercicios() {
        return lineas == null ? 0 : lineas.size();
    }
}
