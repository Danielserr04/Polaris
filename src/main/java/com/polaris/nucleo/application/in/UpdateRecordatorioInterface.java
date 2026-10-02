package com.polaris.nucleo.application.in;

import com.polaris.nucleo.domain.model.Recordatorio;

/**
 * Crea el recordatorio si el usuario aun no lo tenia guardado. Uno por
 * usuario y tipo: no hay un "create" aparte, igual que el perfil.
 */
public interface UpdateRecordatorioInterface {
    Recordatorio update(Long usuarioId, Recordatorio recordatorio);
}
