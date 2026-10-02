package com.polaris.shared.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test: si el contexto llega a existir, es que Flyway ha aplicado todas
 * las migraciones sobre una base vacia y Hibernate (ddl-auto: validate) ha
 * dado por buenas todas las entidades contra el esquema resultante. Es lo
 * que ningun test unitario puede comprobar.
 */
class ArranqueIntegracionTest extends IntegracionBase {

    @Autowired
    private JdbcTemplate jdbc;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    @Test
    @DisplayName("El contexto completo arranca con ddl-auto validate, igual que la app")
    void ddlAutoEsValidate() {
        assertThat(ddlAuto).isEqualTo("validate");
    }

    @Test
    @DisplayName("Es MySQL 8.4 y no otro motor")
    void versionDeMySql() {
        assertThat(jdbc.queryForObject("select version()", String.class)).startsWith("8.4");
    }

    @Test
    @DisplayName("Flyway ha aplicado V1..V24 y V30..V31 (sin V20 ni V25..V29) sin fallos y en orden")
    void flywayAplicaTodasLasMigraciones() {
        List<String> versiones = jdbc.queryForList(
                "select version from flyway_schema_history where success = 1 and version is not null "
                        + "order by installed_rank", String.class);

        assertThat(versiones).containsExactly(
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "21", "22",
                "23", "24", "30", "31");
        assertThat(jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success = 0", Integer.class)).isZero();
    }

    @Test
    @DisplayName("Las tablas de datos nacen en utf8mb4 (CLAUDE.md, regla 7) y ninguna columna es FLOAT ni DOUBLE")
    void esquemaEnUtf8mb4SinFlotantes() {
        List<String> tablasFueraDeUtf8mb4 = jdbc.queryForList(
                "select t.table_name from information_schema.tables t "
                        + "join information_schema.collation_character_set_applicability c "
                        + "on c.collation_name = t.table_collation "
                        + "where t.table_schema = database() and t.table_type = 'BASE TABLE' "
                        + "and t.table_name <> 'flyway_schema_history' and c.character_set_name <> 'utf8mb4'",
                String.class);
        List<String> columnasFlotantes = jdbc.queryForList(
                "select concat(table_name, '.', column_name) from information_schema.columns "
                        + "where table_schema = database() and data_type in ('float', 'double')",
                String.class);

        assertThat(tablasFueraDeUtf8mb4).isEmpty();
        assertThat(columnasFlotantes).isEmpty();
    }

    @Test
    @DisplayName("Toda tabla de datos personales lleva usuario_id (CLAUDE.md, regla 5)")
    void tablasPersonalesConUsuarioId() {
        List<String> tablas = jdbc.queryForList(
                "select table_name from information_schema.tables "
                        + "where table_schema = database() and table_type = 'BASE TABLE' "
                        + "and table_name in ('perfil', 'registro_peso', 'categoria', 'movimiento', "
                        + "'presupuesto', 'objetivo_nutricional', 'comida', 'rutina', 'sesion', "
                        + "'serie_registro') "
                        + "and table_name not in (select table_name from information_schema.columns "
                        + "where table_schema = database() and column_name = 'usuario_id')",
                String.class);

        assertThat(tablas).isEmpty();
    }
}
