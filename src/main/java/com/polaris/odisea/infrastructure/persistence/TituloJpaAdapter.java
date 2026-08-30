package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.application.out.TituloRepositoryPort;
import com.polaris.odisea.domain.model.FuenteExterna;
import com.polaris.odisea.domain.model.Titulo;
import com.polaris.odisea.domain.model.TituloFilter;
import com.polaris.odisea.infrastructure.persistence.mapper.TituloEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * El unico punto del modulo donde conviven modelo y Entity.
 */
@Component
@RequiredArgsConstructor
public class TituloJpaAdapter implements TituloRepositoryPort {

    private final TituloRepository repository;
    private final TituloEntityMapper mapper;

    @Override
    public Titulo save(Titulo titulo) {
        return mapper.toDomain(repository.save(mapper.toEntity(titulo)));
    }

    @Override
    public Optional<Titulo> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Titulo> findAll(TituloFilter filter) {
        Specification<TituloEntity> spec = TituloSpecifications.from(filter);
        return mapper.toDomainList(repository.findAll(spec));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByIdExternoAndFuenteExterna(String idExterno, FuenteExterna fuente) {
        return repository.existsByIdExternoAndFuenteExterna(idExterno, fuente);
    }

    @Override
    public Optional<Titulo> findByIdExternoAndFuenteExterna(String idExterno, FuenteExterna fuente) {
        return repository.findByIdExternoAndFuenteExterna(idExterno, fuente).map(mapper::toDomain);
    }
}
