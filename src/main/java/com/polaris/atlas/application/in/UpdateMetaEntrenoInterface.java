package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.MetaEntreno;

public interface UpdateMetaEntrenoInterface {
    MetaEntreno update(Long usuarioId, Long id, MetaEntreno meta);
}
