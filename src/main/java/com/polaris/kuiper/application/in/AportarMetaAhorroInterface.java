package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.AportacionMeta;
import com.polaris.kuiper.domain.model.MetaAhorro;

/** Aporta (importe positivo) o retira (negativo). Devuelve la meta con el total ya actualizado. */
public interface AportarMetaAhorroInterface {
    MetaAhorro aportar(Long usuarioId, Long metaId, AportacionMeta aportacion);
}
