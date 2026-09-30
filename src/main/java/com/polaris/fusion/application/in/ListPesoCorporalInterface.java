package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.PesoCorporal;
import com.polaris.fusion.domain.model.PesoCorporalFilter;

import java.util.List;

public interface ListPesoCorporalInterface {
    List<PesoCorporal> list(Long usuarioId, PesoCorporalFilter filter);
}
