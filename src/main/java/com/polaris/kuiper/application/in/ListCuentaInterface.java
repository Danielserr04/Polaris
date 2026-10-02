package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.domain.model.CuentaFilter;

import java.util.List;

public interface ListCuentaInterface {
    List<Cuenta> list(Long usuarioId, CuentaFilter filter);
}
