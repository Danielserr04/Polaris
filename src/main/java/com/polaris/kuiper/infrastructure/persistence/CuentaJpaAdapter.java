package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.out.CuentaRepositoryPort;
import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.domain.model.CuentaFilter;
import com.polaris.kuiper.infrastructure.persistence.mapper.CuentaEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * El unico punto del modulo donde conviven modelo y Entity.
 */
@Component
@RequiredArgsConstructor
public class CuentaJpaAdapter implements CuentaRepositoryPort {

    private final CuentaRepository repository;
    private final CuentaEntityMapper mapper;

    @Override
    public Cuenta save(Cuenta cuenta) {
        return mapper.toDomain(repository.save(mapper.toEntity(cuenta)));
    }

    @Override
    public Optional<Cuenta> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    /** Activas primero (archivada = false ordena antes) y por nombre. */
    @Override
    public List<Cuenta> findAll(Long usuarioId, CuentaFilter filter) {
        Specification<CuentaEntity> spec = CuentaSpecifications.from(usuarioId, filter);
        Sort orden = Sort.by("archivada").and(Sort.by("nombre")).and(Sort.by("id"));
        return mapper.toDomainList(repository.findAll(spec, orden));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Cuenta> findByUsuarioIdAndNombre(Long usuarioId, String nombre) {
        return repository.findByUsuarioIdAndNombre(usuarioId, nombre).map(mapper::toDomain);
    }
}
