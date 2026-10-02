package com.polaris.nucleo.application.out;

import com.polaris.nucleo.domain.model.AvisoRecordatorio;
import com.polaris.nucleo.domain.model.TipoRecordatorio;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Si un recordatorio sigue pendiente un dia. Nucleo no sabe nada de comidas,
 * gastos ni sesiones: lo implementa el modulo dueno del dato en su
 * infrastructure/nucleo (Fusion las comidas, Kuiper los gastos y los
 * presupuestos, Atlas el entreno), consumiendo sus propios casos de uso.
 * Ver docs/decisiones/045-recordatorios.md.
 */
public interface ComprobarRecordatorioPort {

    TipoRecordatorio tipo();

    /** El aviso si aun queda algo por hacer ese dia; vacio si ya esta hecho. */
    Optional<AvisoRecordatorio> pendiente(Long usuarioId, LocalDate fecha);
}
