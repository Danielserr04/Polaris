package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.out.TrabajoMuscularRepositoryPort;
import com.polaris.atlas.domain.model.TrabajoMuscular;
import com.polaris.atlas.domain.model.TrabajoMuscularFilter;
import com.polaris.atlas.infrastructure.persistence.mapper.TrabajoMuscularProyeccionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Ejecuta la agregacion por grupo muscular de SerieRegistroRepository y la pasa a dominio. */
@Component
@RequiredArgsConstructor
public class TrabajoMuscularJpaAdapter implements TrabajoMuscularRepositoryPort {

    private final SerieRegistroRepository repository;
    private final TrabajoMuscularProyeccionMapper mapper;

    @Override
    public List<TrabajoMuscular> findTrabajo(Long usuarioId, TrabajoMuscularFilter filter) {
        return mapper.toDomainList(repository.findTrabajoMuscular(usuarioId, filter.getDesde(), filter.getHasta()));
    }
}
