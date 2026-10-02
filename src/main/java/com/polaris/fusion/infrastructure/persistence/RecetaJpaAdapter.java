package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.out.RecetaRepositoryPort;
import com.polaris.fusion.domain.model.AlimentoNotFoundException;
import com.polaris.fusion.domain.model.Receta;
import com.polaris.fusion.domain.model.RecetaFilter;
import com.polaris.fusion.infrastructure.persistence.mapper.RecetaEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * El unico punto donde conviven Receta y RecetaEntity. Mismo patron que
 * ComidaJpaAdapter.
 */
@Component
@RequiredArgsConstructor
public class RecetaJpaAdapter implements RecetaRepositoryPort {

    private final RecetaRepository repository;
    private final AlimentoRepository alimentoRepository;
    private final RecetaEntityMapper mapper;

    /**
     * Cada ingrediente recibe su AlimentoEntity cargado con findById (no un
     * proxy: reventaria al mapear con open-in-view: false) y la referencia a
     * su receta. Con la receta ya existente, save hace merge y orphanRemoval
     * borra los ingredientes que ya no estan.
     */
    @Override
    public Receta save(Receta receta) {
        RecetaEntity entity = mapper.toEntity(receta);

        for (RecetaIngredienteEntity ingrediente : entity.getIngredientes()) {
            Long alimentoId = ingrediente.getAlimento().getId();
            ingrediente.setAlimento(alimentoRepository.findById(alimentoId)
                    .orElseThrow(() -> new AlimentoNotFoundException(alimentoId)));
            ingrediente.setReceta(entity);
        }

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Receta> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Receta> findAll(Long usuarioId, RecetaFilter filter) {
        Specification<RecetaEntity> spec = RecetaSpecifications.from(usuarioId, filter);
        Sort orden = Sort.by(Sort.Order.asc("nombre"), Sort.Order.asc("id"));
        return mapper.toDomainList(repository.findAll(spec, orden));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
