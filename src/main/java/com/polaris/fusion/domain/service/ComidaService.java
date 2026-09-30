package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.in.CreateComidaInterface;
import com.polaris.fusion.application.in.DeleteComidaInterface;
import com.polaris.fusion.application.in.GetComidaInterface;
import com.polaris.fusion.application.in.ListComidaInterface;
import com.polaris.fusion.application.in.UpdateComidaInterface;
import com.polaris.fusion.application.out.AlimentoRepositoryPort;
import com.polaris.fusion.application.out.ComidaRepositoryPort;
import com.polaris.fusion.domain.model.AlimentoNotFoundException;
import com.polaris.fusion.domain.model.Comida;
import com.polaris.fusion.domain.model.ComidaFilter;
import com.polaris.fusion.domain.model.ComidaLinea;
import com.polaris.fusion.domain.model.ComidaNotFoundException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Comida y sus lineas son un solo agregado: se crean y editan juntas. Ver
 * docs/decisiones/017-comida-agregado-con-lineas-macros-al-vuelo.md.
 *
 * <p>El usuario de la comida y de cada linea sale del JWT, nunca del cliente.
 * Los alimentos son catalogo compartido: solo se comprueba que existan.
 */
@Service
@RequiredArgsConstructor
public class ComidaService implements
        CreateComidaInterface,
        GetComidaInterface,
        ListComidaInterface,
        UpdateComidaInterface,
        DeleteComidaInterface {

    static final int MAX_LINEAS = 50;

    private final ComidaRepositoryPort repository;
    private final AlimentoRepositoryPort alimentoRepository;

    @Override
    public Comida create(Long usuarioId, Comida comida) {
        validarLineas(comida);
        prepararLineas(usuarioId, comida);
        comida.setId(null);
        comida.setUsuarioId(usuarioId);
        return repository.save(comida);
    }

    @Override
    public Comida get(Long usuarioId, Long id) {
        return getPropia(usuarioId, id);
    }

    @Override
    public List<Comida> list(Long usuarioId, ComidaFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    /** Reemplazo completo: el conjunto de lineas que llega sustituye al anterior. */
    @Override
    public Comida update(Long usuarioId, Long id, Comida comida) {
        Comida existente = getPropia(usuarioId, id);
        validarLineas(comida);
        prepararLineas(usuarioId, comida);
        comida.setId(existente.getId());
        comida.setUsuarioId(existente.getUsuarioId());
        return repository.save(comida);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropia(usuarioId, id);
        repository.deleteById(id);
    }

    private void validarLineas(Comida comida) {
        List<ComidaLinea> lineas = comida.getLineas();
        if (lineas == null || lineas.isEmpty()) {
            throw new ValidationException("Una comida necesita al menos una linea");
        }
        if (lineas.size() > MAX_LINEAS) {
            throw new ValidationException("Una comida admite como mucho " + MAX_LINEAS + " lineas");
        }
    }

    /**
     * Anula ids de linea (nunca vienen del cliente), fija el usuario y
     * comprueba que cada alimento existe (404 si no).
     */
    private void prepararLineas(Long usuarioId, Comida comida) {
        for (ComidaLinea linea : comida.getLineas()) {
            Long alimentoId = linea.getAlimentoId();
            alimentoRepository.findById(alimentoId)
                    .orElseThrow(() -> new AlimentoNotFoundException(alimentoId));
            linea.setId(null);
            linea.setUsuarioId(usuarioId);
        }
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private Comida getPropia(Long usuarioId, Long id) {
        Comida comida = repository.findById(id)
                .orElseThrow(() -> new ComidaNotFoundException(id));

        if (!comida.getUsuarioId().equals(usuarioId)) {
            throw new ComidaNotFoundException(id);
        }

        return comida;
    }
}
