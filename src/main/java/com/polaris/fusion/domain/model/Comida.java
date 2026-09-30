package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Una ingesta con sus lineas: un solo agregado. Modelo puro, sin anotaciones de
 * persistencia (el mapeo vive en ComidaEntity).
 *
 * <p>Los totales no se guardan: {@link #getTotales()} suma los macros de las
 * lineas, que a su vez se calculan al vuelo.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comida {

    private Long id;
    private Long usuarioId;
    private LocalDate fecha;
    private MomentoComida momento;
    @Builder.Default
    private List<ComidaLinea> lineas = new ArrayList<>();

    public Macros getTotales() {
        return lineas.stream()
                .map(ComidaLinea::getMacros)
                .reduce(Macros.CERO, Macros::plus);
    }
}
