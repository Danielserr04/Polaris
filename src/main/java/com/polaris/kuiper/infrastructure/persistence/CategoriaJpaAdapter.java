package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.out.CategoriaRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaFilter;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.kuiper.infrastructure.persistence.mapper.CategoriaEntityMapper;
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
public class CategoriaJpaAdapter implements CategoriaRepositoryPort {

    private final CategoriaRepository repository;
    private final CategoriaEntityMapper mapper;

    @Override
    public Categoria save(Categoria categoria) {
        return mapper.toDomain(repository.save(mapper.toEntity(categoria)));
    }

    @Override
    public Optional<Categoria> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Categoria> findAll(Long usuarioId, CategoriaFilter filter) {
        Specification<CategoriaEntity> spec = CategoriaSpecifications.from(usuarioId, filter);
        return mapper.toDomainList(repository.findAll(spec, Sort.by("nombre")));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Categoria> findByUsuarioIdAndNombreAndTipo(Long usuarioId, String nombre, TipoMovimiento tipo) {
        return repository.findByUsuarioIdAndNombreAndTipo(usuarioId, nombre, tipo).map(mapper::toDomain);
    }
}
