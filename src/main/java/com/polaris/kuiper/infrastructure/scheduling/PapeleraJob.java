package com.polaris.kuiper.infrastructure.scheduling;

import com.polaris.kuiper.application.in.PurgarPapeleraMovimientoInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Vacia de la papelera los movimientos que llevan mas de 30 dias en ella, una
 * vez al dia (00:15 de Europe/Madrid, despues de RecurrenteJob) y otra al
 * arrancar, por si el servidor estuvo apagado. Ver
 * docs/decisiones/038-movimiento-papelera-y-duplicar.md.
 *
 * <p>Sin {@code @Transactional}: la purga es un solo DELETE, atomico por si mismo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "polaris.jobs.activos", havingValue = "true", matchIfMissing = true)
public class PapeleraJob {

    private final PurgarPapeleraMovimientoInterface purgarPapelera;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 15 0 * * *", zone = "Europe/Madrid")
    public void ejecutar() {
        int borrados = purgarPapelera.purgar(LocalDateTime.now());
        if (borrados > 0) {
            log.info("Papelera: {} movimiento(s) borrado(s) definitivamente", borrados);
        }
    }
}
