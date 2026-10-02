package com.polaris.nucleo.infrastructure.scheduling;

import com.polaris.nucleo.application.in.EnviarRecordatoriosInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Cada minuto (hora de Madrid) manda al movil los recordatorios que ya tocan
 * y siguen sin hacer. Repetir una pasada no duplica nada: avisadoEn deja uno
 * por tipo y dia. Se apaga con polaris.jobs.activos: false, como el resto.
 * Ver docs/decisiones/044-recordatorios.md.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "polaris.jobs.activos", havingValue = "true", matchIfMissing = true)
public class RecordatorioJob {

    private final EnviarRecordatoriosInterface enviarRecordatorios;

    @Scheduled(cron = "0 * * * * *", zone = "Europe/Madrid")
    public void ejecutar() {
        int enviados = enviarRecordatorios.enviar(LocalDateTime.now());
        if (enviados > 0) {
            log.info("Recordatorios: {} aviso(s) mandado(s) al movil", enviados);
        }
    }
}
