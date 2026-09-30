package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.in.CreateAlimentoInterface;
import com.polaris.fusion.application.in.DeleteAlimentoInterface;
import com.polaris.fusion.application.in.GetAlimentoInterface;
import com.polaris.fusion.application.in.ListAlimentoInterface;
import com.polaris.fusion.application.in.UpdateAlimentoInterface;
import com.polaris.fusion.application.out.AlimentoRepositoryPort;
import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.AlimentoFilter;
import com.polaris.fusion.domain.model.AlimentoNotFoundException;
import com.polaris.fusion.domain.model.FuenteAlimento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Catalogo compartido: no filtra por usuario. Ver
 * docs/decisiones/015-alimento-catalogo-compartido-macros-por-100g.md.
 *
 * <p>Todavia no impide borrar un alimento usado en una comida: ComidaLinea no
 * existe. Esa comprobacion (400, como TituloService con sus entradas) entra con
 * ComidaLinea.
 */
@Service
@RequiredArgsConstructor
public class AlimentoService implements
        CreateAlimentoInterface,
        GetAlimentoInterface,
        ListAlimentoInterface,
        UpdateAlimentoInterface,
        DeleteAlimentoInterface {

    private final AlimentoRepositoryPort repository;

    /** Siempre MANUAL y sin idExterno: un alimento creado a mano no puede hacerse pasar por uno externo. */
    @Override
    public Alimento create(Alimento alimento) {
        alimento.setId(null);
        alimento.setFuenteExterna(FuenteAlimento.MANUAL);
        alimento.setIdExterno(null);
        return repository.save(alimento);
    }

    @Override
    public Alimento get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AlimentoNotFoundException(id));
    }

    @Override
    public List<Alimento> list(AlimentoFilter filter) {
        return repository.findAll(filter);
    }

    /** Conserva id, fuente e idExterno: el cliente edita los datos, no el origen. */
    @Override
    public Alimento update(Long id, Alimento alimento) {
        Alimento existente = get(id);
        alimento.setId(existente.getId());
        alimento.setFuenteExterna(existente.getFuenteExterna());
        alimento.setIdExterno(existente.getIdExterno());
        return repository.save(alimento);
    }

    @Override
    public void delete(Long id) {
        get(id);
        repository.deleteById(id);
    }
}
