package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.ArticuloCompra;

import java.util.List;

public interface GetListaCompraInterface {
    List<ArticuloCompra> getListaCompra(Long usuarioId, Long planId);
}
