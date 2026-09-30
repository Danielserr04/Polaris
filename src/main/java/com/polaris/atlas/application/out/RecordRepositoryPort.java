package com.polaris.atlas.application.out;

import com.polaris.atlas.domain.model.MejorPesoEjercicio;
import com.polaris.atlas.domain.model.VolumenSesionEjercicio;

import java.util.List;

/**
 * Las dos agregaciones de las que salen los records. Ambas se calculan en la
 * base y solo cuentan series del usuario; el dominio las une.
 */
public interface RecordRepositoryPort {

    /**
     * Por cada ejercicio con series, sus series de peso maximo agrupadas por
     * reps (una fila por reps distintas a ese peso, con la primera fecha en que
     * se hicieron). Ordenadas por nombre de ejercicio. El dominio se queda con
     * la de mas reps.
     */
    List<MejorPesoEjercicio> findMejorPesoPorEjercicio(Long usuarioId);

    /** Una fila por ejercicio y sesion: volumen de ese ejercicio en esa sesion, por fecha ascendente. */
    List<VolumenSesionEjercicio> findVolumenPorEjercicioYSesion(Long usuarioId);
}
