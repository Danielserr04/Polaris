package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.out.RecetaIngredienteRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementa el puerto que usa AlimentoService para proteger el borrado.
 */
@Component
@RequiredArgsConstructor
public class RecetaIngredienteJpaAdapter implements RecetaIngredienteRepositoryPort {

    private final RecetaIngredienteRepository repository;

    @Override
    public boolean existsByAlimentoId(Long alimentoId) {
        return repository.existsByAlimento_Id(alimentoId);
    }
}
