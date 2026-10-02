package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.MetaEntreno;

public interface CreateMetaEntrenoInterface {
    MetaEntreno create(Long usuarioId, MetaEntreno meta);
}
