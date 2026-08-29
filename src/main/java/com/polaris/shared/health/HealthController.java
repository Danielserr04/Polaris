package com.polaris.shared.health;

import com.polaris.shared.health.dto.HealthDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Entregable de B0: la aplicacion arranca y responde en /health.
 *
 * <p>Controller propio en vez de Actuator para que la ruta sea exactamente la
 * que pide docs/roadmap.md, y para no meter una dependencia mas en la fase 0.
 */
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

    private static final int TIMEOUT_SEGUNDOS = 2;

    private final DataSource dataSource;
    private final String nombreApp;

    public HealthController(DataSource dataSource, @Value("${spring.application.name}") String nombreApp) {
        this.dataSource = dataSource;
        this.nombreApp = nombreApp;
    }

    @GetMapping
    public ResponseEntity<HealthDto> health() {
        boolean baseDatosViva = comprobarBaseDatos();

        HealthDto body = new HealthDto(
                baseDatosViva ? "UP" : "DOWN",
                nombreApp,
                baseDatosViva ? "UP" : "DOWN"
        );

        return baseDatosViva
                ? ResponseEntity.ok(body)
                : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    private boolean comprobarBaseDatos() {
        try (Connection conexion = dataSource.getConnection()) {
            return conexion.isValid(TIMEOUT_SEGUNDOS);
        } catch (SQLException ex) {
            log.warn("La comprobacion de salud no pudo conectar con MySQL: {}", ex.getMessage());
            return false;
        }
    }
}
