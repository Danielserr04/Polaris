package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.application.out.MedidaCorporalRepositoryPort;
import com.polaris.nucleo.domain.model.MedidaCorporal;
import com.polaris.nucleo.domain.model.MedidaCorporalFilter;
import com.polaris.nucleo.infrastructure.persistence.mapper.MedidaCorporalEntityMapper;
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
public class MedidaCorporalJpaAdapter implements MedidaCorporalRepositoryPort {

    private final MedidaCorporalRepository repository;
    private final MedidaCorporalEntityMapper mapper;

    @Override
    public MedidaCorporal save(MedidaCorporal registro) {
        return mapper.toDomain(repository.save(mapper.toEntity(registro)));
    }

    @Override
    public Optional<MedidaCorporal> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<MedidaCorporal> findAll(Long usuarioId, MedidaCorporalFilter filter) {
        Specification<MedidaCorporalEntity> spec = MedidaCorporalSpecifications.from(usuarioId, filter);
        return mapper.toDomainList(repository.findAll(spec, Sort.by(Sort.Direction.DESC, "fecha")));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<MedidaCorporal> findByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha) {
        return repository.findByUsuarioIdAndFecha(usuarioId, fecha).map(mapper::toDomain);
    }
}
