package com.polaris.fusion.infrastructure.persistence.dto.out;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un peso, tanto en el listado como en la respuesta del POST. No hay FormDto y
 * ListDto separados: son cuatro campos, un registro por dia, y no hay detalle
 * que aligerar. Las notas van tambien en el listado: si no, quien reescribe el
 * dia desde Fusion las perderia sin haberlas visto. Sin id: el dia lo identifica.
 */
public record PesoCorporalDto(
        LocalDate fecha,
        BigDecimal pesoKg,
        BigDecimal grasaPct,
        String notas
) {
}
