package com.polaris.odisea.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Un resultado de busqueda en una fuente externa. No se persiste: es lo que se
 * ensena para elegir antes de importar.
 *
 * <p>Trae menos que un Titulo a proposito. La ficha completa se pide a la
 * fuente al importar, no en cada busqueda: son 20 llamadas de detalle que
 * nadie ha pedido todavia.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoCatalogo {

    private FuenteExterna fuenteExterna;
    private String idExterno;
    private TipoContenido tipo;
    private String titulo;
    private String tituloOriginal;
    private Integer anio;
    private String sinopsis;
    private String imagenUrl;
    /** Id del Titulo ya guardado, si esta ficha ya se importo. null si no. */
    private Long tituloId;
}
