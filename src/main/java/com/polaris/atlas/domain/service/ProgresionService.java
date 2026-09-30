package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.in.GetProgresionInterface;
import com.polaris.atlas.application.out.EjercicioRepositoryPort;
import com.polaris.atlas.application.out.ProgresionRepositoryPort;
import com.polaris.atlas.domain.model.EjercicioNotFoundException;
import com.polaris.atlas.domain.model.ProgresionFilter;
import com.polaris.atlas.domain.model.ProgresionSesion;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.List;

/**
 * Progresion de un ejercicio: volumen total (suma de reps por peso) por sesion,
 * calculado en la base. Ver docs/decisiones/026-progresion-y-records-por-volumen.md.
 *
 * <p>El ejercicio tiene que ser visible para el usuario (catalogo o suyo). Si no
 * existe o es propio de otro, 404 sin distinguir: es el recurso sobre el que se
 * consulta, como en {@code GET /ejercicio/{id}}. El usuario del JWT filtra las
 * series: nadie ve la progresion de otro.
 */
@Service
@RequiredArgsConstructor
public class ProgresionService implements GetProgresionInterface {

    private static final int ESCALA = 2;

    private final ProgresionRepositoryPort repository;
    private final EjercicioRepositoryPort ejercicioRepository;

    @Override
    public List<ProgresionSesion> get(Long usuarioId, ProgresionFilter filter) {
        if (filter == null || filter.getEjercicioId() == null) {
            throw new ValidationException("El ejercicio es obligatorio");
        }
        if (filter.getDesde() != null && filter.getHasta() != null && filter.getDesde().isAfter(filter.getHasta())) {
            throw new ValidationException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        Long ejercicioId = filter.getEjercicioId();
        ejercicioRepository.findById(ejercicioId)
                .filter(e -> !e.isPropio() || e.perteneceA(usuarioId))
                .orElseThrow(() -> new EjercicioNotFoundException(ejercicioId));

        return repository.findProgresion(usuarioId, filter).stream()
                .map(ProgresionService::conEscala)
                .toList();
    }

    /** Los BigDecimal salen siempre con dos decimales, vengan como vengan de la base. */
    private static ProgresionSesion conEscala(ProgresionSesion sesion) {
        return ProgresionSesion.builder()
                .sesionId(sesion.getSesionId())
                .fecha(sesion.getFecha())
                .volumen(sesion.getVolumen().setScale(ESCALA, RoundingMode.HALF_UP))
                .numeroSeries(sesion.getNumeroSeries())
                .pesoMaximo(sesion.getPesoMaximo().setScale(ESCALA, RoundingMode.HALF_UP))
                .repsTotales(sesion.getRepsTotales())
                .build();
    }
}
