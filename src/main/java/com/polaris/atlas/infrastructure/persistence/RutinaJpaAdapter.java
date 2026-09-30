package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.out.RutinaRepositoryPort;
import com.polaris.atlas.domain.model.EjercicioNotFoundException;
import com.polaris.atlas.domain.model.Rutina;
import com.polaris.atlas.domain.model.RutinaFilter;
import com.polaris.atlas.infrastructure.persistence.mapper.RutinaEntityMapper;
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
public class RutinaJpaAdapter implements RutinaRepositoryPort {

    private final RutinaRepository repository;
    private final EjercicioRepository ejercicioRepository;
    private final RutinaEntityMapper mapper;

    /**
     * Cada linea recibe su EjercicioEntity cargado con findById (no
     * getReferenceById: un proxy sin inicializar reventaria al mapear a dominio
     * con open-in-view: false) y la referencia a su rutina. Con la rutina ya
     * existente, save hace merge: las lineas que ya no estan en la coleccion se
     * borran (orphanRemoval) y las nuevas se insertan, que es el reemplazo del PUT.
     */
    @Override
    public Rutina save(Rutina rutina) {
        RutinaEntity entity = mapper.toEntity(rutina);

        for (RutinaEjercicioEntity linea : entity.getLineas()) {
            Long ejercicioId = linea.getEjercicio().getId();
            linea.setEjercicio(ejercicioRepository.findById(ejercicioId)
                    .orElseThrow(() -> new EjercicioNotFoundException(ejercicioId)));
            linea.setRutina(entity);
        }

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Rutina> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Rutina> findAll(Long usuarioId, RutinaFilter filter) {
        Specification<RutinaEntity> spec = RutinaSpecifications.from(usuarioId, filter);
        return mapper.toDomainList(repository.findAll(spec, Sort.by("nombre", "id")));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Rutina> findByUsuarioIdAndNombre(Long usuarioId, String nombre) {
        return repository.findByUsuarioIdAndNombre(usuarioId, nombre).map(mapper::toDomain);
    }
}
