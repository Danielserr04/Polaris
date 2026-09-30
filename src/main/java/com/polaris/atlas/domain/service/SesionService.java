package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.in.CreateSesionInterface;
import com.polaris.atlas.application.in.DeleteSesionInterface;
import com.polaris.atlas.application.in.GetSesionInterface;
import com.polaris.atlas.application.in.ListSesionInterface;
import com.polaris.atlas.application.in.UpdateSesionInterface;
import com.polaris.atlas.application.out.EjercicioRepositoryPort;
import com.polaris.atlas.application.out.RutinaRepositoryPort;
import com.polaris.atlas.application.out.SesionRepositoryPort;
import com.polaris.atlas.domain.model.SerieRegistro;
import com.polaris.atlas.domain.model.Sesion;
import com.polaris.atlas.domain.model.SesionFilter;
import com.polaris.atlas.domain.model.SesionNotFoundException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sesion y sus series son un solo agregado: se crean y editan juntas. Ver
 * docs/decisiones/025-sesion-agregado-con-series.md.
 *
 * <p>El usuario de la sesion y de cada serie sale del JWT, nunca del cliente.
 * Cada ejercicio tiene que ser visible para el usuario (catalogo o suyo) y la
 * rutina, si viene, tiene que ser suya: en ambos casos 400 sin distinguir "no
 * existe" de "es de otro". Las reglas de las series (rangos, unicidad de
 * numeroSerie, tope) se aplican aqui aunque el DTO ya filtre lo obvio: el
 * servicio es quien las garantiza.
 */
