package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.out.SerieRegistroRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementa el puerto que usa EjercicioService para proteger el borrado.
 */
@Component
@RequiredArgsConstructor
public class SerieRegistroJpaAdapter implements SerieRegistroRepositoryPort {

    private final SerieRegistroRepository repository;

    @Override
    public boolean existsByEjercicioId(Long ejercicioId) {
        return repository.existsByEjercicio_Id(ejercicioId);
    }
}
