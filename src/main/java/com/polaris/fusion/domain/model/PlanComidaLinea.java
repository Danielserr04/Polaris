package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Una linea de un PlanComida: un dia, un momento y O BIEN un alimento con sus
 * gramos O BIEN una receta con sus raciones (nunca las dos cosas; lo valida el
 * servicio). Modelo puro. {@code alimento} y {@code receta} solo llegan
 * cargados en lecturas, como en ComidaLinea.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanComidaLinea {

    private Long id;
    private Long usuarioId;
    private DiaSemana diaSemana;
    private MomentoComida momento;
    private Long alimentoId;
    private Alimento alimento;
    private BigDecimal cantidadG;
    private Long recetaId;
    private Receta receta;
    private BigDecimal raciones;

    public boolean esReceta() {
        return recetaId != null;
    }

    /**
     * Alimento: sus macros para esos gramos. Receta: los totales de la receta
     * escalados a raciones / raciones de la receta.
     */
    public Macros getMacros() {
        if (esReceta()) {
            if (receta == null) {
                throw new IllegalStateException("No se pueden calcular los macros sin la receta cargada");
            }
            return receta.getTotales().escalar(raciones, BigDecimal.valueOf(receta.getRaciones()));
        }
        if (alimento == null) {
            throw new IllegalStateException("No se pueden calcular los macros sin el alimento cargado");
        }
        return Macros.de(alimento, cantidadG);
    }
}
