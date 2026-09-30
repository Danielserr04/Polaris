package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.application.out.PerfilRepositoryPort;
import com.polaris.nucleo.domain.model.Perfil;
import com.polaris.nucleo.domain.model.PerfilFilter;
import com.polaris.nucleo.infrastructure.persistence.mapper.PerfilEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * El unico punto del modulo donde conviven modelo y Entity.
 */
@Component
@RequiredArgsConstructor
public class PerfilJpaAdapter implements PerfilRepositoryPort {

    private final PerfilRepository repository;
    private final PerfilEntityMapper mapper;

    @Override
    public Perfil save(Perfil perfil) {
        return mapper.toDomain(repository.save(mapper.toEntity(perfil)));
    }

    /** findOne es seguro: el unique de usuario_id garantiza como mucho una fila. */
    @Override
    public Optional<Perfil> findByUsuarioId(Long usuarioId) {
        return repository.findOne(PerfilSpecifications.from(usuarioId, new PerfilFilter()))
                .map(mapper::toDomain);
    }
}
