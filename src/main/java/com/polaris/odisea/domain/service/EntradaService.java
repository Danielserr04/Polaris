package com.polaris.odisea.domain.service;

import com.polaris.odisea.application.in.CreateEntradaInterface;
import com.polaris.odisea.application.in.DeleteEntradaInterface;
import com.polaris.odisea.application.in.GetEntradaInterface;
import com.polaris.odisea.application.in.ListEntradaInterface;
import com.polaris.odisea.application.in.UpdateEntradaInterface;
import com.polaris.odisea.application.out.EntradaRepositoryPort;
import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.domain.model.EntradaFilter;
import com.polaris.odisea.domain.model.EntradaNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Que el titulo referenciado exista se valida en EntradaJpaAdapter, no aqui:
 * es una comprobacion de integridad al escribir, no una regla del dominio.
 */
@Service
@RequiredArgsConstructor
public class EntradaService implements
        CreateEntradaInterface,
        GetEntradaInterface,
        ListEntradaInterface,
        UpdateEntradaInterface,
        DeleteEntradaInterface {

    private final EntradaRepositoryPort repository;

    @Override
    public Entrada create(Long usuarioId, Entrada entrada) {
        entrada.setUsuarioId(usuarioId);
        return repository.save(entrada);
    }

    @Override
    public Entrada get(Long usuarioId, Long id) {
        return getPropia(usuarioId, id);
    }

    @Override
    public List<Entrada> list(Long usuarioId, EntradaFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    @Override
    public Entrada update(Long usuarioId, Long id, Entrada entrada) {
        Entrada existente = getPropia(usuarioId, id);
        entrada.setId(existente.getId());
        entrada.setUsuarioId(existente.getUsuarioId());
        return repository.save(entrada);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropia(usuarioId, id);
        repository.deleteById(id);
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private Entrada getPropia(Long usuarioId, Long id) {
        Entrada entrada = repository.findById(id)
                .orElseThrow(() -> new EntradaNotFoundException(id));

        if (!entrada.getUsuarioId().equals(usuarioId)) {
            throw new EntradaNotFoundException(id);
        }

        return entrada;
    }
}
