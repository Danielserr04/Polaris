package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Transferencia;
import com.polaris.kuiper.domain.model.TransferenciaFilter;

import java.util.List;

public interface ListTransferenciaInterface {
    List<Transferencia> list(Long usuarioId, TransferenciaFilter filter);
}
