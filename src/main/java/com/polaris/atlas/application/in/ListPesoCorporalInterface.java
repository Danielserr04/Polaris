package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.PesoCorporal;
import com.polaris.atlas.domain.model.PesoCorporalFilter;

import java.util.List;

public interface ListPesoCorporalInterface {
    List<PesoCorporal> list(Long usuarioId, PesoCorporalFilter filter);
}
