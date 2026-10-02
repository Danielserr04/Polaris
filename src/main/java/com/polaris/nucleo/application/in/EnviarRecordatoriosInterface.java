package com.polaris.nucleo.application.in;

import java.time.LocalDateTime;

/**
 * La pasada de RecordatorioJob: manda al movil los recordatorios pendientes
 * de los usuarios con algun dispositivo suscrito, uno por tipo y dia como
 * mucho. Devuelve cuantos avisos ha mandado.
 */
public interface EnviarRecordatoriosInterface {
    int enviar(LocalDateTime ahora);
}
