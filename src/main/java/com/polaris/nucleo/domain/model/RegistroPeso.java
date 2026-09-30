package com.polaris.nucleo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en RegistroPesoEntity.
 *
 * <p>Nucleo guarda el numero y la fecha, no interpreta: ni IMC, ni tendencias,
 * ni objetivos. Cada modulo calcula lo que necesita. Ver docs/modulos/nucleo.md.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroPeso {

    private Long id;
    private Long usuarioId;
    private LocalDate fecha;
    private BigDecimal pesoKg;
    /** Opcional. */
    private BigDecimal grasaPct;
    private String notas;
}
