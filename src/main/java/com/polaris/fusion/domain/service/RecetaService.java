package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.in.CreateRecetaInterface;
import com.polaris.fusion.application.in.DeleteRecetaInterface;
import com.polaris.fusion.application.in.GetRecetaInterface;
import com.polaris.fusion.application.in.ListRecetaInterface;
import com.polaris.fusion.application.in.UpdateRecetaInterface;
import com.polaris.fusion.application.out.AlimentoRepositoryPort;
import com.polaris.fusion.application.out.PlanComidaLineaRepositoryPort;
import com.polaris.fusion.application.out.RecetaRepositoryPort;
import com.polaris.fusion.domain.model.AlimentoNotFoundException;
import com.polaris.fusion.domain.model.Receta;
import com.polaris.fusion.domain.model.RecetaFilter;
import com.polaris.fusion.domain.model.RecetaIngrediente;
import com.polaris.fusion.domain.model.RecetaNotFoundException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Receta e ingredientes son un solo agregado, como Comida y sus lineas. Ver
 * docs/decisiones/050-receta-agregado-con-ingredientes.md.
 *
 * <p>Las recetas son de cada usuario (no catalogo compartido): el usuario de
 * la receta y de cada ingrediente sale del JWT. Una receta usada en algun plan
 * de comidas no se borra (400).
 */
@Service
@RequiredArgsConstructor
public class RecetaService implements
        CreateRecetaInterface,
        GetRecetaInterface,
        ListRecetaInterface,
        UpdateRecetaInterface,
        DeleteRecetaInterface {

    static final int MAX_INGREDIENTES = 50;

    private final RecetaRepositoryPort repository;
    private final AlimentoRepositoryPort alimentoRepository;
    private final PlanComidaLineaRepositoryPort planComidaLineaRepository;

    @Override
    public Receta create(Long usuarioId, Receta receta) {
        validarIngredientes(receta);
        prepararIngredientes(usuarioId, receta);
        receta.setId(null);
        receta.setUsuarioId(usuarioId);
        return repository.save(receta);
    }

    @Override
    public Receta get(Long usuarioId, Long id) {
        return getPropia(usuarioId, id);
    }

    @Override
    public List<Receta> list(Long usuarioId, RecetaFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    /** Reemplazo completo: los ingredientes que llegan sustituyen a los anteriores. */
    @Override
    public Receta update(Long usuarioId, Long id, Receta receta) {
        Receta existente = getPropia(usuarioId, id);
        validarIngredientes(receta);
        prepararIngredientes(usuarioId, receta);
        receta.setId(existente.getId());
        receta.setUsuarioId(existente.getUsuarioId());
        return repository.save(receta);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropia(usuarioId, id);

        if (planComidaLineaRepository.existsByRecetaId(id)) {
            throw new ValidationException("No se puede borrar una receta que esta en algun plan de comidas");
        }

        repository.deleteById(id);
    }

    private void validarIngredientes(Receta receta) {
        List<RecetaIngrediente> ingredientes = receta.getIngredientes();
        if (ingredientes == null || ingredientes.isEmpty()) {
            throw new ValidationException("Una receta necesita al menos un ingrediente");
        }
        if (ingredientes.size() > MAX_INGREDIENTES) {
            throw new ValidationException("Una receta admite como mucho " + MAX_INGREDIENTES + " ingredientes");
        }
    }

    /**
     * Anula ids de ingrediente (nunca vienen del cliente), fija el usuario y
     * comprueba que cada alimento existe (404 si no).
     */
    private void prepararIngredientes(Long usuarioId, Receta receta) {
        for (RecetaIngrediente ingrediente : receta.getIngredientes()) {
            Long alimentoId = ingrediente.getAlimentoId();
            alimentoRepository.findById(alimentoId)
                    .orElseThrow(() -> new AlimentoNotFoundException(alimentoId));
            ingrediente.setId(null);
            ingrediente.setUsuarioId(usuarioId);
        }
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private Receta getPropia(Long usuarioId, Long id) {
        Receta receta = repository.findById(id)
                .orElseThrow(() -> new RecetaNotFoundException(id));

        if (!receta.getUsuarioId().equals(usuarioId)) {
            throw new RecetaNotFoundException(id);
        }

        return receta;
    }
}
