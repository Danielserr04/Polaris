package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.PlanComida;

public interface CreatePlanComidaInterface {
    PlanComida create(Long usuarioId, PlanComida plan);
}
