package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Un logro con el progreso del usuario. No se guarda: el catalogo es fijo
 * (LogroService) y el progreso se calcula al pedirlo. Ver
 * docs/decisiones/043-logros-calculados-y-metas.md.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Logro {

    private String codigo;
    private String nombre;
    private String descripcion;
    /** Nombre de un icono del design system. */
    private String icono;
    private MetricaLogro metrica;
    private long objetivo;
    /** Lo que lleva el usuario, sin topar en el objetivo. */
    private long progreso;

    public boolean isConseguido() {
        return progreso >= objetivo;
    }
}
