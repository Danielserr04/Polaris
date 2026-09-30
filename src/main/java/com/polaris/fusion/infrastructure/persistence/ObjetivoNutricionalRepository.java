package com.polaris.fusion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data. Solo la usa ObjetivoNutricionalJpaAdapter. Sin
 * JpaSpecificationExecutor: no hay filtros dinamicos, solo consultas derivadas.
 */
public interface ObjetivoNutricionalRepository extends JpaRepository<ObjetivoNutricionalEntity, Long> {

    Optional<ObjetivoNutricionalEntity> findFirstByUsuarioIdAndVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(
            Long usuarioId, LocalDate fecha);

    List<ObjetivoNutricionalEntity> findByUsuarioIdOrderByVigenteDesdeDesc(Long usuarioId);

    boolean existsByUsuarioIdAndVigenteDesde(Long usuarioId, LocalDate vigenteDesde);
}
