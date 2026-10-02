package com.polaris.nucleo.application.in;

import com.polaris.nucleo.domain.model.Recordatorio;

import java.util.List;

/**
 * Siempre uno por tipo, en el orden del enum: los que el usuario no ha
 * guardado salen con sus valores por defecto.
 */
public interface ListRecordatorioInterface {
    List<Recordatorio> list(Long usuarioId);
}
