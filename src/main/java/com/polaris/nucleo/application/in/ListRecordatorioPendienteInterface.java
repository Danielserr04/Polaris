package com.polaris.nucleo.application.in;

import com.polaris.nucleo.domain.model.AvisoRecordatorio;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lo que la campana ensena: los recordatorios que ya tocan hoy y siguen sin
 * hacer, en el orden del enum.
 */
public interface ListRecordatorioPendienteInterface {
    List<AvisoRecordatorio> pendientes(Long usuarioId, LocalDateTime ahora);
}
