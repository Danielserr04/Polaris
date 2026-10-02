package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.PlanComida;
import com.polaris.fusion.domain.model.PlanComidaFilter;

import java.util.List;

public interface ListPlanComidaInterface {
    List<PlanComida> list(Long usuarioId, PlanComidaFilter filter);
}
