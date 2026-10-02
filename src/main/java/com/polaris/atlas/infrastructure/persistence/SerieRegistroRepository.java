package com.polaris.atlas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data. Solo la usan SerieRegistroJpaAdapter (comprobacion de uso de un
 * ejercicio), ProgresionJpaAdapter, RecordJpaAdapter, TrabajoMuscularJpaAdapter y
 * EstadisticasEntrenoJpaAdapter (agregaciones para los logros). Las series se escriben siempre a
 * traves de SesionEntity.
 *
 * <p>Las agregaciones (SUM, MAX, COUNT, GROUP BY) se hacen en la base y
 * devuelven proyecciones, nunca las series. Todas filtran por usuario_id: el
 * aislamiento entre usuarios. Ver docs/decisiones/026-progresion-y-records-por-volumen.md.
 */
public interface SerieRegistroRepository extends JpaRepository<SerieRegistroEntity, Long> {

    boolean existsByEjercicio_Id(Long ejercicioId);

    /**
     * Una fila por sesion con series del ejercicio. El volumen es la suma de
     * reps por peso (un peso 0 aporta 0). Usa el indice
     * (ejercicio_id, sesion_id) y el join a sesion por clave primaria.
     */
    @Query("select se.id as sesionId, se.fecha as fecha, "
            + "sum(s.reps * s.pesoKg) as volumen, count(s) as numeroSeries, "
            + "max(s.pesoKg) as pesoMaximo, sum(s.reps) as repsTotales "
            + "from SerieRegistroEntity s join s.sesion se "
            + "where s.usuarioId = :usuarioId and s.ejercicio.id = :ejercicioId "
            + "and (:desde is null or se.fecha >= :desde) "
            + "and (:hasta is null or se.fecha <= :hasta) "
            + "group by se.id, se.fecha "
            + "order by se.fecha asc, se.id asc")
    List<ProgresionFila> findProgresion(@Param("usuarioId") Long usuarioId,
                                        @Param("ejercicioId") Long ejercicioId,
                                        @Param("desde") LocalDate desde,
                                        @Param("hasta") LocalDate hasta);

    /**
     * Las series del peso maximo de cada ejercicio, agrupadas por reps y con la
     * primera fecha de cada grupo: pocas filas por ejercicio (las reps distintas
     * que se hicieron a ese peso), no una por serie. El peso maximo sale de una
     * tabla derivada (HQL) que agrega una sola vez; una subconsulta correlacionada
     * (peso = max(...) por cada fila) se evaluaba una vez por serie y tardaba
     * decenas de segundos con 36.000 series. Elegir la de mas reps es cosa del
     * dominio.
     */
    @Query("select e.id as ejercicioId, e.nombre as ejercicioNombre, e.grupoMuscular as ejercicioGrupoMuscular, "
            + "s.pesoKg as pesoKg, s.reps as reps, min(se.fecha) as fecha "
            + "from SerieRegistroEntity s join s.ejercicio e join s.sesion se "
            + "join (select s2.ejercicio.id as ejercicioId, max(s2.pesoKg) as pesoMaximo "
            + "from SerieRegistroEntity s2 where s2.usuarioId = :usuarioId group by s2.ejercicio.id) m "
            + "on m.ejercicioId = e.id and m.pesoMaximo = s.pesoKg "
            + "where s.usuarioId = :usuarioId "
            + "group by e.id, e.nombre, e.grupoMuscular, s.pesoKg, s.reps "
            + "order by e.nombre asc, e.id asc, s.reps desc")
    List<MejorPesoFila> findMejorPesoPorEjercicio(@Param("usuarioId") Long usuarioId);

    /** Una fila por ejercicio y sesion. Usa el indice (ejercicio_id, sesion_id). */
    @Query("select s.ejercicio.id as ejercicioId, se.id as sesionId, se.fecha as fecha, "
            + "sum(s.reps * s.pesoKg) as volumen "
            + "from SerieRegistroEntity s join s.sesion se "
            + "where s.usuarioId = :usuarioId "
            + "group by s.ejercicio.id, se.id, se.fecha "
            + "order by se.fecha asc, se.id asc")
    List<VolumenSesionFila> findVolumenPorEjercicioYSesion(@Param("usuarioId") Long usuarioId);

    /**
     * Una fila por grupo muscular con series del usuario en el rango. Las
     * series de un ejercicio sin grupo no cuentan.
     */
    @Query("select e.grupoMuscular as grupoMuscular, count(s) as numeroSeries, "
            + "count(distinct se.id) as numeroSesiones, sum(s.reps * s.pesoKg) as volumen "
            + "from SerieRegistroEntity s join s.ejercicio e join s.sesion se "
            + "where s.usuarioId = :usuarioId and e.grupoMuscular is not null "
            + "and (:desde is null or se.fecha >= :desde) "
            + "and (:hasta is null or se.fecha <= :hasta) "
            + "group by e.grupoMuscular")
    List<TrabajoMuscularFila> findTrabajoMuscular(@Param("usuarioId") Long usuarioId,
                                                  @Param("desde") LocalDate desde,
                                                  @Param("hasta") LocalDate hasta);

    /** El primer dia en que se hizo cada ejercicio: una fecha por ejercicio distinto, para los logros. */
    @Query("select min(se.fecha) from SerieRegistroEntity s join s.sesion se "
            + "where s.usuarioId = :usuarioId "
            + "group by s.ejercicio.id")
    List<LocalDate> findPrimerUsoPorEjercicio(@Param("usuarioId") Long usuarioId);

    /** El volumen de cada sesion con su fecha, para las toneladas de los logros. */
    @Query("select se.fecha as fecha, sum(s.reps * s.pesoKg) as volumen "
            + "from SerieRegistroEntity s join s.sesion se "
            + "where s.usuarioId = :usuarioId "
            + "group by se.id, se.fecha")
    List<VolumenFechaFila> findVolumenPorSesion(@Param("usuarioId") Long usuarioId);

    interface ProgresionFila {
        Long getSesionId();

        LocalDate getFecha();

        BigDecimal getVolumen();

        Long getNumeroSeries();

        BigDecimal getPesoMaximo();

        Long getRepsTotales();
    }

    interface MejorPesoFila {
        Long getEjercicioId();

        String getEjercicioNombre();

        String getEjercicioGrupoMuscular();

        BigDecimal getPesoKg();

        Integer getReps();

        LocalDate getFecha();
    }

    interface VolumenSesionFila {
        Long getEjercicioId();

        Long getSesionId();

        LocalDate getFecha();

        BigDecimal getVolumen();
    }

    interface TrabajoMuscularFila {
        String getGrupoMuscular();

        Long getNumeroSeries();

        Long getNumeroSesiones();

        BigDecimal getVolumen();
    }

    interface VolumenFechaFila {
        LocalDate getFecha();

        BigDecimal getVolumen();
    }
}
