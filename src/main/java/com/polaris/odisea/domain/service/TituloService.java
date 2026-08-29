package com.polaris.odisea.domain.service;

import com.polaris.odisea.application.in.CreateTituloInterface;
import com.polaris.odisea.application.in.DeleteTituloInterface;
import com.polaris.odisea.application.in.GetTituloInterface;
import com.polaris.odisea.application.in.ListTituloInterface;
import com.polaris.odisea.application.in.UpdateTituloInterface;
import com.polaris.odisea.application.out.TituloRepositoryPort;
import com.polaris.odisea.domain.model.Titulo;
import com.polaris.odisea.domain.model.TituloFilter;
import com.polaris.odisea.domain.model.TituloNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TituloService implements
        CreateTituloInterface,
        GetTituloInterface,
        ListTituloInterface,
        UpdateTituloInterface,
        DeleteTituloInterface {

    private final TituloRepositoryPort repository;

    @Override
    public Titulo create(Titulo titulo) {
        return repository.save(titulo);
    }

    @Override
    public Titulo get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TituloNotFoundException(id));
    }

    @Override
    public List<Titulo> list(TituloFilter filter) {
        return repository.findAll(filter);
    }

    @Override
    public Titulo update(Long id, Titulo titulo) {
        Titulo existente = get(id);
        titulo.setId(existente.getId());
        return repository.save(titulo);
    }

    @Override
    public void delete(Long id) {
        get(id);
        repository.deleteById(id);
    }
}
