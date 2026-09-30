package com.polaris.nucleo.application.in;

import com.polaris.nucleo.domain.model.RegistroPeso;

public interface GetRegistroPesoInterface {
    RegistroPeso get(Long usuarioId, Long id);
}