@Service
@RequiredArgsConstructor
public class SesionService implements
        CreateSesionInterface,
        GetSesionInterface,
        ListSesionInterface,
        UpdateSesionInterface,
        DeleteSesionInterface {

    static final int MAX_SERIES = 200;
    static final int MAX_NUMERO_SERIE = 999;
    static final int MAX_REPS = 999;
    static final int MAX_DURACION_MIN = 1440;
    /** Sobra para cualquier prensa; la columna admite hasta 9999.99. */
    static final BigDecimal MAX_PESO_KG = new BigDecimal("1000");
    static final BigDecimal RPE_MIN = BigDecimal.ONE;
    static final BigDecimal RPE_MAX = BigDecimal.TEN;

    private static final BigDecimal DOS = BigDecimal.valueOf(2);

    private final SesionRepositoryPort repository;
    private final EjercicioRepositoryPort ejercicioRepository;
    private final RutinaRepositoryPort rutinaRepository;

    @Override
    public Sesion create(Long usuarioId, Sesion sesion) {
        validarYPreparar(usuarioId, sesion);
        sesion.setId(null);
        sesion.setUsuarioId(usuarioId);
        return repository.save(sesion);
    }

    @Override
    public Sesion get(Long usuarioId, Long id) {
        return getPropia(usuarioId, id);
    }

    @Override
    public List<Sesion> list(Long usuarioId, SesionFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    /** Reemplazo completo: el conjunto de series que llega sustituye al anterior. */
    @Override
    public Sesion update(Long usuarioId, Long id, Sesion sesion) {
        Sesion existente = getPropia(usuarioId, id);
        validarYPreparar(usuarioId, sesion);
        sesion.setId(existente.getId());
        sesion.setUsuarioId(existente.getUsuarioId());
        return repository.save(sesion);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropia(usuarioId, id);
        repository.deleteById(id);
    }

    /**
     * Validaciones, todas con 400 y en este orden: cabecera de la sesion (fecha,
     * duracion), estructura y rangos de las series, ejercicios visibles, rutina
     * propia. Deja las series sin id (nunca vienen del cliente), con el usuario
     * fijado y agrupadas por ejercicio en el orden en que aparecen, con
     * numeroSerie ascendente dentro de cada uno: el repositorio las guarda en
     * ese orden y las lecturas las devuelven igual.
     */
    private void validarYPreparar(Long usuarioId, Sesion sesion) {
        validarCabecera(sesion);
        validarSeries(sesion.getSeries());

        List<SerieRegistro> series = sesion.getSeries();
        for (Long ejercicioId : ejerciciosDistintos(series)) {
            comprobarEjercicioVisible(usuarioId, ejercicioId);
        }
        comprobarRutinaPropia(usuarioId, sesion.getRutinaId());

        for (SerieRegistro serie : series) {
            serie.setId(null);
            serie.setUsuarioId(usuarioId);
            serie.setPesoKg(serie.getPesoKg().setScale(2, RoundingMode.UNNECESSARY));
            if (serie.getRpe() != null) {
                serie.setRpe(serie.getRpe().setScale(1, RoundingMode.UNNECESSARY));
            }
        }
        sesion.setSeries(agrupadasPorEjercicio(series));
    }

    /** Una sesion registra lo que ya ocurrio: la fecha no puede ser futura (la de hoy vale). */
    private void validarCabecera(Sesion sesion) {
        if (sesion.getFecha() == null) {
            throw new ValidationException("La fecha de la sesion es obligatoria");
        }
        if (sesion.getFecha().isAfter(LocalDate.now())) {
            throw new ValidationException("La fecha de la sesion no puede ser futura");
        }
        Integer duracion = sesion.getDuracionMin();
        if (duracion != null && (duracion < 1 || duracion > MAX_DURACION_MIN)) {
            throw new ValidationException("La duracion debe estar entre 1 y " + MAX_DURACION_MIN + " minutos");
        }
    }

    private void validarSeries(List<SerieRegistro> series) {
        if (series == null || series.isEmpty()) {
            throw new ValidationException("Una sesion necesita al menos una serie");
        }
        if (series.size() > MAX_SERIES) {
            throw new ValidationException("Una sesion admite como mucho " + MAX_SERIES + " series");
        }
        Map<Long, Set<Integer>> numerosPorEjercicio = new LinkedHashMap<>();
        for (SerieRegistro serie : series) {
            validarSerie(serie);
            boolean nueva = numerosPorEjercicio
                    .computeIfAbsent(serie.getEjercicioId(), id -> new HashSet<>())
                    .add(serie.getNumeroSerie());
            if (!nueva) {
                throw new ValidationException("El numero de serie " + serie.getNumeroSerie()
                        + " esta repetido en el ejercicio " + serie.getEjercicioId());
            }
        }
    }

    private void validarSerie(SerieRegistro serie) {
        if (serie == null || serie.getEjercicioId() == null) {
            throw new ValidationException("Cada serie necesita un ejercicio");
        }
        String de = "La serie " + serie.getNumeroSerie() + " del ejercicio " + serie.getEjercicioId();

        Integer numero = serie.getNumeroSerie();
        if (numero == null || numero < 1 || numero > MAX_NUMERO_SERIE) {
            throw new ValidationException("Cada serie necesita un numero de serie entre 1 y " + MAX_NUMERO_SERIE
                    + " (ejercicio " + serie.getEjercicioId() + ")");
        }
        Integer reps = serie.getReps();
        if (reps == null || reps < 1 || reps > MAX_REPS) {
            throw new ValidationException(de + " necesita entre 1 y " + MAX_REPS + " repeticiones");
        }
        validarPeso(serie.getPesoKg(), de);
        validarRpe(serie.getRpe(), de);
    }

    private void validarPeso(BigDecimal peso, String de) {
        if (peso == null) {
            throw new ValidationException(de + " necesita un peso (0 si es con el peso corporal)");
        }
        if (peso.signum() < 0 || peso.compareTo(MAX_PESO_KG) > 0) {
            throw new ValidationException(de + " necesita un peso entre 0 y " + MAX_PESO_KG.toPlainString() + " kg");
        }
        if (peso.stripTrailingZeros().scale() > 2) {
            throw new ValidationException(de + " admite como mucho 2 decimales en el peso");
        }
    }

    /** Opcional; si viene, de 1 a 10 en pasos de 0.5. */
    private void validarRpe(BigDecimal rpe, String de) {
        if (rpe == null) {
            return;
        }
        if (rpe.compareTo(RPE_MIN) < 0 || rpe.compareTo(RPE_MAX) > 0) {
            throw new ValidationException(de + " necesita un RPE entre 1 y 10");
        }
        if (rpe.multiply(DOS).stripTrailingZeros().scale() > 0) {
            throw new ValidationException(de + " necesita un RPE en pasos de 0.5");
        }
    }

    private static Set<Long> ejerciciosDistintos(List<SerieRegistro> series) {
        Set<Long> ids = new java.util.LinkedHashSet<>();
        for (SerieRegistro serie : series) {
            ids.add(serie.getEjercicioId());
        }
        return ids;
    }

    private static List<SerieRegistro> agrupadasPorEjercicio(List<SerieRegistro> series) {
        Map<Long, List<SerieRegistro>> porEjercicio = new LinkedHashMap<>();
        for (SerieRegistro serie : series) {
            porEjercicio.computeIfAbsent(serie.getEjercicioId(), id -> new ArrayList<>()).add(serie);
        }
        List<SerieRegistro> ordenadas = new ArrayList<>(series.size());
        for (List<SerieRegistro> grupo : porEjercicio.values()) {
            grupo.sort(Comparator.comparing(SerieRegistro::getNumeroSerie));
            ordenadas.addAll(grupo);
        }
        return ordenadas;
    }

    /**
     * El ejercicio tiene que ser del catalogo o del propio usuario. Un ejercicio
     * que no existe y uno propio de otro usuario dan el mismo mensaje: no se
     * confirma que ese id exista.
     */
    private void comprobarEjercicioVisible(Long usuarioId, Long ejercicioId) {
        boolean visible = ejercicioRepository.findById(ejercicioId)
                .filter(e -> !e.isPropio() || e.perteneceA(usuarioId))
                .isPresent();
        if (!visible) {
            throw new ValidationException("El ejercicio " + ejercicioId + " no existe o no esta disponible para ti");
        }
    }

    /**
     * La rutina es opcional (un entreno libre es valido); si viene tiene que ser
     * del usuario. Da el mismo 400 si no existe que si es de otro. Una rutina
     * inactiva vale: se puede registrar un entreno hecho con ella.
     */
    private void comprobarRutinaPropia(Long usuarioId, Long rutinaId) {
        if (rutinaId == null) {
            return;
        }
        boolean propia = rutinaRepository.findById(rutinaId)
                .filter(r -> r.getUsuarioId().equals(usuarioId))
                .isPresent();
        if (!propia) {
            throw new ValidationException("La rutina " + rutinaId + " no existe o no esta disponible para ti");
        }
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private Sesion getPropia(Long usuarioId, Long id) {
        Sesion sesion = repository.findById(id)
                .orElseThrow(() -> new SesionNotFoundException(id));

        if (!sesion.getUsuarioId().equals(usuarioId)) {
            throw new SesionNotFoundException(id);
        }

        return sesion;
    }
}
