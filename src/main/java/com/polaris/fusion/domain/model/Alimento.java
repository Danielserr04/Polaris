package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en AlimentoEntity.
 *
 * <p>Catalogo compartido, como Titulo en Odisea: no lleva usuarioId. Los
 * macros son POR 100 g y nunca se guarda un total: se calcula al vuelo con la
 * cantidad de cada linea, asi que corregir un alimento corrige el historico.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alimento {

    private Long id;
    private String nombre;
    private String marca;
    private BigDecimal kcal100g;
    private BigDecimal proteinas100g;
    private BigDecimal carbohidratos100g;
    private BigDecimal grasas100g;
    private FuenteAlimento fuenteExterna;
    private String idExterno;
}
