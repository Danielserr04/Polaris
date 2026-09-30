package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Lo que se calcula al vuelo a partir de las comidas del dia: nunca se guarda
 * un total. {@code objetivoVigenteDesde} es la vigente_desde del objetivo con
 * el que se compara, o null si ese dia no habia objetivo vigente.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDiario {

    private LocalDate fecha;
    private LocalDate objetivoVigenteDesde;
    private MacroResumen kcal;
    private MacroResumen proteinas;
    private MacroResumen carbohidratos;
    private MacroResumen grasas;
}
