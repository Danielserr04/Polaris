package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.domain.model.CategoriaNotFoundException;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.infrastructure.persistence.mapper.MovimientoEntityMapper;
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
public class MovimientoJpaAdapter implements MovimientoRepositoryPort {

    private final MovimientoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoEntityMapper mapper;

    /**
     * findById, no getReferenceById: un proxy lazy sin inicializar reventaria
     * al mapear a dominio si repository.save() ya cerro su transaccion (aqui
     * open-in-view: false). Es la misma razon que en EntradaJpaAdapter.
     */
    @Override
    public Movimiento save(Movimiento movimiento) {
        CategoriaEntity categoria = categoriaRepository.findById(movimiento.getCategoriaId())
                .orElseThrow(() -> new CategoriaNotFoundException(movimiento.getCategoriaId()));

        MovimientoEntity entity = mapper.toEntity(movimiento);
        entity.setCategoria(categoria);

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Movimiento> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Movimiento> findAll(Long usuarioId, MovimientoFilter filter) {
        Specification<MovimientoEntity> spec = MovimientoSpecifications.from(usuarioId, filter);
        Sort orden = Sort.by(Sort.Direction.DESC, "fecha").and(Sort.by(Sort.Direction.DESC, "id"));
        return mapper.toDomainList(repository.findAll(spec, orden));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByCategoriaId(Long categoriaId) {
        return repository.existsByCategoria_Id(categoriaId);
    }
}
