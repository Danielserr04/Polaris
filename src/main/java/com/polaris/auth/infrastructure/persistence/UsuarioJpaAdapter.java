package com.polaris.auth.infrastructure.persistence;

import com.polaris.auth.application.out.UsuarioRepositoryPort;
import com.polaris.auth.domain.model.Usuario;
import com.polaris.auth.infrastructure.persistence.mapper.UsuarioEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * El unico punto del modulo donde conviven modelo y Entity.
 */
@Component
@RequiredArgsConstructor
public class UsuarioJpaAdapter implements UsuarioRepositoryPort {

    private final UsuarioRepository repository;
    private final UsuarioEntityMapper mapper;

    @Override
    public Usuario save(Usuario usuario) {
        return mapper.toDomain(repository.save(mapper.toEntity(usuario)));
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> findByGoogleId(String googleId) {
        return repository.findByGoogleId(googleId).map(mapper::toDomain);
    }
}
