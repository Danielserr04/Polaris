package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.out.ComidaRepositoryPort;
import com.polaris.fusion.domain.model.AlimentoNotFoundException;
import com.polaris.fusion.domain.model.Comida;
import com.polaris.fusion.domain.model.ComidaFilter;
import com.polaris.fusion.infrastructure.persistence.mapper.ComidaEntityMapper;
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
public class ComidaJpaAdapter implements ComidaRepositoryPort {

    private final ComidaRepository repository;
    private final AlimentoRepository alimentoRepository;
    private final ComidaEntityMapper mapper;

    /**
     * Cada linea recibe su AlimentoEntity cargado con findById (no
     * getReferenceById: un proxy sin inicializar reventaria al mapear a dominio
     * con open-in-view: false) y la referencia a su comida. Con la comida ya
     * existente, save hace merge: las lineas que ya no estan en la coleccion se
     * borran (orphanRemoval) y las nuevas se insertan, que es el reemplazo del PUT.
     */
    @Override
    public Comida save(Comida comida) {
        ComidaEntity entity = mapper.toEntity(comida);

        for (ComidaLineaEntity linea : entity.getLineas()) {
            Long alimentoId = linea.getAlimento().getId();
            linea.setAlimento(alimentoRepository.findById(alimentoId)
                    .orElseThrow(() -> new AlimentoNotFoundException(alimentoId)));
            linea.setComida(entity);
        }

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Comida> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    /**
     * momento se ordena por la columna: es un ENUM de MySQL y se ordena por su
     * posicion, que es el orden natural del dia (ver V10__fusion_comida.sql).
     */
    @Override
    public List<Comida> findAll(Long usuarioId, ComidaFilter filter) {
        Specification<ComidaEntity> spec = ComidaSpecifications.from(usuarioId, filter);
        Sort orden = Sort.by(Sort.Order.desc("fecha"), Sort.Order.asc("momento"), Sort.Order.asc("id"));
        return mapper.toDomainList(repository.findAll(spec, orden));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
