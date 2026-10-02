package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.MetaAhorro;

public interface CreateMetaAhorroInterface {
    MetaAhorro create(Long usuarioId, MetaAhorro meta);
}
