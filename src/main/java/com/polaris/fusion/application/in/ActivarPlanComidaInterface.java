package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.PlanComida;

public interface ActivarPlanComidaInterface {
    PlanComida activar(Long usuarioId, Long id);
}
