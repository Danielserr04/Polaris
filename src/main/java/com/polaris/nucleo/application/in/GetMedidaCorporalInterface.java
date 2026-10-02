package com.polaris.nucleo.application.in;

import com.polaris.nucleo.domain.model.MedidaCorporal;

public interface GetMedidaCorporalInterface {
    MedidaCorporal get(Long usuarioId, Long id);
}
