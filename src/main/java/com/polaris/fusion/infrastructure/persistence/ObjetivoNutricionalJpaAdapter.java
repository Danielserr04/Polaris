package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.out.ObjetivoNutricionalRepositoryPort;
import com.polaris.fusion.domain.model.ObjetivoNutricional;
import com.polaris.fusion.infrastructure.persistence.mapper.ObjetivoNutricionalEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * El unico punto del modulo donde conviven modelo y Entity.
 */
@Component
@RequiredArgsConstructor
public class ObjetivoNutricionalJpaAdapter implements ObjetivoNutricionalRepositoryPort {

    private final ObjetivoNutricionalRepository repository;
    private final ObjetivoNutricionalEntityMapper mapper;

    @Override
    public ObjetivoNutricional save(ObjetivoNutricional objetivo) {
        return mapper.toDomain(repository.save(mapper.toEntity(objetivo)));
    }

    @Override
    public Optional<ObjetivoNutricional> findVigente(Long usuarioId, LocalDate fecha) {
        return repository
                .findFirstByUsuarioIdAndVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(usuarioId, fecha)
                .map(mapper::toDomain);
    }

    @Override
    public List<ObjetivoNutricional> findAll(Long usuarioId) {
        return mapper.toDomainList(repository.findByUsuarioIdOrderByVigenteDesdeDesc(usuarioId));
    }

    @Override
    public boolean existsByUsuarioIdAndVigenteDesde(Long usuarioId, LocalDate vigenteDesde) {
        return repository.existsByUsuarioIdAndVigenteDesde(usuarioId, vigenteDesde);
    }
}
