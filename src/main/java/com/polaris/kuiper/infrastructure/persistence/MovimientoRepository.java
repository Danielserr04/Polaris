package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data. Solo la usa MovimientoJpaAdapter.
 */
public interface MovimientoRepository extends JpaRepository<MovimientoEntity, Long>,
        JpaSpecificationExecutor<MovimientoEntity> {

    boolean existsByCategoria_Id(Long categoriaId);

    boolean existsByCuenta_Id(Long cuentaId);

    /** Un SUM por cuenta para el saldo actual. Los movimientos sin cuenta no entran. */
    @Query("select m.cuenta.id as cuentaId, sum(m.importe) as total from MovimientoEntity m "
            + "where m.usuarioId = :usuarioId and m.tipo = :tipo and m.cuenta is not null group by m.cuenta.id")
    List<SumaPorCuenta> sumarPorCuenta(@Param("usuarioId") Long usuarioId, @Param("tipo") TipoMovimiento tipo);
}
