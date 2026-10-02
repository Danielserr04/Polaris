package com.polaris.shared.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Activa los {@code @Scheduled} de los modulos. Se apaga con
 * {@code polaris.jobs.activos: false}, que es lo que hacen los tests: un job
 * escribiendo en mitad de un test de integracion seria un fallo aleatorio.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "polaris.jobs.activos", havingValue = "true", matchIfMissing = true)
public class ProgramacionConfig {
}
