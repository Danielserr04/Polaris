package com.polaris.nucleo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en PerfilEntity.
 *
 * <p>Todo es opcional salvo usuarioId: se puede guardar un perfil a medias.
 * Nucleo guarda el dato crudo y no calcula nada con el (ni edad, ni TMB).
 * Ver docs/modulos/nucleo.md.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {

    private Long id;
    private Long usuarioId;
    private Integer alturaCm;
    private LocalDate fechaNacimiento;
    private Sexo sexo;
    private NivelActividad nivelActividad;
}
