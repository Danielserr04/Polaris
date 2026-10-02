package com.polaris.nucleo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en
 * RecordatorioEntity.
 *
 * <p>La configuracion (si esta encendido, a que hora y que dias de la
 * semana) y dos marcas de dia: cuando se mando al movil y cuando se descarto. Hay uno por usuario y tipo. Ver docs/decisiones/045-recordatorios.md.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recordatorio {

    private Long id;
    private Long usuarioId;
    private TipoRecordatorio tipo;
    private boolean activo;
    private LocalTime hora;
    private Set<DayOfWeek> dias;
    /** Ultimo dia en que se mando al movil. Como mucho un push por tipo y dia. */
    private LocalDate avisadoEn;
    /** Ultimo dia en que el usuario lo dio por hecho desde la campana. */
    private LocalDate descartadoEn;

    /** Toca hoy: encendido, es uno de sus dias, ya es la hora y no lo ha descartado. */
    public boolean tocaEn(LocalDate fecha, LocalTime ahora) {
        return activo
                && dias != null && dias.contains(fecha.getDayOfWeek())
                && hora != null && !ahora.isBefore(hora)
                && !fecha.equals(descartadoEn);
    }
}
