package com.polaris.shared.testing;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * El MySQL de los tests de integracion: un unico contenedor por JVM de test,
 * arrancado la primera vez que alguien lo pide y reutilizado por todas las
 * clases (y por tanto por todos los contextos de Spring) del mismo run. Lo
 * recoge Ryuk, o el propio fin de la JVM, no hay que pararlo a mano.
 *
 * <p>Arranque perezoso a proposito: si la clase se inicializara al cargarla, una
 * maquina sin Docker fallaria antes de que {@code @Testcontainers(disabledWithoutDocker
 * = true)} pudiera saltarse el test.
 *
 * <p>La imagen es configurable con la propiedad de sistema
 * {@code polaris.test.mysql.imagen} o la variable {@code POLARIS_TEST_MYSQL_IMAGEN}.
 * Por defecto va de mirror.gcr.io porque Docker Hub devuelve 429 en el entorno
 * cloud. Ver docs/decisiones/027-testcontainers-tests-de-integracion.md.
 */
final class MySqlContenedor {

    static final String IMAGEN_POR_DEFECTO = "mirror.gcr.io/library/mysql:8.4";

    private static MySQLContainer<?> contenedor;

    private MySqlContenedor() {
    }

    static synchronized MySQLContainer<?> instancia() {
        if (contenedor == null) {
            DockerImageName imagen = DockerImageName.parse(nombreImagen())
                    .asCompatibleSubstituteFor("mysql");
            MySQLContainer<?> nuevo = new MySQLContainer<>(imagen)
                    .withDatabaseName("polaris")
                    .withUsername("polaris")
                    .withPassword("polaris-tests")
                    // Como docker-compose.yml: las tablas nacen en utf8mb4.
                    .withCommand("--character-set-server=utf8mb4",
                            "--collation-server=utf8mb4_unicode_ci");
            nuevo.start();
            contenedor = nuevo;
        }
        return contenedor;
    }

    static String nombreImagen() {
        String propiedad = System.getProperty("polaris.test.mysql.imagen");
        if (propiedad != null && !propiedad.isBlank()) {
            return propiedad;
        }
        String entorno = System.getenv("POLARIS_TEST_MYSQL_IMAGEN");
        if (entorno != null && !entorno.isBlank()) {
            return entorno;
        }
        return IMAGEN_POR_DEFECTO;
    }
}
