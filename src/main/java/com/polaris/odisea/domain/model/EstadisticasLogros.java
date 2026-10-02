package com.polaris.odisea.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/** Lo que necesitan los logros de Odisea. Las fechas de fin pueden ser nulas. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasLogros {

    /** Una por entrada terminada. */
    private List<Terminado> terminados;
    /** La fecha de fin de cada entrada valorada, o nula si no la tiene. */
    private List<LocalDate> fechasValoradas;

    /** Una entrada terminada reducida a lo que miran los logros. */
    public record Terminado(TipoContenido tipo, LocalDate fechaFin) {
    }
}
