package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.MetaAhorro;

public interface GetMetaAhorroInterface {
    MetaAhorro get(Long usuarioId, Long id);
}
