package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en ObjetivoNutricionalEntity.
 *
 * <p>Una fila por cambio de objetivo, con su vigente_desde. Los macros son
 * gramos objetivo al dia. No se exige que las kcal cuadren con los macros.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjetivoNutricional {

    private Long id;
    private Long usuarioId;
    private Integer kcalDiarias;
    private Integer proteinasObj;
    private Integer carbosObj;
    private Integer grasasObj;
    private LocalDate vigenteDesde;
}
