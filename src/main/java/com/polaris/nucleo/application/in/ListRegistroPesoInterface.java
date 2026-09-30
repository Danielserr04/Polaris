package com.polaris.nucleo.application.in;

import com.polaris.nucleo.domain.model.RegistroPeso;
import com.polaris.nucleo.domain.model.RegistroPesoFilter;

import java.util.List;

public interface ListRegistroPesoInterface {
    List<RegistroPeso> list(Long usuarioId, RegistroPesoFilter filter);
}
