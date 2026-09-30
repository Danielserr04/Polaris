package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Las mejores marcas de un ejercicio. Un "record" en Atlas es (a) el mayor peso
 * levantado en una sola serie, con sus reps y su fecha, y (b) el mayor volumen
 * en una sola sesion, con su fecha. No se guarda: se calcula. Ver
 * docs/decisiones/026-progresion-y-records-por-volumen.md.
 *
 * <p>{@code volumenMaximoSesion} y su fecha son nulos si el volumen mas alto es
 * 0 (un ejercicio solo con peso corporal): un record de 0 no dice nada.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordEjercicio {

    private Long ejercicioId;
    private String ejercicioNombre;
    private String ejercicioGrupoMuscular;
    private BigDecimal pesoMaximo;
    /** Reps de la serie del peso maximo (las mas altas si hubo empate a ese peso). */
    private int repsPesoMaximo;
    /** La primera vez que se levanto ese peso con esas reps. */
    private LocalDate fechaPesoMaximo;
    private BigDecimal volumenMaximoSesion;
    /** La primera sesion con ese volumen. */
    private LocalDate fechaVolumenMaximo;
}
