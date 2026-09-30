package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** El volumen de un ejercicio en una sesion: entrada de la segunda mitad de un record. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumenSesionEjercicio {

    private Long ejercicioId;
    private Long sesionId;
    private LocalDate fecha;
    private BigDecimal volumen;
}
