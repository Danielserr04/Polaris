package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.in.GetTrabajoMuscularInterface;
import com.polaris.atlas.application.out.TrabajoMuscularRepositoryPort;
import com.polaris.atlas.domain.model.TrabajoMuscular;
import com.polaris.atlas.domain.model.TrabajoMuscularFilter;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * Series, sesiones y volumen por grupo muscular, calculados en la base. Ver
 * docs/decisiones/042-calendario-y-mapa-muscular.md.
 */
@Service
@RequiredArgsConstructor
public class TrabajoMuscularService implements GetTrabajoMuscularInterface {

    private static final int ESCALA = 2;

    private final TrabajoMuscularRepositoryPort repository;

    @Override
    public List<TrabajoMuscular> get(Long usuarioId, TrabajoMuscularFilter filter) {
        TrabajoMuscularFilter filtro = filter != null ? filter : new TrabajoMuscularFilter();
        if (filtro.getDesde() != null && filtro.getHasta() != null && filtro.getDesde().isAfter(filtro.getHasta())) {
            throw new ValidationException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        return repository.findTrabajo(usuarioId, filtro).stream()
                .map(TrabajoMuscularService::conEscala)
                .sorted(Comparator.comparingInt(TrabajoMuscular::getNumeroSeries).reversed()
                        .thenComparing(TrabajoMuscular::getGrupoMuscular))
                .toList();
    }

    private static TrabajoMuscular conEscala(TrabajoMuscular t) {
        return TrabajoMuscular.builder()
                .grupoMuscular(t.getGrupoMuscular())
                .numeroSeries(t.getNumeroSeries())
                .numeroSesiones(t.getNumeroSesiones())
                .volumen(t.getVolumen().setScale(ESCALA, RoundingMode.HALF_UP))
                .build();
    }
}
