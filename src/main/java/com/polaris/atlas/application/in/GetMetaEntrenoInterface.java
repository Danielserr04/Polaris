package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.MetaEntreno;

public interface GetMetaEntrenoInterface {
    MetaEntreno get(Long usuarioId, Long id);
}
