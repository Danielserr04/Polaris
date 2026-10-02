package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.out.RecurrenteRepositoryPort;
import com.polaris.kuiper.domain.model.CategoriaNotFoundException;
import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.domain.model.RecurrenteFilter;
import com.polaris.kuiper.infrastructure.persistence.mapper.RecurrenteEntityMapper;
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
public class RecurrenteJpaAdapter implements RecurrenteRepositoryPort {

    private final RecurrenteRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final RecurrenteEntityMapper mapper;

    /** findById, no getReferenceById, por lo mismo que en MovimientoJpaAdapter. */
    @Override
    public Recurrente save(Recurrente recurrente) {
        CategoriaEntity categoria = categoriaRepository.findById(recurrente.getCategoriaId())
                .orElseThrow(() -> new CategoriaNotFoundException(recurrente.getCategoriaId()));

        RecurrenteEntity entity = mapper.toEntity(recurrente);
        entity.setCategoria(categoria);

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Recurrente> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Recurrente> findAll(Long usuarioId, RecurrenteFilter filter) {
        Specification<RecurrenteEntity> spec = RecurrenteSpecifications.from(usuarioId, filter);
        Sort orden = Sort.by("proximaFecha").and(Sort.by("id"));
        return mapper.toDomainList(repository.findAll(spec, orden));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<Recurrente> findPendientes(LocalDate hoy) {
        return mapper.toDomainList(repository.findByActivoTrueAndProximaFechaLessThanEqualOrderByIdAsc(hoy));
    }

    @Override
    public boolean existsByCategoriaId(Long categoriaId) {
        return repository.existsByCategoria_Id(categoriaId);
    }
}
