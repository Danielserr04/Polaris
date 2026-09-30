package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.in.ListRecordsInterface;
import com.polaris.atlas.application.out.RecordRepositoryPort;
import com.polaris.atlas.domain.model.MejorPesoEjercicio;
import com.polaris.atlas.domain.model.RecordEjercicio;
import com.polaris.atlas.domain.model.VolumenSesionEjercicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mejores marcas por ejercicio. Un record es (a) el mayor peso en una serie y
 * (b) el mayor volumen en una sola sesion. Ver
 * docs/decisiones/026-progresion-y-records-por-volumen.md.
 *
 * <p>La base agrega (las series del peso maximo agrupadas por reps, y una fila
 * por ejercicio y sesion para el volumen); aqui solo se une y se elige, por
 * ejercicio, la serie de mas reps y la sesion de mayor volumen: JPQL no admite
 * {@code max(sum(...))}. Son tantas filas como reps distintas al peso maximo y
 * como sesiones con ese ejercicio, no como series.
 */
@Service
@RequiredArgsConstructor
public class RecordService implements ListRecordsInterface {

    private static final int ESCALA = 2;

    private final RecordRepositoryPort repository;

    @Override
    public List<RecordEjercicio> list(Long usuarioId) {
        Collection<MejorPesoEjercicio> mejoresPesos = mejorPesoPorEjercicio(
                repository.findMejorPesoPorEjercicio(usuarioId));
        if (mejoresPesos.isEmpty()) {
            return List.of();
        }
        Map<Long, VolumenSesionEjercicio> mejorVolumen = mejorVolumenPorEjercicio(
                repository.findVolumenPorEjercicioYSesion(usuarioId));

        return mejoresPesos.stream()
                .map(peso -> registro(peso, mejorVolumen.get(peso.getEjercicioId())))
                .toList();
    }

    /**
     * Llegan las series del peso maximo agrupadas por reps: se queda la de mas
     * reps de cada ejercicio (a igual peso, mas reps es la mejor serie). Cada
     * grupo ya trae la primera fecha en que se hizo. Se conserva el orden de
     * llegada (por nombre de ejercicio).
     */
    private static Collection<MejorPesoEjercicio> mejorPesoPorEjercicio(List<MejorPesoEjercicio> filas) {
        Map<Long, MejorPesoEjercicio> mejores = new LinkedHashMap<>();
        for (MejorPesoEjercicio fila : filas) {
            mejores.merge(fila.getEjercicioId(), fila,
                    (actual, nueva) -> nueva.getReps() > actual.getReps() ? nueva : actual);
        }
        return mejores.values();
    }

    /**
     * Las filas llegan por fecha ascendente: con un maximo empatado gana la
     * primera, que es cuando se logro por primera vez.
     */
    private static Map<Long, VolumenSesionEjercicio> mejorVolumenPorEjercicio(List<VolumenSesionEjercicio> filas) {
        Map<Long, VolumenSesionEjercicio> mejores = new HashMap<>();
        for (VolumenSesionEjercicio fila : filas) {
            mejores.merge(fila.getEjercicioId(), fila,
                    (actual, nueva) -> nueva.getVolumen().compareTo(actual.getVolumen()) > 0 ? nueva : actual);
        }
        return mejores;
    }

    private static RecordEjercicio registro(MejorPesoEjercicio peso, VolumenSesionEjercicio volumen) {
        // Un volumen maximo de 0 (solo peso corporal) no es una marca: se omite.
        boolean hayVolumen = volumen != null && volumen.getVolumen().signum() > 0;
        return RecordEjercicio.builder()
                .ejercicioId(peso.getEjercicioId())
                .ejercicioNombre(peso.getEjercicioNombre())
                .ejercicioGrupoMuscular(peso.getEjercicioGrupoMuscular())
                .pesoMaximo(peso.getPesoKg().setScale(ESCALA, RoundingMode.HALF_UP))
                .repsPesoMaximo(peso.getReps())
                .fechaPesoMaximo(peso.getFecha())
                .volumenMaximoSesion(hayVolumen ? volumen.getVolumen().setScale(ESCALA, RoundingMode.HALF_UP) : null)
                .fechaVolumenMaximo(hayVolumen ? volumen.getFecha() : null)
                .build();
    }
}
