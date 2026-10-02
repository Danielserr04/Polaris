package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.AportacionMeta;

import java.util.List;

public interface ListAportacionMetaInterface {
    List<AportacionMeta> listAportaciones(Long usuarioId, Long metaId);
}
