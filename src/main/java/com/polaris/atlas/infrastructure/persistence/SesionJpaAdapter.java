package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.out.SesionRepositoryPort;
import com.polaris.atlas.application.out.SesionRutinaRepositoryPort;
import com.polaris.atlas.domain.model.EjercicioNotFoundException;
import com.polaris.atlas.domain.model.Sesion;
import com.polaris.atlas.domain.model.SesionFilter;
import com.polaris.atlas.infrastructure.persistence.mapper.SesionEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * El unico punto del modulo donde conviven modelo y Entity. Implementa ademas
 * el puerto que usa RutinaService para proteger el borrado de una rutina con
 * sesiones.
 */
@Component
@RequiredArgsConstructor
public class SesionJpaAdapter implements SesionRepositoryPort, SesionRutinaRepositoryPort {

    private final SesionRepository repository;
    private final EjercicioRepository ejercicioRepository;
    private final SesionEntityMapper mapper;

    /**
     * Cada serie recibe su EjercicioEntity cargado con findById (no
     * getReferenceById: un proxy sin inicializar reventaria al mapear a dominio
     * con open-in-view: false) y la referencia a su sesion. Con la sesion ya
     * existente, save hace merge: las series que ya no estan en la coleccion se
     * borran (orphanRemoval) y las nuevas se insertan, en el orden de la lista:
     * ese es el reemplazo del PUT.
     */
    @Override
    public Sesion save(Sesion sesion) {
        SesionEntity entity = mapper.toEntity(sesion);

        Map<Long, EjercicioEntity> ejercicios = new HashMap<>();
        for (SerieRegistroEntity serie : entity.getSeries()) {
            Long ejercicioId = serie.getEjercicio().getId();
            serie.setEjercicio(ejercicios.computeIfAbsent(ejercicioId, id -> ejercicioRepository.findById(id)
                    .orElseThrow(() -> new EjercicioNotFoundException(id))));
            serie.setSesion(entity);
        }

        return conNombreDeRutina(mapper.toDomain(repository.save(entity)));
    }

    @Override
    public Optional<Sesion> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain).map(this::conNombreDeRutina);
    }

    @Override
    public List<Sesion> findAll(Long usuarioId, SesionFilter filter) {
        Specification<SesionEntity> spec = SesionSpecifications.from(usuarioId, filter);
        Sort orden = Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("id"));
        return conNombresDeRutina(mapper.toDomainList(repository.findAll(spec, orden)));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByRutinaId(Long rutinaId) {
        return repository.existsByRutinaId(rutinaId);
    }

    private Sesion conNombreDeRutina(Sesion sesion) {
        conNombresDeRutina(List.of(sesion));
        return sesion;
    }

    /** Una sola consulta por lectura, sea cual sea el numero de sesiones. */
    private List<Sesion> conNombresDeRutina(List<Sesion> sesiones) {
        Set<Long> rutinaIds = sesiones.stream().map(Sesion::getRutinaId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (rutinaIds.isEmpty()) {
            return sesiones;
        }
        Map<Long, String> nombres = repository.findNombresRutina(rutinaIds).stream()
                .collect(Collectors.toMap(SesionRepository.NombreRutina::getId,
                        SesionRepository.NombreRutina::getNombre));
        sesiones.forEach(s -> {
            if (s.getRutinaId() != null) {
                s.setRutinaNombre(nombres.get(s.getRutinaId()));
            }
        });
        return sesiones;
    }
}
