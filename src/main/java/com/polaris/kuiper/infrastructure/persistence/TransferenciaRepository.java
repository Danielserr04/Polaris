package com.polaris.kuiper.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data. Solo la usa TransferenciaJpaAdapter.
 */
public interface TransferenciaRepository extends JpaRepository<TransferenciaEntity, Long>,
        JpaSpecificationExecutor<TransferenciaEntity> {

    /** Entra por idx_transferencia_usuario_fecha. */
    @Query("select t.cuentaDestino.id as cuentaId, sum(t.importe) as total from TransferenciaEntity t "
            + "where t.usuarioId = :usuarioId group by t.cuentaDestino.id")
    List<SumaPorCuenta> sumarEntrantesPorCuenta(@Param("usuarioId") Long usuarioId);

    @Query("select t.cuentaOrigen.id as cuentaId, sum(t.importe) as total from TransferenciaEntity t "
            + "where t.usuarioId = :usuarioId group by t.cuentaOrigen.id")
    List<SumaPorCuenta> sumarSalientesPorCuenta(@Param("usuarioId") Long usuarioId);

    boolean existsByCuentaOrigen_IdOrCuentaDestino_Id(Long cuentaOrigenId, Long cuentaDestinoId);
}
