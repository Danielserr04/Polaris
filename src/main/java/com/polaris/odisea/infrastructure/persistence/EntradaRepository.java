package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.domain.model.EstadoEntrada;
import com.polaris.odisea.domain.model.TipoContenido;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data. La usan EntradaJpaAdapter y EstadisticasLogrosJpaAdapter (logros).
 */
public interface EntradaRepository extends JpaRepository<EntradaEntity, Long>,
        JpaSpecificationExecutor<EntradaEntity> {

    /**
     * El listado con el titulo en la misma sentencia (JOIN). Sin esto, el titulo EAGER de cada
     * entrada se pedia con una sentencia aparte: N+1 (14.001 sentencias con 14.000 entradas).
     */
    @Override
    @EntityGraph(attributePaths = "titulo")
    List<EntradaEntity> findAll(Specification<EntradaEntity> spec);

    boolean existsByTitulo_Id(Long tituloId);

    boolean existsByUsuarioIdAndTitulo_Id(Long usuarioId, Long tituloId);

    /** Tipo y fecha de fin de cada entrada en un estado, para los logros. La fecha puede ser nula. */
    @Query("select t.tipo as tipo, e.fechaFin as fechaFin from EntradaEntity e join e.titulo t "
            + "where e.usuarioId = :usuarioId and e.estado = :estado")
    List<TipoFechaFila> findTipoYFechaFin(@Param("usuarioId") Long usuarioId, @Param("estado") EstadoEntrada estado);

    /** La fecha de fin (o nula) de cada entrada valorada, para los logros. */
    @Query("select e.fechaFin from EntradaEntity e where e.usuarioId = :usuarioId and e.valoracion is not null")
    List<LocalDate> findFechaFinValoradas(@Param("usuarioId") Long usuarioId);

    interface TipoFechaFila {
        TipoContenido getTipo();

        LocalDate getFechaFin();
    }
}
