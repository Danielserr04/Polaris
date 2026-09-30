package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en EjercicioEntity.
 *
 * <p>{@code usuarioId} nulo significa ejercicio del catalogo compartido, de solo
 * lectura; con valor, ejercicio propio de ese usuario. Ver
 * docs/decisiones/023-ejercicio-catalogo-y-propios.md.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ejercicio {

    private Long id;
    /** Nulo en el catalogo compartido. */
    private Long usuarioId;
    private String nombre;
    private String grupoMuscular;
    /** Opcional: un ejercicio con el peso corporal no lleva equipamiento. */
    private String equipamiento;

    /** Derivado de usuarioId: no se guarda aparte para que no pueda contradecirlo. */
    public boolean isPropio() {
        return usuarioId != null;
    }

    public boolean perteneceA(Long otroUsuarioId) {
        return usuarioId != null && usuarioId.equals(otroUsuarioId);
    }
}
