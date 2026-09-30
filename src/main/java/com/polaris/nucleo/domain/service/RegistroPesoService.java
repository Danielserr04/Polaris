package com.polaris.nucleo.domain.service;

import com.polaris.nucleo.application.in.CreateRegistroPesoInterface;
import com.polaris.nucleo.application.in.DeleteRegistroPesoInterface;
import com.polaris.nucleo.application.in.GetRegistroPesoInterface;
import com.polaris.nucleo.application.in.ListRegistroPesoInterface;
import com.polaris.nucleo.application.in.UpdateRegistroPesoInterface;
import com.polaris.nucleo.application.out.RegistroPesoRepositoryPort;
import com.polaris.nucleo.domain.model.RegistroPeso;
import com.polaris.nucleo.domain.model.RegistroPesoFilter;
import com.polaris.nucleo.domain.model.RegistroPesoNotFoundException;
import com.polaris.shared.error.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Un peso por dia (unique usuario_id + fecha). Ver
 * docs/decisiones/010-registro-peso-un-peso-por-dia.md.
 */
@Service
@RequiredArgsConstructor
public class RegistroPesoService implements
        CreateRegistroPesoInterface,
        GetRegistroPesoInterface,
        ListRegistroPesoInterface,
        UpdateRegistroPesoInterface,
        DeleteRegistroPesoInterface {

    private final RegistroPesoRepositoryPort repository;

    /**
     * Si ya hay registro ese dia, conserva su id para que el save actualice y
     * no inserte una segunda fila (que el unique de la tabla rechazaria).
     */
    @Override
    public RegistroPeso create(Long usuarioId, RegistroPeso registro) {
        registro.setId(repository.findByUsuarioIdAndFecha(usuarioId, registro.getFecha())
                .map(RegistroPeso::getId)
                .orElse(null));
        registro.setUsuarioId(usuarioId);
        return repository.save(registro);
    }

    @Override
    public RegistroPeso get(Long usuarioId, Long id) {
        return getPropio(usuarioId, id);
    }

    @Override
    public List<RegistroPeso> list(Long usuarioId, RegistroPesoFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    /**
     * Mover un registro a una fecha que ya tiene otro es un 409: aqui no hay
     * "actualizar el otro", el cliente pidio editar este.
     */
    @Override
    public RegistroPeso update(Long usuarioId, Long id, RegistroPeso registro) {
        RegistroPeso existente = getPropio(usuarioId, id);

        repository.findByUsuarioIdAndFecha(usuarioId, registro.getFecha())
                .filter(otro -> !otro.getId().equals(existente.getId()))
                .ifPresent(otro -> {
                    throw new DuplicateResourceException("Ya hay un peso registrado en esa fecha");
                });

        registro.setId(existente.getId());
        registro.setUsuarioId(existente.getUsuarioId());
        return repository.save(registro);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropio(usuarioId, id);
        repository.deleteById(id);
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private RegistroPeso getPropio(Long usuarioId, Long id) {
        RegistroPeso registro = repository.findById(id)
                .orElseThrow(() -> new RegistroPesoNotFoundException(id));

        if (!registro.getUsuarioId().equals(usuarioId)) {
            throw new RegistroPesoNotFoundException(id);
        }

        return registro;
    }
}
