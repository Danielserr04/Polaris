package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.AportarMetaAhorroInterface;
import com.polaris.kuiper.application.in.CrearNotificacionInterface;
import com.polaris.kuiper.application.in.CreateMetaAhorroInterface;
import com.polaris.kuiper.application.in.DeleteAportacionMetaInterface;
import com.polaris.kuiper.application.in.DeleteMetaAhorroInterface;
import com.polaris.kuiper.application.in.GetMetaAhorroInterface;
import com.polaris.kuiper.application.in.ListAportacionMetaInterface;
import com.polaris.kuiper.application.in.ListMetaAhorroInterface;
import com.polaris.kuiper.application.in.UpdateMetaAhorroInterface;
import com.polaris.kuiper.application.out.MetaAhorroRepositoryPort;
import com.polaris.kuiper.domain.model.AportacionMeta;
import com.polaris.kuiper.domain.model.AportacionMetaNotFoundException;
import com.polaris.kuiper.domain.model.MetaAhorro;
import com.polaris.kuiper.domain.model.MetaAhorroFilter;
import com.polaris.kuiper.domain.model.MetaAhorroNotFoundException;
import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.domain.model.TipoNotificacion;
import com.polaris.kuiper.domain.model.TextoAviso;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Metas de ahorro y sus aportaciones. Lo ahorrado es la suma de las
 * aportaciones y nunca baja de 0. Ver
 * docs/decisiones/036-meta-ahorro-con-aportaciones.md.
 */
