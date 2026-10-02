package com.polaris.nucleo.infrastructure.push;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polaris.nucleo.application.out.EnviarPushPort;
import com.polaris.nucleo.application.out.SuscripcionPushRepositoryPort;
import com.polaris.nucleo.domain.model.AvisoRecordatorio;
import com.polaris.nucleo.domain.model.SuscripcionPush;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manda el aviso cifrado al servicio de push de cada dispositivo del usuario
 * (Google, Apple, Mozilla...). Gratis y sin cuentas: basta con la firma VAPID.
 * Un 404 o 410 significa que el dispositivo ya no existe (desinstalo la app,
 * quito el permiso): se borra su suscripcion. Ver
 * docs/decisiones/044-recordatorios.md.
 */
@Slf4j
@Component
public class WebPushAdapter implements EnviarPushPort {

    /** Cuanto guarda el servicio de push el aviso si el movil esta apagado. */
    private static final int TTL_SEGUNDOS = 6 * 3600;

    private final SuscripcionPushRepositoryPort suscripciones;
    private final VapidClavesAdapter claves;
    private final ObjectMapper json;
    private final String contacto;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public WebPushAdapter(SuscripcionPushRepositoryPort suscripciones,
                          VapidClavesAdapter claves,
                          ObjectMapper json,
                          @Value("${polaris.push.contacto:mailto:polaris@example.com}") String contacto) {
        this.suscripciones = suscripciones;
        this.claves = claves;
        this.json = json;
        this.contacto = contacto;
    }

    @Override
    public int enviar(Long usuarioId, AvisoRecordatorio aviso) {
        byte[] mensaje = mensaje(aviso);
        int enviados = 0;
        for (SuscripcionPush s : suscripciones.findAllByUsuarioId(usuarioId)) {
            try {
                int estado = enviarA(s, mensaje);
                if (estado == 404 || estado == 410) {
                    suscripciones.deleteById(s.getId());
                    log.info("Push: suscripcion {} caducada ({}), borrada", s.getId(), estado);
                } else if (estado >= 200 && estado < 300) {
                    enviados++;
                } else {
                    log.warn("Push: el servicio respondio {} para la suscripcion {}", estado, s.getId());
                }
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                log.warn("Push: no se ha podido mandar a la suscripcion {}: {}", s.getId(), e.getMessage());
            }
        }
        return enviados;
    }

    private int enviarA(SuscripcionPush s, byte[] mensaje) throws Exception {
        URI endpoint = URI.create(s.getEndpoint());
        String audiencia = endpoint.getScheme() + "://" + endpoint.getHost()
                + (endpoint.getPort() > 0 ? ":" + endpoint.getPort() : "");
        long expira = Instant.now().plus(Duration.ofHours(12)).getEpochSecond();
        HttpRequest peticion = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/octet-stream")
                .header("Content-Encoding", "aes128gcm")
                .header("TTL", String.valueOf(TTL_SEGUNDOS))
                .header("Urgency", "normal")
                .header("Authorization", WebPushCifrado.autorizacion(audiencia, contacto, expira,
                        claves.clavePublica(), claves.clavePrivada()))
                .POST(HttpRequest.BodyPublishers.ofByteArray(
                        WebPushCifrado.cifrar(mensaje, s.getP256dh(), s.getAuth())))
                .build();
        return http.send(peticion, HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    /** Lo que lee el service worker del frontend (public/sw.js). */
    private byte[] mensaje(AvisoRecordatorio aviso) {
        Map<String, String> cuerpo = new LinkedHashMap<>();
        cuerpo.put("tipo", aviso.tipo() == null ? null : aviso.tipo().name());
        cuerpo.put("titulo", aviso.titulo());
        cuerpo.put("texto", aviso.texto());
        cuerpo.put("enlace", aviso.enlace());
        try {
            return json.writeValueAsString(cuerpo).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
