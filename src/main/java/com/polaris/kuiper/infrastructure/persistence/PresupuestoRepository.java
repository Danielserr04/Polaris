package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data. La usan PresupuestoJpaAdapter y EstadisticasLogrosJpaAdapter (logros).
 */
public interface PresupuestoRepository extends JpaRepository<PresupuestoEntity, Long>,
        JpaSpecificationExecutor<PresupuestoEntity> {

    Optional<PresupuestoEntity> findByUsuarioIdAndCategoria_IdAndPeriodo(Long usuarioId, Long categoriaId,
                                                                          PeriodoPresupuesto periodo);

    boolean existsByCategoria_Id(Long categoriaId);

    List<PresupuestoEntity> findByPeriodoOrderByIdAsc(PeriodoPresupuesto periodo);

    long countByUsuarioId(Long usuarioId);
}
