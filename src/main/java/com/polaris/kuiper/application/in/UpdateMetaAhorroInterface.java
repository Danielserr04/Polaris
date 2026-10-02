package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.MetaAhorro;

public interface UpdateMetaAhorroInterface {
    MetaAhorro update(Long usuarioId, Long id, MetaAhorro meta);
}
