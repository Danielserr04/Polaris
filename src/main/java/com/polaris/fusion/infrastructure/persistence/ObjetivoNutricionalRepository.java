package com.polaris.fusion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data. La usan ObjetivoNutricionalJpaAdapter y EstadisticasLogrosJpaAdapter (logros). Sin
 * JpaSpecificationExecutor: no hay filtros dinamicos, solo consultas derivadas.
 */
public interface ObjetivoNutricionalRepository extends JpaRepository<ObjetivoNutricionalEntity, Long> {

    Optional<ObjetivoNutricionalEntity> findFirstByUsuarioIdAndVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(
            Long usuarioId, LocalDate fecha);

    List<ObjetivoNutricionalEntity> findByUsuarioIdOrderByVigenteDesdeDesc(Long usuarioId);

    boolean existsByUsuarioIdAndVigenteDesde(Long usuarioId, LocalDate vigenteDesde);

    /** El primer dia con objetivo, o nulo si nunca ha tenido. */
    @Query("select min(o.vigenteDesde) from ObjetivoNutricionalEntity o where o.usuarioId = :usuarioId")
    LocalDate findPrimerVigenteDesde(@Param("usuarioId") Long usuarioId);
}
