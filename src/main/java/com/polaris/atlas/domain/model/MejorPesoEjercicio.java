package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Una serie hecha al peso maximo de un ejercicio: ese peso, las reps y la
 * primera fecha en que se hizo con esas reps. Es la primera mitad de un record
 * (la de mas reps de entre las del peso maximo); ver {@link RecordEjercicio}.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MejorPesoEjercicio {

    private Long ejercicioId;
    private String ejercicioNombre;
    private String ejercicioGrupoMuscular;
    private BigDecimal pesoKg;
    private int reps;
    private LocalDate fecha;
}
