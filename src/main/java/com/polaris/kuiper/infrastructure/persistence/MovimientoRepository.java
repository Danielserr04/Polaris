package com.polaris.kuiper.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data. Solo la usa MovimientoJpaAdapter.
 *
 * <p>Los {@code @Modifying} llevan su propio {@code @Transactional}: los
 * metodos de consulta heredan uno de solo lectura y un UPDATE o DELETE ahi
 * fallaria. Cada uno es una sola sentencia, asi que es atomico por si mismo.
 */
public interface MovimientoRepository extends JpaRepository<MovimientoEntity, Long>,
        JpaSpecificationExecutor<MovimientoEntity> {

    boolean existsByCategoria_IdAndBorradoEnIsNull(Long categoriaId);

    Optional<MovimientoEntity> findByIdAndBorradoEnIsNull(Long id);

    Optional<MovimientoEntity> findByIdAndBorradoEnIsNotNull(Long id);

    @EntityGraph(attributePaths = "categoria")
    List<MovimientoEntity> findByUsuarioIdAndBorradoEnIsNotNullOrderByBorradoEnDescIdDesc(Long usuarioId);

    @Transactional
    @Modifying
    @Query("update MovimientoEntity m set m.borradoEn = :borradoEn "
            + "where m.usuarioId = :usuarioId and m.id in :ids and m.borradoEn is null")
    int moverAPapelera(@Param("usuarioId") Long usuarioId, @Param("ids") List<Long> ids,
                       @Param("borradoEn") LocalDateTime borradoEn);

    @Transactional
    @Modifying
    @Query("delete from MovimientoEntity m where m.usuarioId = :usuarioId and m.borradoEn is not null")
    int vaciarPapelera(@Param("usuarioId") Long usuarioId);

    @Transactional
    @Modifying
    @Query("delete from MovimientoEntity m where m.borradoEn < :limite")
    int purgarPapelera(@Param("limite") LocalDateTime limite);

    @Transactional
    @Modifying
    @Query("delete from MovimientoEntity m where m.categoria.id = :categoriaId and m.borradoEn is not null")
    int deleteEnPapeleraByCategoriaId(@Param("categoriaId") Long categoriaId);

    /** Solo fuera de la papelera: lo borrado no cuenta para el resumen mensual. */
    @Query("select distinct m.usuarioId from MovimientoEntity m "
            + "where m.fecha between :desde and :hasta and m.borradoEn is null")
    List<Long> findUsuarioIdsConMovimientos(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
