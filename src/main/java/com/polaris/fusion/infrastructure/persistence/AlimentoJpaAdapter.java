package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.out.AlimentoRepositoryPort;
import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.AlimentoFilter;
import com.polaris.fusion.domain.model.FuenteAlimento;
import com.polaris.fusion.infrastructure.persistence.mapper.AlimentoEntityMapper;
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
public class AlimentoJpaAdapter implements AlimentoRepositoryPort {

    private final AlimentoRepository repository;
    private final AlimentoEntityMapper mapper;

    @Override
    public Alimento save(Alimento alimento) {
        return mapper.toDomain(repository.save(mapper.toEntity(alimento)));
    }

    @Override
    public Optional<Alimento> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Alimento> findAll(AlimentoFilter filter) {
        Specification<AlimentoEntity> spec = AlimentoSpecifications.from(filter);
        return mapper.toDomainList(repository.findAll(spec, Sort.by("nombre")));
    }

    @Override
    public Optional<Alimento> findByFuenteExternaAndIdExterno(FuenteAlimento fuente, String idExterno) {
        return repository.findByFuenteExternaAndIdExterno(fuente, idExterno).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
