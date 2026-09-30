package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.out.ProgresionRepositoryPort;
import com.polaris.atlas.domain.model.ProgresionFilter;
import com.polaris.atlas.domain.model.ProgresionSesion;
import com.polaris.atlas.infrastructure.persistence.mapper.ProgresionProyeccionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Ejecuta la agregacion por sesion de SerieRegistroRepository y la pasa a dominio. */
@Component
@RequiredArgsConstructor
public class ProgresionJpaAdapter implements ProgresionRepositoryPort {

    private final SerieRegistroRepository repository;
    private final ProgresionProyeccionMapper mapper;

    @Override
    public List<ProgresionSesion> findProgresion(Long usuarioId, ProgresionFilter filter) {
        return mapper.toDomainList(repository.findProgresion(
                usuarioId, filter.getEjercicioId(), filter.getDesde(), filter.getHasta()));
    }
}
