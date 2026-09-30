package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.application.out.RegistroPesoRepositoryPort;
import com.polaris.nucleo.domain.model.RegistroPeso;
import com.polaris.nucleo.domain.model.RegistroPesoFilter;
import com.polaris.nucleo.infrastructure.persistence.mapper.RegistroPesoEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * El unico punto del modulo donde conviven modelo y Entity.
 */
@Component
@RequiredArgsConstructor
public class RegistroPesoJpaAdapter implements RegistroPesoRepositoryPort {

    private final RegistroPesoRepository repository;
    private final RegistroPesoEntityMapper mapper;

    @Override
    public RegistroPeso save(RegistroPeso registro) {
        return mapper.toDomain(repository.save(mapper.toEntity(registro)));
    }

    @Override
    public Optional<RegistroPeso> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<RegistroPeso> findAll(Long usuarioId, RegistroPesoFilter filter) {
        Specification<RegistroPesoEntity> spec = RegistroPesoSpecifications.from(usuarioId, filter);
        return mapper.toDomainList(repository.findAll(spec, Sort.by(Sort.Direction.DESC, "fecha")));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<RegistroPeso> findByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha) {
        return repository.findByUsuarioIdAndFecha(usuarioId, fecha).map(mapper::toDomain);
    }
}
