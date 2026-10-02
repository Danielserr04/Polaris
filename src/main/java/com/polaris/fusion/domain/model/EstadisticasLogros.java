package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/** Lo que necesitan los logros de Fusion, sin orden garantizado en las listas. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasLogros {

    /** La fecha de cada comida: dos comidas el mismo dia son dos fechas iguales. */
    private List<LocalDate> fechasComida;
    private long numeroRecetas;
    private long numeroPlanes;
    /** El primer dia con objetivo nutricional; nulo si nunca ha tenido. */
    private LocalDate primerObjetivo;
}
