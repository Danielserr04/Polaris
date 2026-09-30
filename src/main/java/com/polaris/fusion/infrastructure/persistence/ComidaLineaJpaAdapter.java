package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.out.ComidaLineaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementa el puerto que usa AlimentoService para proteger el borrado.
 */
@Component
@RequiredArgsConstructor
public class ComidaLineaJpaAdapter implements ComidaLineaRepositoryPort {

    private final ComidaLineaRepository repository;

    @Override
    public boolean existsByAlimentoId(Long alimentoId) {
        return repository.existsByAlimento_Id(alimentoId);
    }
}
