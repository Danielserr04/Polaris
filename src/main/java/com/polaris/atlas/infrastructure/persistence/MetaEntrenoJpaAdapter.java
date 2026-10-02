package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.out.MetaEntrenoRepositoryPort;
import com.polaris.atlas.domain.model.MetaEntreno;
import com.polaris.atlas.domain.model.MetaEntrenoFilter;
import com.polaris.atlas.infrastructure.persistence.mapper.MetaEntrenoEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * El unico punto de la entidad donde conviven modelo y Entity.
 */
@Component
@RequiredArgsConstructor
public class MetaEntrenoJpaAdapter implements MetaEntrenoRepositoryPort {

    private final MetaEntrenoRepository repository;
    private final MetaEntrenoEntityMapper mapper;

    @Override
    public MetaEntreno save(MetaEntreno meta) {
        return mapper.toDomain(repository.save(mapper.toEntity(meta)));
    }

    @Override
    public Optional<MetaEntreno> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<MetaEntreno> findAll(Long usuarioId, MetaEntrenoFilter filter) {
        return mapper.toDomainList(repository.findAll(MetaEntrenoSpecifications.from(usuarioId, filter),
                Sort.by(Sort.Direction.DESC, "creadaEn", "id")));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
