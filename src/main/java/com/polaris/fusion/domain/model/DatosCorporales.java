package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Lo que Fusion necesita del perfil para calcular un objetivo. El dato vive en
 * Nucleo (tabla perfil); este es el modelo propio de Fusion, como
 * PesoCorporal. Cualquier campo puede faltar: el perfil se guarda a medias.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosCorporales {

    private Integer alturaCm;
    private LocalDate fechaNacimiento;
    private Sexo sexo;
    private NivelActividad nivelActividad;
}
