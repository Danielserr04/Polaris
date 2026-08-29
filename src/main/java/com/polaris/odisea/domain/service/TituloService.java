package com.polaris.odisea.domain.service;

import com.polaris.odisea.application.in.CreateTituloInterface;
import com.polaris.odisea.application.in.DeleteTituloInterface;
import com.polaris.odisea.application.in.GetTituloInterface;
import com.polaris.odisea.application.in.ListTituloInterface;
import com.polaris.odisea.application.in.UpdateTituloInterface;
import com.polaris.odisea.application.out.EntradaRepositoryPort;
import com.polaris.odisea.application.out.TituloRepositoryPort;
import com.polaris.odisea.domain.model.Titulo;
import com.polaris.odisea.domain.model.TituloFilter;
import com.polaris.odisea.domain.model.TituloNotFoundException;
import com.polaris.shared.error.ValidationException;
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
    private final EntradaRepositoryPort entradaRepository;

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

    /**
     * Titulo es catalogo compartido: borrarlo en cascada arrastraria entradas
     * de otros usuarios. Se rechaza con 400 en vez de dejar que la FK reviente
     * con un error de base de datos crudo.
     */
    @Override
    public void delete(Long id) {
        get(id);

        if (entradaRepository.existsByTituloId(id)) {
            throw new ValidationException("No se puede borrar un titulo que tiene entradas asociadas");
        }

        repository.deleteById(id);
    }
}
