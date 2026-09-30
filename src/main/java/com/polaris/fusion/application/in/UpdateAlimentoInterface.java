package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.Alimento;

public interface UpdateAlimentoInterface {
    Alimento update(Long id, Alimento alimento);
}
