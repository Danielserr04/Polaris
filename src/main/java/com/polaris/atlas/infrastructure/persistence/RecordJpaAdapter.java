package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.out.RecordRepositoryPort;
import com.polaris.atlas.domain.model.MejorPesoEjercicio;
import com.polaris.atlas.domain.model.VolumenSesionEjercicio;
import com.polaris.atlas.infrastructure.persistence.mapper.RecordProyeccionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Ejecuta las agregaciones de records de SerieRegistroRepository y las pasa a dominio. */
@Component
@RequiredArgsConstructor
public class RecordJpaAdapter implements RecordRepositoryPort {

    private final SerieRegistroRepository repository;
    private final RecordProyeccionMapper mapper;

    @Override
    public List<MejorPesoEjercicio> findMejorPesoPorEjercicio(Long usuarioId) {
        return mapper.toMejorPesoList(repository.findMejorPesoPorEjercicio(usuarioId));
    }

    @Override
    public List<VolumenSesionEjercicio> findVolumenPorEjercicioYSesion(Long usuarioId) {
        return mapper.toVolumenList(repository.findVolumenPorEjercicioYSesion(usuarioId));
    }
}
