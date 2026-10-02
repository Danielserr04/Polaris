package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.MetaAhorro;
import com.polaris.kuiper.domain.model.MetaAhorroFilter;

import java.util.List;

public interface ListMetaAhorroInterface {
    List<MetaAhorro> list(Long usuarioId, MetaAhorroFilter filter);
}
