package com.polaris.nucleo.domain.service;

import com.polaris.nucleo.application.in.CreateSuscripcionPushInterface;
import com.polaris.nucleo.application.in.DeleteSuscripcionPushInterface;
import com.polaris.nucleo.application.in.GetClavePushInterface;
import com.polaris.nucleo.application.out.ClavePushPort;
import com.polaris.nucleo.application.out.SuscripcionPushRepositoryPort;
import com.polaris.nucleo.domain.model.SuscripcionPush;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Dispositivos que reciben los recordatorios. Siempre los del usuario del
 * JWT. Ver docs/decisiones/045-recordatorios.md.
 */
@Service
@RequiredArgsConstructor
public class SuscripcionPushService implements
        CreateSuscripcionPushInterface,
        DeleteSuscripcionPushInterface,
        GetClavePushInterface {

    private final SuscripcionPushRepositoryPort repository;
    private final ClavePushPort clave;

    @Override
    public String clavePublica() {
        return clave.clavePublica();
    }

    /**
     * Un endpoint es un dispositivo: si ya existia (aunque fuera de otra
     * cuenta que se uso antes en ese navegador) se reutiliza la fila y pasa a
     * ser del usuario actual, con sus claves nuevas.
     */
    @Override
    public SuscripcionPush create(Long usuarioId, SuscripcionPush suscripcion) {
        repository.findByEndpoint(suscripcion.getEndpoint()).ifPresentOrElse(
                existente -> {
                    suscripcion.setId(existente.getId());
                    suscripcion.setCreadaEn(existente.getCreadaEn());
                },
                () -> suscripcion.setCreadaEn(LocalDateTime.now()));
        suscripcion.setUsuarioId(usuarioId);
        return repository.save(suscripcion);
    }

    /** Si no existe o es de otro usuario no hace nada: el resultado es el mismo. */
    @Override
    public void delete(Long usuarioId, String endpoint) {
        repository.findByEndpoint(endpoint)
                .filter(s -> s.getUsuarioId().equals(usuarioId))
                .ifPresent(s -> repository.deleteById(s.getId()));
    }
}
