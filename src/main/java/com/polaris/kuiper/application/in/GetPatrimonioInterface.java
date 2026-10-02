package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Patrimonio;

/**
 * La suma del saldo actual de todas las cuentas del usuario, archivadas
 * incluidas. Ver docs/decisiones/039-cuentas-y-transferencias.md.
 */
public interface GetPatrimonioInterface {
    Patrimonio get(Long usuarioId);
}
