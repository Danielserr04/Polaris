package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.MetaEntreno;
import com.polaris.atlas.domain.model.MetaEntrenoFilter;

import java.util.List;

public interface ListMetaEntrenoInterface {
    List<MetaEntreno> list(Long usuarioId, MetaEntrenoFilter filter);
}
