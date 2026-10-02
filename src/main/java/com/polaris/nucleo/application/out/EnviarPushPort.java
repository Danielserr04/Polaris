package com.polaris.nucleo.application.out;

import com.polaris.nucleo.domain.model.AvisoRecordatorio;

/**
 * Manda un aviso a todos los dispositivos del usuario que lo han pedido
 * (Web Push). Devuelve a cuantos ha llegado. Nunca lanza: un movil que no
 * contesta no debe parar el resto.
 */
public interface EnviarPushPort {

    int enviar(Long usuarioId, AvisoRecordatorio aviso);
}
