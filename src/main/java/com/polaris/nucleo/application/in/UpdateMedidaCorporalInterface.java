package com.polaris.nucleo.application.in;

import com.polaris.nucleo.domain.model.MedidaCorporal;

public interface UpdateMedidaCorporalInterface {
    MedidaCorporal update(Long usuarioId, Long id, MedidaCorporal registro);
}
