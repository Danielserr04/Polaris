package com.polaris.shared.logro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Un logro con el progreso del usuario. Lo usan todos los modulos: cada uno
 * tiene su catalogo y calcula sus hitos, y {@link CalculoLogros} pone el
 * progreso y la fecha. No se guarda. Ver
 * docs/decisiones/044-logros-globales-con-fecha-calculada.md.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Logro {

    private String codigo;
    private String nombre;
    private String descripcion;
    /** Nombre de un icono del design system. */
    private String icono;
    private NivelLogro nivel;
    /** Como se cuenta el progreso, en plural: "sesiones", "t", "dias". */
    private String unidad;
    private long objetivo;
    /** Lo que lleva el usuario, sin topar en el objetivo. */
    private long progreso;
    /** El dia en que se alcanzo el objetivo; nulo si no se ha conseguido o si el dato no tiene fecha. */
    private LocalDate fechaConseguido;

    public boolean isConseguido() {
        return progreso >= objetivo;
    }

    /** Una entrada del catalogo, todavia sin progreso. */
    public static Logro definicion(String codigo, String nombre, String descripcion, String icono,
                                   NivelLogro nivel, String unidad, long objetivo) {
        return Logro.builder().codigo(codigo).nombre(nombre).descripcion(descripcion).icono(icono)
                .nivel(nivel).unidad(unidad).objetivo(objetivo).build();
    }
}
