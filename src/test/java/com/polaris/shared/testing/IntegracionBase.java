package com.polaris.shared.testing;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base de los tests de integracion: contexto completo de la aplicacion contra un
 * MySQL 8.4 real (Testcontainers), con Flyway aplicando V1..Vn y
 * {@code ddl-auto: validate}, igual que la app. Ver
 * docs/decisiones/027-testcontainers-tests-de-integracion.md.
 *
 * <ul>
 *   <li>{@code @Tag("integracion")}: se filtran con {@code -Dgroups=integracion}
 *       o {@code -DexcludedGroups=integracion}.</li>
 *   <li>{@code disabledWithoutDocker = true}: sin Docker se saltan, no fallan.</li>
 *   <li>Todas las subclases comparten el contenedor ({@link MySqlContenedor}) y,
 *       si no cambian la configuracion, tambien el contexto de Spring (con
 *       MockMvc ya configurado, para que no haya un contexto por clase).</li>
 * </ul>
 *
 * <p>Los tests comparten base de datos y no se hace rollback: cada clase usa sus
 * propios {@code usuarioId} y limpia sus datos antes de cada test.
 *
 * <p>Perfil {@code dev} (para el adaptador de correo de dev) mas
 * {@code integracion} ({@code application-integracion.yml}: Flyway y validate,
 * que el application.yml de test no trae). Los secretos son los falsos de
 * src/test/resources/application.yml.
 */
@Tag("integracion")
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "integracion"})
public abstract class IntegracionBase {

    @DynamicPropertySource
    static void baseDeDatos(DynamicPropertyRegistry registry) {
        MySQLContainer<?> mysql = MySqlContenedor.instancia();
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
}
