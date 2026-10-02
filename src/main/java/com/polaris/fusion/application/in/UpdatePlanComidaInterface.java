package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.PlanComida;

public interface UpdatePlanComidaInterface {
    PlanComida update(Long usuarioId, Long id, PlanComida plan);
}
