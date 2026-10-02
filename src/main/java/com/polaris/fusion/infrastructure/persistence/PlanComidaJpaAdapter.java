package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.out.PlanComidaRepositoryPort;
import com.polaris.fusion.domain.model.AlimentoNotFoundException;
import com.polaris.fusion.domain.model.PlanComida;
import com.polaris.fusion.domain.model.PlanComidaFilter;
import com.polaris.fusion.domain.model.PlanComidaLinea;
import com.polaris.fusion.domain.model.RecetaNotFoundException;
import com.polaris.fusion.infrastructure.persistence.mapper.PlanComidaEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * El unico punto donde conviven PlanComida y PlanComidaEntity.
 */
@Component
@RequiredArgsConstructor
public class PlanComidaJpaAdapter implements PlanComidaRepositoryPort {

    private final PlanComidaRepository repository;
    private final AlimentoRepository alimentoRepository;
    private final RecetaRepository recetaRepository;
    private final PlanComidaEntityMapper mapper;

    /**
     * El mapper deja alimento y receta sin rellenar: aqui cada linea recibe la
     * Entity completa (findById, no un proxy) segun los ids del modelo, en el
     * mismo orden, y la referencia a su plan.
     */
    @Override
    public PlanComida save(PlanComida plan) {
        PlanComidaEntity entity = mapper.toEntity(plan);
        List<PlanComidaLinea> lineas = plan.getLineas();

        for (int i = 0; i < lineas.size(); i++) {
            PlanComidaLinea linea = lineas.get(i);
            PlanComidaLineaEntity lineaEntity = entity.getLineas().get(i);
            Long alimentoId = linea.getAlimentoId();
            Long recetaId = linea.getRecetaId();
            lineaEntity.setAlimento(alimentoId == null ? null : alimentoRepository.findById(alimentoId)
                    .orElseThrow(() -> new AlimentoNotFoundException(alimentoId)));
            lineaEntity.setReceta(recetaId == null ? null : recetaRepository.findById(recetaId)
                    .orElseThrow(() -> new RecetaNotFoundException(recetaId)));
            lineaEntity.setPlan(entity);
        }

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<PlanComida> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<PlanComida> findAll(Long usuarioId, PlanComidaFilter filter) {
        Specification<PlanComidaEntity> spec = PlanComidaSpecifications.from(usuarioId, filter);
        Sort orden = Sort.by(Sort.Order.desc("activo"), Sort.Order.asc("nombre"), Sort.Order.asc("id"));
        return mapper.toDomainList(repository.findAll(spec, orden));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void activarUnico(Long usuarioId, Long id) {
        repository.activarUnico(usuarioId, id);
    }
}
