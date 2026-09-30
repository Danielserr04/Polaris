package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.ResultadoCatalogoAlimento;

import java.util.List;

public interface BuscarCatalogoAlimentoInterface {
    List<ResultadoCatalogoAlimento> buscar(String texto);
}
