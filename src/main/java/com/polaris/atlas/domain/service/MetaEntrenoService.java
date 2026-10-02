package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.in.CreateMetaEntrenoInterface;
import com.polaris.atlas.application.in.DeleteMetaEntrenoInterface;
import com.polaris.atlas.application.in.GetMetaEntrenoInterface;
import com.polaris.atlas.application.in.ListMetaEntrenoInterface;
import com.polaris.atlas.application.in.UpdateMetaEntrenoInterface;
import com.polaris.atlas.application.out.EjercicioRepositoryPort;
import com.polaris.atlas.application.out.MetaEntrenoRepositoryPort;
import com.polaris.atlas.application.out.PesoCorporalPort;
import com.polaris.atlas.application.out.RecordRepositoryPort;
import com.polaris.atlas.application.out.SesionRepositoryPort;
import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.MejorPesoEjercicio;
import com.polaris.atlas.domain.model.MetaEntreno;
import com.polaris.atlas.domain.model.MetaEntrenoFilter;
import com.polaris.atlas.domain.model.MetaEntrenoNotFoundException;
import com.polaris.atlas.domain.model.PesoCorporal;
import com.polaris.atlas.domain.model.PesoCorporalFilter;
import com.polaris.atlas.domain.model.SesionFilter;
import com.polaris.atlas.domain.model.TipoMetaEntreno;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Metas personales de entreno. Se guarda la definicion y el punto de partida;
 * el valor actual y el progreso se calculan al leer con el ultimo peso, los
 * records de peso y las sesiones de la semana. Ver
 * docs/decisiones/043-logros-calculados-y-metas.md.
 */
