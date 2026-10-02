package com.polaris.kuiper.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data. Solo la usa MovimientoJpaAdapter.
 */
public interface MovimientoRepository extends JpaRepository<MovimientoEntity, Long>,
        JpaSpecificationExecutor<MovimientoEntity> {

    boolean existsByCategoria_Id(Long categoriaId);

    @Query("select distinct m.usuarioId from MovimientoEntity m where m.fecha between :desde and :hasta")
    List<Long> findUsuarioIdsConMovimientos(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
