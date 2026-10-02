package com.polaris.nucleo.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Que recuerda cada recordatorio y con que valores nace. Si ya has cumplido
 * lo del dia (comidas, gastos, sesion, presupuestos) lo dice el modulo dueno
 * del dato a traves de ComprobarRecordatorioPort. Ver
 * docs/decisiones/045-recordatorios.md.
 */
public enum TipoRecordatorio {

    /** Apuntar las comidas del dia en Fusion. */
    COMIDAS(true, LocalTime.of(21, 0), EnumSet.allOf(DayOfWeek.class)),
    /** Apuntar los gastos del dia en Kuiper. */
    GASTOS(true, LocalTime.of(22, 0), EnumSet.allOf(DayOfWeek.class)),
    /** Entrenar en Atlas. Apagado de serie: los dias de gym son de cada uno. */
    ENTRENO(false, LocalTime.of(18, 0),
            EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
    /** Algun presupuesto de Kuiper en aviso o pasado del limite este mes. */
    PRESUPUESTO(true, LocalTime.of(10, 0), EnumSet.allOf(DayOfWeek.class));

    private final boolean activoPorDefecto;
    private final LocalTime horaPorDefecto;
    private final Set<DayOfWeek> diasPorDefecto;

    TipoRecordatorio(boolean activoPorDefecto, LocalTime horaPorDefecto, Set<DayOfWeek> diasPorDefecto) {
        this.activoPorDefecto = activoPorDefecto;
        this.horaPorDefecto = horaPorDefecto;
        this.diasPorDefecto = diasPorDefecto;
    }

    /** El recordatorio que tiene un usuario que aun no lo ha tocado. Sin id: no esta guardado. */
    public Recordatorio porDefecto(Long usuarioId) {
        return Recordatorio.builder()
                .usuarioId(usuarioId)
                .tipo(this)
                .activo(activoPorDefecto)
                .hora(horaPorDefecto)
                .dias(EnumSet.copyOf(diasPorDefecto))
                .build();
    }
}
