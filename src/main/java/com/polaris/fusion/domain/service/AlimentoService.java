package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.in.CreateAlimentoInterface;
import com.polaris.fusion.application.in.DeleteAlimentoInterface;
import com.polaris.fusion.application.in.GetAlimentoInterface;
import com.polaris.fusion.application.in.ListAlimentoInterface;
import com.polaris.fusion.application.in.UpdateAlimentoInterface;
import com.polaris.fusion.application.out.AlimentoRepositoryPort;
import com.polaris.fusion.application.out.ComidaLineaRepositoryPort;
import com.polaris.fusion.application.out.PlanComidaLineaRepositoryPort;
import com.polaris.fusion.application.out.RecetaIngredienteRepositoryPort;
import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.AlimentoFilter;
import com.polaris.fusion.domain.model.AlimentoNotFoundException;
import com.polaris.fusion.domain.model.FuenteAlimento;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Catalogo compartido: no filtra por usuario. Ver
 * docs/decisiones/015-alimento-catalogo-compartido-macros-por-100g.md.
 *
 * <p>Un alimento usado en alguna linea de comida, de cualquier usuario, no se
 * borra: 400, como TituloService con sus entradas. Ver
 * docs/decisiones/017-comida-agregado-con-lineas-macros-al-vuelo.md. Lo mismo
 * si esta en alguna receta o plan de comidas (ADR 050 y 051).
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
    private final ComidaLineaRepositoryPort comidaLineaRepository;
    private final RecetaIngredienteRepositoryPort recetaIngredienteRepository;
    private final PlanComidaLineaRepositoryPort planComidaLineaRepository;

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

        if (comidaLineaRepository.existsByAlimentoId(id)) {
            throw new ValidationException("No se puede borrar un alimento que esta en alguna comida");
        }
        if (recetaIngredienteRepository.existsByAlimentoId(id)) {
            throw new ValidationException("No se puede borrar un alimento que esta en alguna receta");
        }
        if (planComidaLineaRepository.existsByAlimentoId(id)) {
            throw new ValidationException("No se puede borrar un alimento que esta en algun plan de comidas");
        }

        repository.deleteById(id);
    }
}
