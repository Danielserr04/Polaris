package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.application.out.RecordatorioRepositoryPort;
import com.polaris.nucleo.domain.model.Recordatorio;
import com.polaris.nucleo.domain.model.RecordatorioFilter;
import com.polaris.nucleo.domain.model.TipoRecordatorio;
import com.polaris.nucleo.infrastructure.persistence.mapper.RecordatorioEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * El unico punto del modulo donde conviven modelo y Entity.
 */
@Component
@RequiredArgsConstructor
public class RecordatorioJpaAdapter implements RecordatorioRepositoryPort {

    private final RecordatorioRepository repository;
    private final RecordatorioEntityMapper mapper;

    @Override
    public Recordatorio save(Recordatorio recordatorio) {
        return mapper.toDomain(repository.save(mapper.toEntity(recordatorio)));
    }

    /** findOne es seguro: el unique (usuario_id, tipo) garantiza como mucho una fila. */
    @Override
    public Optional<Recordatorio> findByUsuarioIdAndTipo(Long usuarioId, TipoRecordatorio tipo) {
        return repository.findOne(RecordatorioSpecifications.deTipo(usuarioId, tipo))
                .map(mapper::toDomain);
    }

    @Override
    public List<Recordatorio> findAllByUsuarioId(Long usuarioId) {
        return mapper.toDomainList(repository.findAll(
                RecordatorioSpecifications.from(usuarioId, new RecordatorioFilter())));
    }
}
