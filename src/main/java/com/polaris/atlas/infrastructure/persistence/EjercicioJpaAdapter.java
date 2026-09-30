package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.out.EjercicioRepositoryPort;
import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.EjercicioFilter;
import com.polaris.atlas.infrastructure.persistence.mapper.EjercicioEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * El unico punto del modulo donde conviven modelo y Entity.
 */
@Component
@RequiredArgsConstructor
public class EjercicioJpaAdapter implements EjercicioRepositoryPort {

    private final EjercicioRepository repository;
    private final EjercicioEntityMapper mapper;

    @Override
    public Ejercicio save(Ejercicio ejercicio) {
        return mapper.toDomain(repository.save(mapper.toEntity(ejercicio)));
    }

    @Override
    public Optional<Ejercicio> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Ejercicio> findAll(Long usuarioId, EjercicioFilter filter) {
        Specification<EjercicioEntity> spec = EjercicioSpecifications.from(usuarioId, filter);
        return mapper.toDomainList(repository.findAll(spec, Sort.by("nombre")));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<Ejercicio> findVisiblesByNombre(Long usuarioId, String nombre) {
        return mapper.toDomainList(repository.findVisiblesByNombre(usuarioId, nombre));
    }
}
