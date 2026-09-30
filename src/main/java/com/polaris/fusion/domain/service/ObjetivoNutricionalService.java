package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.in.CreateObjetivoNutricionalInterface;
import com.polaris.fusion.application.in.GetObjetivoNutricionalInterface;
import com.polaris.fusion.application.in.ListObjetivoNutricionalInterface;
import com.polaris.fusion.application.out.ObjetivoNutricionalRepositoryPort;
import com.polaris.fusion.domain.model.ObjetivoNutricional;
import com.polaris.fusion.domain.model.ObjetivoNutricionalNotFoundException;
import com.polaris.shared.error.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Historico inmutable: solo se crean filas nuevas. Ver
 * docs/decisiones/016-objetivo-nutricional-historico-inmutable.md.
 */
@Service
@RequiredArgsConstructor
public class ObjetivoNutricionalService implements
        CreateObjetivoNutricionalInterface,
        GetObjetivoNutricionalInterface,
        ListObjetivoNutricionalInterface {

    private final ObjetivoNutricionalRepositoryPort repository;

    /**
     * Dos objetivos con la misma vigente_desde no tienen orden: 409 en vez de
     * sustituir. El id se fuerza a null para que nunca pueda ser una actualizacion.
     */
    @Override
    public ObjetivoNutricional create(Long usuarioId, ObjetivoNutricional objetivo) {
        if (repository.existsByUsuarioIdAndVigenteDesde(usuarioId, objetivo.getVigenteDesde())) {
            throw new DuplicateResourceException("Ya hay un objetivo nutricional con vigencia desde esa fecha");
        }

        objetivo.setId(null);
        objetivo.setUsuarioId(usuarioId);
        return repository.save(objetivo);
    }

    @Override
    public ObjetivoNutricional getVigente(Long usuarioId, LocalDate fecha) {
        return repository.findVigente(usuarioId, fecha)
                .orElseThrow(() -> new ObjetivoNutricionalNotFoundException(fecha));
    }

    @Override
    public List<ObjetivoNutricional> list(Long usuarioId) {
        return repository.findAll(usuarioId);
    }
}
