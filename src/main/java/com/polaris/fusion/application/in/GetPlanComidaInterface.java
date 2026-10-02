package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.PlanComida;

public interface GetPlanComidaInterface {
    PlanComida get(Long usuarioId, Long id);
}
