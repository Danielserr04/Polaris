package com.polaris.nucleo.application.in;

import com.polaris.nucleo.domain.model.MedidaCorporal;
import com.polaris.nucleo.domain.model.MedidaCorporalFilter;

import java.util.List;

public interface ListMedidaCorporalInterface {
    List<MedidaCorporal> list(Long usuarioId, MedidaCorporalFilter filter);
}
