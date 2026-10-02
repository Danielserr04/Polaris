package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.application.out.SuscripcionPushRepositoryPort;
import com.polaris.nucleo.domain.model.SuscripcionPush;
import com.polaris.nucleo.domain.model.SuscripcionPushFilter;
import com.polaris.nucleo.infrastructure.persistence.mapper.SuscripcionPushEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * El unico punto del modulo donde conviven modelo y Entity.
 */
@Component
@RequiredArgsConstructor
public class SuscripcionPushJpaAdapter implements SuscripcionPushRepositoryPort {

    private final SuscripcionPushRepository repository;
    private final SuscripcionPushEntityMapper mapper;

    @Override
    public SuscripcionPush save(SuscripcionPush suscripcion) {
        return mapper.toDomain(repository.save(mapper.toEntity(suscripcion)));
    }

    /** findOne es seguro: el endpoint es unico en la tabla. */
    @Override
    public Optional<SuscripcionPush> findByEndpoint(String endpoint) {
        return repository.findOne(SuscripcionPushSpecifications.porEndpoint(endpoint)).map(mapper::toDomain);
    }

    @Override
    public List<SuscripcionPush> findAllByUsuarioId(Long usuarioId) {
        return mapper.toDomainList(repository.findAll(
                SuscripcionPushSpecifications.from(usuarioId, new SuscripcionPushFilter())));
    }

    @Override
    public List<Long> findUsuarioIds() {
        return repository.findUsuarioIds();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
