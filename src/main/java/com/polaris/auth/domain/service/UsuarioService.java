package com.polaris.auth.domain.service;

import com.polaris.auth.application.in.GetOrCreateUsuarioInterface;
import com.polaris.auth.application.in.GetUsuarioInterface;
import com.polaris.auth.application.out.UsuarioRepositoryPort;
import com.polaris.auth.domain.model.PerfilGoogle;
import com.polaris.auth.domain.model.Usuario;
import com.polaris.auth.domain.model.UsuarioNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Aqui vive la logica de auth. No sabe que existe HTTP, OAuth2 ni JPA:
 * solo puertos y modelo.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService implements
        GetUsuarioInterface,
        GetOrCreateUsuarioInterface {

    private final UsuarioRepositoryPort repository;

    @Override
    public Usuario get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
    }

    /**
     * El googleId es la identidad estable, no el email: Google permite cambiar
     * la direccion de una cuenta sin que cambie el identificador.
     *
     * <p>En cada login se refrescan nombre y avatar, que el usuario puede
     * haber cambiado en su cuenta de Google desde la ultima vez.
     */
    @Override
    public Usuario getOrCreate(PerfilGoogle perfil) {
        return repository.findByGoogleId(perfil.googleId())
                .map(existente -> actualizarDatosDeGoogle(existente, perfil))
                .orElseGet(() -> crear(perfil));
    }

    private Usuario actualizarDatosDeGoogle(Usuario usuario, PerfilGoogle perfil) {
        usuario.setEmail(perfil.email());
        usuario.setNombre(perfil.nombre());
        usuario.setAvatarUrl(perfil.avatarUrl());
        return repository.save(usuario);
    }

    private Usuario crear(PerfilGoogle perfil) {
        Usuario nuevo = Usuario.builder()
                .googleId(perfil.googleId())
                .email(perfil.email())
                .nombre(perfil.nombre())
                .avatarUrl(perfil.avatarUrl())
                .creadoEn(Instant.now())
                .build();

        return repository.save(nuevo);
    }
}