@Service
@RequiredArgsConstructor
public class MetaAhorroService implements
        CreateMetaAhorroInterface,
        GetMetaAhorroInterface,
        ListMetaAhorroInterface,
        UpdateMetaAhorroInterface,
        DeleteMetaAhorroInterface,
        AportarMetaAhorroInterface,
        ListAportacionMetaInterface,
        DeleteAportacionMetaInterface {

    private final MetaAhorroRepositoryPort repository;
    private final CrearNotificacionInterface crearNotificacion;

    /** Empieza en 0: lo ahorrado solo entra por aportaciones. */
    @Override
    public MetaAhorro create(Long usuarioId, MetaAhorro meta) {
        comprobarNombreLibre(usuarioId, meta.getNombre(), null);
        meta.setId(null);
        meta.setUsuarioId(usuarioId);
        meta.setCreadaEn(Instant.now());
        return conPlazo(repository.save(meta));
    }

    @Override
    public MetaAhorro get(Long usuarioId, Long id) {
        return conPlazo(getPropia(usuarioId, id));
    }

    @Override
    public List<MetaAhorro> list(Long usuarioId, MetaAhorroFilter filter) {
        LocalDate hoy = LocalDate.now();
        List<MetaAhorro> metas = repository.findAll(usuarioId, filter);
        metas.forEach(m -> m.calcularPlazo(hoy));
        return metas;
    }

    /**
     * Cambia nombre, objetivo, fecha, color e icono. Las aportaciones no se
     * tocan: bajar el objetivo por debajo de lo ahorrado solo la completa.
     */
    @Override
    public MetaAhorro update(Long usuarioId, Long id, MetaAhorro meta) {
        MetaAhorro existente = getPropia(usuarioId, id);
        comprobarNombreLibre(usuarioId, meta.getNombre(), existente.getId());

        meta.setId(existente.getId());
        meta.setUsuarioId(existente.getUsuarioId());
        meta.setCreadaEn(existente.getCreadaEn());
        return conPlazo(repository.save(meta));
    }

    /** Sus aportaciones se van con ella (ON DELETE CASCADE). */
    @Override
    public void delete(Long usuarioId, Long id) {
        getPropia(usuarioId, id);
        repository.deleteById(id);
    }

    /**
     * Sin fecha, hoy. No se admiten fechas futuras ni importe 0, y una
     * retirada no puede dejar el total por debajo de 0 (400).
     */
    @Override
    public MetaAhorro aportar(Long usuarioId, Long metaId, AportacionMeta aportacion) {
        MetaAhorro meta = getPropia(usuarioId, metaId);
        boolean yaCompletada = meta.isCompletada();
        LocalDate hoy = LocalDate.now();

        if (aportacion.getImporte().signum() == 0) {
            throw new ValidationException("El importe no puede ser 0");
        }
        if (aportacion.getFecha() == null) {
            aportacion.setFecha(hoy);
        } else if (aportacion.getFecha().isAfter(hoy)) {
            throw new ValidationException("La fecha de la aportacion no puede ser futura");
        }
        if (meta.actual().add(aportacion.getImporte()).signum() < 0) {
            throw new ValidationException("No se puede retirar mas de lo ahorrado en la meta");
        }

        aportacion.setId(null);
        aportacion.setUsuarioId(usuarioId);
        aportacion.setMetaId(metaId);
        repository.saveAportacion(aportacion);

        meta.setImporteActual(repository.sumaAportaciones(metaId));
        meta.calcularPlazo(hoy);
        if (!yaCompletada && meta.isCompletada()) {
            avisarMetaAlcanzada(meta);
        }
        return meta;
    }

    /**
     * Una sola vez por meta (clave meta-{id}): si se retira y se vuelve a
     * llegar, no se repite. Ver docs/decisiones/040-notificaciones-de-kuiper.md.
     */
    private void avisarMetaAlcanzada(MetaAhorro meta) {
        crearNotificacion.crear(Notificacion.builder()
                .usuarioId(meta.getUsuarioId())
                .tipo(TipoNotificacion.META_ALCANZADA)
                .clave("meta-" + meta.getId())
                .titulo("Meta alcanzada: " + meta.getNombre())
                .texto("Has llegado a los " + TextoAviso.euros(meta.getImporteObjetivo()) + " que te propusiste.")
                .enlace(Notificacion.ENLACE_METAS)
                .build());
    }

    @Override
    public List<AportacionMeta> listAportaciones(Long usuarioId, Long metaId) {
        getPropia(usuarioId, metaId);
        return repository.findAportaciones(metaId);
    }

    /**
     * Borrar una aportacion tampoco puede dejar el total en negativo: pasa al
     * borrar un ingreso que ya se retiro despues.
     */
    @Override
    public void deleteAportacion(Long usuarioId, Long metaId, Long aportacionId) {
        MetaAhorro meta = getPropia(usuarioId, metaId);
        AportacionMeta aportacion = repository.findAportacionById(aportacionId)
                .filter(a -> a.getMetaId().equals(metaId))
                .orElseThrow(() -> new AportacionMetaNotFoundException(aportacionId));

        BigDecimal sinElla = meta.actual().subtract(aportacion.getImporte());
        if (sinElla.signum() < 0) {
            throw new ValidationException(
                    "No se puede borrar: el total de la meta quedaria en negativo. Borra antes las retiradas posteriores");
        }
        repository.deleteAportacionById(aportacionId);
    }

    private MetaAhorro conPlazo(MetaAhorro meta) {
        meta.calcularPlazo(LocalDate.now());
        return meta;
    }

    /** 409 si otra meta del usuario (distinta de {@code propiaId}) ya usa ese nombre. */
    private void comprobarNombreLibre(Long usuarioId, String nombre, Long propiaId) {
        repository.findByUsuarioIdAndNombre(usuarioId, nombre)
                .filter(otra -> !otra.getId().equals(propiaId))
                .ifPresent(otra -> {
                    throw new DuplicateResourceException("Ya tienes una meta con ese nombre");
                });
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private MetaAhorro getPropia(Long usuarioId, Long id) {
        MetaAhorro meta = repository.findById(id)
                .orElseThrow(() -> new MetaAhorroNotFoundException(id));

        if (!meta.getUsuarioId().equals(usuarioId)) {
            throw new MetaAhorroNotFoundException(id);
        }

        return meta;
    }
}
