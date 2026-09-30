package com.polaris.nucleo.domain.service;

import com.polaris.nucleo.application.in.GetPerfilInterface;
import com.polaris.nucleo.application.in.UpdatePerfilInterface;
import com.polaris.nucleo.application.out.PerfilRepositoryPort;
import com.polaris.nucleo.domain.model.Perfil;
import com.polaris.nucleo.domain.model.PerfilNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Un perfil por usuario, siempre el del JWT: nunca se busca por id, asi que
 * no hay forma de pedir el perfil de otro. Ver
 * docs/decisiones/009-perfil-unico-por-usuario.md.
 */
@Service
@RequiredArgsConstructor
public class PerfilService implements
        GetPerfilInterface,
        UpdatePerfilInterface {

    private final PerfilRepositoryPort repository;

    @Override
    public Perfil get(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNotFoundException(usuarioId));
    }

    /**
     * Si ya existe, conserva su id para que el save actualice y no inserte
     * una segunda fila (que el unique de usuario_id rechazaria).
     */
    @Override
    public Perfil update(Long usuarioId, Perfil perfil) {
        perfil.setId(repository.findByUsuarioId(usuarioId)
                .map(Perfil::getId)
                .orElse(null));
        perfil.setUsuarioId(usuarioId);
        return repository.save(perfil);
    }
}
