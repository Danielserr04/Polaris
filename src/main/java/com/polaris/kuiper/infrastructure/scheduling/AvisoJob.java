package com.polaris.kuiper.infrastructure.scheduling;

import com.polaris.kuiper.application.in.GenerarAvisosInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Genera los avisos del dia (cargos proximos, presupuestos y, el dia 1, el
 * resumen del mes anterior) a las 08:00 de Europe/Madrid, despues de
 * RecurrenteJob, y otra vez al arrancar. Repetir una pasada no duplica nada:
 * cada aviso tiene su clave unica.
 *
 * <p>Sin {@code @Transactional}, al contrario que RecurrenteJob: cada aviso se
 * guarda en su propia transaccion y un fallo en uno no deshace los demas.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "polaris.jobs.activos", havingValue = "true", matchIfMissing = true)
public class AvisoJob {

    private final GenerarAvisosInterface generarAvisos;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 8 * * *", zone = "Europe/Madrid")
    public void ejecutar() {
        int creadas = generarAvisos.generar(LocalDate.now());
        if (creadas > 0) {
            log.info("Avisos: {} notificacion(es) creada(s)", creadas);
        }
    }
}
