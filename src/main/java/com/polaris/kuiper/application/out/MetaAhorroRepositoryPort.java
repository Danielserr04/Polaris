package com.polaris.kuiper.application.out;

import com.polaris.kuiper.domain.model.AportacionMeta;
import com.polaris.kuiper.domain.model.MetaAhorro;
import com.polaris.kuiper.domain.model.MetaAhorroFilter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de MetaAhorro y AportacionMeta, nunca de sus Entity.
 *
 * <p>Las aportaciones van por este mismo puerto porque solo existen dentro de
 * su meta. Toda MetaAhorro que sale de aqui trae {@code importeActual} (la
 * suma de sus aportaciones) ya relleno.
 */
public interface MetaAhorroRepositoryPort {

    MetaAhorro save(MetaAhorro meta);

    Optional<MetaAhorro> findById(Long id);

    /** Por fecha limite (las que no tienen, al final) y despues por nombre. */
    List<MetaAhorro> findAll(Long usuarioId, MetaAhorroFilter filter);

    /** Borra tambien sus aportaciones (ON DELETE CASCADE). */
    void deleteById(Long id);

    /** Para comprobar el nombre unico por usuario antes de guardar. */
    Optional<MetaAhorro> findByUsuarioIdAndNombre(Long usuarioId, String nombre);

    AportacionMeta saveAportacion(AportacionMeta aportacion);

    Optional<AportacionMeta> findAportacionById(Long id);

    /** De la mas reciente a la mas antigua. */
    List<AportacionMeta> findAportaciones(Long metaId);

    void deleteAportacionById(Long id);

    /** Lo ahorrado: la suma de las aportaciones de la meta, 0 si no tiene. */
    BigDecimal sumaAportaciones(Long metaId);
}
