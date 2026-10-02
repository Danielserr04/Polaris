package com.polaris.nucleo.application.in;

import com.polaris.nucleo.domain.model.Recordatorio;
import com.polaris.nucleo.domain.model.TipoRecordatorio;

/**
 * Nunca falla por no existir: si el usuario aun no lo ha tocado, devuelve el
 * de por defecto sin guardarlo. Ver docs/decisiones/045-recordatorios.md.
 */
public interface GetRecordatorioInterface {
    Recordatorio get(Long usuarioId, TipoRecordatorio tipo);
}
