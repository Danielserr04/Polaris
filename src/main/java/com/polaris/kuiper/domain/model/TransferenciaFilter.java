package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Criterios de busqueda como datos, no como Specification. El rango de fechas
 * es inclusivo; {@code cuentaId} casa con el origen o con el destino. Todos
 * opcionales.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaFilter {

    private LocalDate desde;
    private LocalDate hasta;
    private Long cuentaId;
}
