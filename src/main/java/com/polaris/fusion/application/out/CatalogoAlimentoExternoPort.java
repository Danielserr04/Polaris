package com.polaris.fusion.application.out;

import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.FuenteAlimento;
import com.polaris.fusion.domain.model.ResultadoCatalogoAlimento;

import java.util.List;

/**
 * La fuente externa de alimentos (Open Food Facts, ver
 * docs/decisiones/018-alimentos-open-food-facts.md).
 *
 * <p>Contrato: nunca devuelve datos basura. Los resultados de busqueda sin
 * nombre o sin los cuatro macros validos se descartan, y obtener lanza
 * ValidationException si la ficha no se puede guardar tal cual.
 */
public interface CatalogoAlimentoExternoPort {

    FuenteAlimento fuente();

    List<ResultadoCatalogoAlimento> buscar(String texto);

    /** La ficha completa y valida, lista para guardar al importar. */
    Alimento obtener(String idExterno);
}
