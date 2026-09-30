package com.polaris.nucleo.application.in;

import com.polaris.nucleo.domain.model.RegistroPeso;

public interface UpdateRegistroPesoInterface {
    RegistroPeso update(Long usuarioId, Long id, RegistroPeso registro);
}