@Service
@RequiredArgsConstructor
public class MetaEntrenoService implements
        CreateMetaEntrenoInterface,
        GetMetaEntrenoInterface,
        ListMetaEntrenoInterface,
        UpdateMetaEntrenoInterface,
        DeleteMetaEntrenoInterface {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final MetaEntrenoRepositoryPort repository;
    private final EjercicioRepositoryPort ejercicioRepository;
    private final PesoCorporalPort pesoCorporalPort;
    private final RecordRepositoryPort recordRepository;
    private final SesionRepositoryPort sesionRepository;

    @Override
    public MetaEntreno create(Long usuarioId, MetaEntreno meta) {
        LocalDate hoy = LocalDate.now();
        validar(usuarioId, meta, hoy);
        meta.setId(null);
        meta.setUsuarioId(usuarioId);
        meta.setCreadaEn(hoy);
        Datos datos = Datos.cargar(this, usuarioId, hoy);
        meta.setValorInicial(valorActual(meta, datos));
        return enriquecer(repository.save(meta), datos);
    }

    @Override
    public MetaEntreno get(Long usuarioId, Long id) {
        return enriquecer(getPropia(usuarioId, id), Datos.cargar(this, usuarioId, LocalDate.now()));
    }

    @Override
    public List<MetaEntreno> list(Long usuarioId, MetaEntrenoFilter filter) {
        List<MetaEntreno> metas = repository.findAll(usuarioId, filter);
        if (metas.isEmpty()) {
            return metas;
        }
        Datos datos = Datos.cargar(this, usuarioId, LocalDate.now());
        return metas.stream().map(m -> enriquecer(m, datos)).toList();
    }

    /**
     * Conserva la fecha de creacion y el punto de partida, salvo que cambie lo
     * que se mide (tipo o ejercicio): entonces se parte del valor de hoy.
     */
    @Override
    public MetaEntreno update(Long usuarioId, Long id, MetaEntreno meta) {
        MetaEntreno existente = getPropia(usuarioId, id);
        LocalDate hoy = LocalDate.now();
        validar(usuarioId, meta, hoy);
        meta.setId(existente.getId());
        meta.setUsuarioId(existente.getUsuarioId());
        meta.setCreadaEn(existente.getCreadaEn());
        boolean mismaMedida = meta.getTipo() == existente.getTipo()
                && Objects.equals(meta.getEjercicioId(), existente.getEjercicioId());
        Datos datos = Datos.cargar(this, usuarioId, hoy);
        meta.setValorInicial(mismaMedida ? existente.getValorInicial() : valorActual(meta, datos));
        return enriquecer(repository.save(meta), datos);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropia(usuarioId, id);
        repository.deleteById(id);
    }

    private void validar(Long usuarioId, MetaEntreno meta, LocalDate hoy) {
        if (meta.getTipo() == TipoMetaEntreno.MARCA_EJERCICIO) {
            if (meta.getEjercicioId() == null) {
                throw new ValidationException("Elige el ejercicio de la marca");
            }
            ejercicioRepository.findById(meta.getEjercicioId())
                    .filter(e -> !e.isPropio() || e.perteneceA(usuarioId))
                    .orElseThrow(() -> new ValidationException("El ejercicio no existe"));
        } else {
            meta.setEjercicioId(null);
        }
        if (meta.getTipo() == TipoMetaEntreno.SESIONES_SEMANA) {
            BigDecimal v = meta.getValorObjetivo();
            if (v.stripTrailingZeros().scale() > 0 || v.compareTo(BigDecimal.ONE) < 0 || v.compareTo(BigDecimal.valueOf(7)) > 0) {
                throw new ValidationException("Las sesiones por semana van de 1 a 7");
            }
        }
        if (meta.getFechaLimite() != null && meta.getFechaLimite().isBefore(hoy)) {
            throw new ValidationException("La fecha limite no puede ser pasada");
        }
    }

    /** Rellena lo calculado: valor actual, progreso de 0 a 100 y si esta conseguida. */
    private MetaEntreno enriquecer(MetaEntreno meta, Datos datos) {
        BigDecimal actual = valorActual(meta, datos);
        BigDecimal objetivo = meta.getValorObjetivo();
        meta.setValorActual(actual);
        meta.setEjercicioNombre(meta.getEjercicioId() != null
                ? ejercicioRepository.findById(meta.getEjercicioId()).map(Ejercicio::getNombre).orElse(null)
                : null);

        if (actual == null) {
            meta.setConseguida(false);
            meta.setProgresoPct(0);
            return meta;
        }
        BigDecimal inicial = meta.getValorInicial() != null ? meta.getValorInicial() : BigDecimal.ZERO;
        if (meta.getTipo() == TipoMetaEntreno.PESO_CORPORAL) {
            inicial = meta.getValorInicial() != null ? meta.getValorInicial() : actual;
            boolean bajar = objetivo.compareTo(inicial) < 0;
            meta.setConseguida(bajar ? actual.compareTo(objetivo) <= 0 : actual.compareTo(objetivo) >= 0);
        } else {
            if (meta.getTipo() == TipoMetaEntreno.SESIONES_SEMANA) {
                inicial = BigDecimal.ZERO;
            }
            meta.setConseguida(actual.compareTo(objetivo) >= 0);
        }
        meta.setProgresoPct(meta.isConseguida() ? 100 : porcentaje(inicial, actual, objetivo));
        return meta;
    }

    /** Lo recorrido de inicial a objetivo, de 0 a 100, en cualquier sentido. */
    static int porcentaje(BigDecimal inicial, BigDecimal actual, BigDecimal objetivo) {
        BigDecimal total = objetivo.subtract(inicial);
        if (total.signum() == 0) {
            return 0;
        }
        BigDecimal hecho = actual.subtract(inicial).multiply(CIEN).divide(total, 0, RoundingMode.DOWN);
        return Math.max(0, Math.min(99, hecho.intValue()));
    }

    private static BigDecimal valorActual(MetaEntreno meta, Datos datos) {
        return switch (meta.getTipo()) {
            case PESO_CORPORAL -> datos.ultimoPeso();
            case MARCA_EJERCICIO -> datos.records().getOrDefault(meta.getEjercicioId(), BigDecimal.ZERO);
            case SESIONES_SEMANA -> BigDecimal.valueOf(datos.sesionesSemana());
        };
    }

    private MetaEntreno getPropia(Long usuarioId, Long id) {
        MetaEntreno meta = repository.findById(id).orElseThrow(() -> new MetaEntrenoNotFoundException(id));
        if (!meta.getUsuarioId().equals(usuarioId)) {
            throw new MetaEntrenoNotFoundException(id);
        }
        return meta;
    }

    /** Lo que hace falta para calcular cualquier meta, cargado una vez por peticion. */
    private record Datos(BigDecimal ultimoPeso, Map<Long, BigDecimal> records, int sesionesSemana) {

        static Datos cargar(MetaEntrenoService s, Long usuarioId, LocalDate hoy) {
            BigDecimal peso = s.pesoCorporalPort.findAll(usuarioId, new PesoCorporalFilter()).stream()
                    .findFirst().map(PesoCorporal::getPesoKg).orElse(null);
            Map<Long, BigDecimal> records = s.recordRepository.findMejorPesoPorEjercicio(usuarioId).stream()
                    .collect(Collectors.toMap(MejorPesoEjercicio::getEjercicioId, MejorPesoEjercicio::getPesoKg,
                            BigDecimal::max));
            LocalDate lunes = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            int sesiones = s.sesionRepository.findAll(usuarioId, SesionFilter.builder()
                    .desde(lunes).hasta(lunes.plusDays(6)).build()).size();
            return new Datos(peso, records, sesiones);
        }
    }
}
