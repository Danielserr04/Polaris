package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.BorrarNotificacionesLeidasInterface;
import com.polaris.kuiper.application.in.ContarNotificacionesNoLeidasInterface;
import com.polaris.kuiper.application.in.CrearNotificacionInterface;
import com.polaris.kuiper.application.in.DeleteNotificacionInterface;
import com.polaris.kuiper.application.in.GetNotificacionInterface;
import com.polaris.kuiper.application.in.LeerTodasNotificacionesInterface;
import com.polaris.kuiper.application.in.ListNotificacionInterface;
import com.polaris.kuiper.application.in.UpdateNotificacionInterface;
import com.polaris.kuiper.application.out.NotificacionRepositoryPort;
import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.domain.model.NotificacionFilter;
import com.polaris.kuiper.domain.model.NotificacionNotFoundException;
import com.polaris.kuiper.domain.model.TextoAviso;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Avisos dentro de la app. Se crean desde otros servicios, nunca por HTTP, y
 * crear uno nunca lanza. Ver docs/decisiones/040-notificaciones-de-kuiper.md.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService implements
        CrearNotificacionInterface,
        GetNotificacionInterface,
        ListNotificacionInterface,
        ContarNotificacionesNoLeidasInterface,
        UpdateNotificacionInterface,
        LeerTodasNotificacionesInterface,
        DeleteNotificacionInterface,
        BorrarNotificacionesLeidasInterface {

    /** Los limites de las columnas de V21. */
    static final int MAX_CLAVE = 120;
    static final int MAX_TITULO = 150;
    static final int MAX_TEXTO = 500;

    private final NotificacionRepositoryPort repository;

    /**
     * Comprueba la clave antes de guardar para no depender del unique en el
     * caso normal (el job pasa varias veces por el mismo hecho). Cualquier
     * fallo se queda en un warn: quien llama sigue como si nada.
     */
    @Override
    public Optional<Notificacion> crear(Notificacion notificacion) {
        try {
            if (repository.existsByUsuarioIdAndClave(notificacion.getUsuarioId(), notificacion.getClave())) {
                return Optional.empty();
            }
            notificacion.setId(null);
            notificacion.setLeida(false);
            notificacion.setCreadaEn(LocalDateTime.now());
            notificacion.setClave(TextoAviso.cortar(notificacion.getClave(), MAX_CLAVE));
            notificacion.setTitulo(TextoAviso.cortar(notificacion.getTitulo(), MAX_TITULO));
            notificacion.setTexto(TextoAviso.cortar(notificacion.getTexto(), MAX_TEXTO));
            return Optional.of(repository.save(notificacion));
        } catch (RuntimeException e) {
            log.warn("No se pudo crear la notificacion {} del usuario {}: {}",
                    notificacion.getClave(), notificacion.getUsuarioId(), e.toString());
            return Optional.empty();
        }
    }

    @Override
    public Notificacion get(Long usuarioId, Long id) {
        return getPropia(usuarioId, id);
    }

    @Override
    public List<Notificacion> list(Long usuarioId, NotificacionFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    @Override
    public long contarNoLeidas(Long usuarioId) {
        return repository.countNoLeidas(usuarioId);
    }

    @Override
    public Notificacion update(Long usuarioId, Long id, Notificacion cambios) {
        Notificacion existente = getPropia(usuarioId, id);
        if (existente.isLeida() == cambios.isLeida()) {
            return existente;
        }
        existente.setLeida(cambios.isLeida());
        return repository.save(existente);
    }

    @Override
    public int leerTodas(Long usuarioId) {
        return repository.marcarTodasLeidas(usuarioId);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropia(usuarioId, id);
        repository.deleteById(id);
    }

    @Override
    public int borrarLeidas(Long usuarioId) {
        return repository.deleteLeidas(usuarioId);
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private Notificacion getPropia(Long usuarioId, Long id) {
        Notificacion notificacion = repository.findById(id)
                .orElseThrow(() -> new NotificacionNotFoundException(id));

        if (!notificacion.getUsuarioId().equals(usuarioId)) {
            throw new NotificacionNotFoundException(id);
        }

        return notificacion;
    }
}
