package com.polaris.nucleo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en MedidaCorporalEntity.
 *
 * <p>Perimetros en centimetros, todos opcionales, pero al menos uno por dia.
 * Como el peso, Nucleo guarda el numero y la fecha y no interpreta. Ver
 * docs/decisiones/041-medida-corporal-una-por-dia.md.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedidaCorporal {

    private Long id;
    private Long usuarioId;
    private LocalDate fecha;
    private BigDecimal cuelloCm;
    private BigDecimal pechoCm;
    private BigDecimal cinturaCm;
    private BigDecimal caderaCm;
    private BigDecimal brazoIzqCm;
    private BigDecimal brazoDchoCm;
    private BigDecimal musloIzqCm;
    private BigDecimal musloDchoCm;
    private String notas;

    /** Un registro sin ningun perimetro no dice nada: el servicio lo rechaza. */
    public boolean sinMedidas() {
        return Stream.of(cuelloCm, pechoCm, cinturaCm, caderaCm, brazoIzqCm, brazoDchoCm, musloIzqCm, musloDchoCm)
                .allMatch(Objects::isNull);
    }
}
