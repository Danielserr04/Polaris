package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.out.PlanComidaLineaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementa el puerto que usan AlimentoService y RecetaService para proteger el borrado.
 */
@Component
@RequiredArgsConstructor
public class PlanComidaLineaJpaAdapter implements PlanComidaLineaRepositoryPort {

    private final PlanComidaLineaRepository repository;

    @Override
    public boolean existsByAlimentoId(Long alimentoId) {
        return repository.existsByAlimento_Id(alimentoId);
    }

    @Override
    public boolean existsByRecetaId(Long recetaId) {
        return repository.existsByReceta_Id(recetaId);
    }
}
