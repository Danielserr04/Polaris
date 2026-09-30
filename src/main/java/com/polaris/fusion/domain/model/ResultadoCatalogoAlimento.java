package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Un resultado de busqueda en la API externa de alimentos. No se persiste.
 *
 * <p>A diferencia de Odisea, aqui los macros ya vienen en la busqueda, asi que
 * se ensenan antes de importar. Un resultado solo existe si tiene nombre y los
 * cuatro valores validos: los incompletos los descarta el adaptador.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoCatalogoAlimento {

    private FuenteAlimento fuenteExterna;
    private String idExterno;
    private String nombre;
    private String marca;
    private BigDecimal kcal100g;
    private BigDecimal proteinas100g;
    private BigDecimal carbohidratos100g;
    private BigDecimal grasas100g;
    /** Id del Alimento ya guardado, si esta ficha ya se importo. null si no. */
    private Long alimentoId;
}
