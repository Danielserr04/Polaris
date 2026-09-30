package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.domain.model.CategoriaNotFoundException;
import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.domain.model.PresupuestoFilter;
import com.polaris.kuiper.infrastructure.persistence.mapper.PresupuestoEntityMapper;
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
public class PresupuestoJpaAdapter implements PresupuestoRepositoryPort {

    private final PresupuestoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final PresupuestoEntityMapper mapper;

    /**
     * findById, no getReferenceById, por la misma razon que en
     * MovimientoJpaAdapter: un proxy lazy sin inicializar reventaria al mapear
     * a dominio con open-in-view: false.
     */
    @Override
    public Presupuesto save(Presupuesto presupuesto) {
        CategoriaEntity categoria = categoriaRepository.findById(presupuesto.getCategoriaId())
                .orElseThrow(() -> new CategoriaNotFoundException(presupuesto.getCategoriaId()));

        PresupuestoEntity entity = mapper.toEntity(presupuesto);
        entity.setCategoria(categoria);

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Presupuesto> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Presupuesto> findAll(Long usuarioId, PresupuestoFilter filter) {
        Specification<PresupuestoEntity> spec = PresupuestoSpecifications.from(usuarioId, filter);
        return mapper.toDomainList(repository.findAll(spec, Sort.by("categoria.nombre")));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Presupuesto> findByUsuarioIdAndCategoriaIdAndPeriodo(Long usuarioId, Long categoriaId,
                                                                          PeriodoPresupuesto periodo) {
        return repository.findByUsuarioIdAndCategoria_IdAndPeriodo(usuarioId, categoriaId, periodo)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByCategoriaId(Long categoriaId) {
        return repository.existsByCategoria_Id(categoriaId);
    }
}
