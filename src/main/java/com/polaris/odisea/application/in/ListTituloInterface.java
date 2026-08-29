package com.polaris.odisea.application.in;

import com.polaris.odisea.domain.model.Titulo;
import com.polaris.odisea.domain.model.TituloFilter;

import java.util.List;

public interface ListTituloInterface {
    List<Titulo> list(TituloFilter filter);
}
