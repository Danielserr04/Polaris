package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Una observacion generada sobre los movimientos del mes. {@code titulo} y
 * {@code texto} ya van redactados para el usuario.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Insight {

    private TipoInsight tipo;
    private SeveridadInsight severidad;
    private String titulo;
    private String texto;
}
