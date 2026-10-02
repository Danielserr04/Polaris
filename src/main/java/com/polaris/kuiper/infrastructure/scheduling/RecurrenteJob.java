package com.polaris.kuiper.infrastructure.scheduling;

import com.polaris.kuiper.application.in.GenerarCargosRecurrentesInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Genera los cargos de los recurrentes una vez al dia, a las 00:05 de
 * Europe/Madrid, y otra al arrancar: si el servidor estuvo apagado, recupera
 * los dias perdidos (el servicio genera todos los atrasados, no solo el ultimo).
 *
 * <p>Una sola transaccion por pasada: o se generan todos los movimientos y
 * avanzan todas las fechas, o ninguno. Generar un cargo dos veces seria peor
 * que generarlo con un dia de retraso.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "polaris.jobs.activos", havingValue = "true", matchIfMissing = true)
public class RecurrenteJob {

    private final GenerarCargosRecurrentesInterface generarCargos;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 5 0 * * *", zone = "Europe/Madrid")
    @Transactional
    public void ejecutar() {
        int creados = generarCargos.generar(LocalDate.now());
        if (creados > 0) {
            log.info("Recurrentes: {} movimiento(s) generado(s)", creados);
        }
    }
}
