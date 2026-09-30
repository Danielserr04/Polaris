package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.AlimentoFilter;

import java.util.List;

public interface ListAlimentoInterface {
    List<Alimento> list(AlimentoFilter filter);
}
