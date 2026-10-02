package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.GetResumenAnualInterface;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.domain.model.EstadoPresupuesto;
import com.polaris.kuiper.domain.model.GastoAnualCategoria;
import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.domain.model.PresupuestoFilter;
import com.polaris.shared.testing.IntegracionBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * La columna porcentaje_alerta de V17 contra MySQL real: DEFAULT 80 para las
 * filas que no la traen (las de antes de la migracion), CHECK 1..100, ida y
 * vuelta por el adaptador, y el resumen anual sumando el gasto de todo el anio.
 */
class PresupuestoAlertaIntegracionTest extends IntegracionBase {

    private static final Long USUARIO = 9631L;

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PresupuestoRepositoryPort presupuestos;
    @Autowired
    private GetResumenAnualInterface resumenAnual;

    private Long viajes;

    @BeforeEach
    void datos() {
        jdbc.update("delete from presupuesto where usuario_id = ?", USUARIO);
        jdbc.update("delete from movimiento where usuario_id = ?", USUARIO);
        jdbc.update("delete from categoria where usuario_id = ?", USUARIO);
        viajes = new SimpleJdbcInsert(jdbc).withTableName("categoria").usingGeneratedKeyColumns("id")
                .executeAndReturnKey(Map.of("usuario_id", USUARIO, "nombre", "Viajes", "tipo", "GASTO")).longValue();
    }

    private void insertarPresupuesto(Map<String, Object> columnas) {
        new SimpleJdbcInsert(jdbc).withTableName("presupuesto").usingGeneratedKeyColumns("id")
                .usingColumns(columnas.keySet().toArray(String[]::new)).execute(columnas);
    }

    private void gasto(String fecha, String importe) {
        new SimpleJdbcInsert(jdbc).withTableName("movimiento").usingGeneratedKeyColumns("id")
                .execute(Map.of("usuario_id", USUARIO, "fecha", LocalDate.parse(fecha),
                        "importe", new BigDecimal(importe), "tipo", "GASTO", "categoria_id", viajes,
                        "recurrente", false));
    }

    @Test
    @DisplayName("una fila sin porcentaje_alerta se queda con el 80 por defecto")
    void defecto80() {
        insertarPresupuesto(Map.of("usuario_id", USUARIO, "categoria_id", viajes, "periodo", "MENSUAL",
                "importe_limite", new BigDecimal("100.00")));

        List<Presupuesto> leidos = presupuestos.findAll(USUARIO,
                PresupuestoFilter.builder().build());

        assertThat(leidos).singleElement().extracting(Presupuesto::getPorcentajeAlerta).isEqualTo(80);
    }

    @Test
    @DisplayName("el CHECK rechaza un porcentaje_alerta fuera de 1..100")
    void checkDeRango() {
        assertThatThrownBy(() -> insertarPresupuesto(Map.of("usuario_id", USUARIO, "categoria_id", viajes,
                "periodo", "MENSUAL", "importe_limite", new BigDecimal("100.00"), "porcentaje_alerta", 101)))
                .isInstanceOf(DataAccessException.class).hasMessageContaining("chk_presupuesto_porcentaje_alerta");
        assertThatThrownBy(() -> insertarPresupuesto(Map.of("usuario_id", USUARIO, "categoria_id", viajes,
                "periodo", "ANUAL", "importe_limite", new BigDecimal("100.00"), "porcentaje_alerta", 0)))
                .isInstanceOf(DataAccessException.class).hasMessageContaining("chk_presupuesto_porcentaje_alerta");
    }

    @Test
    @DisplayName("el adaptador guarda y lee el porcentaje de alerta")
    void idaYVuelta() {
        Presupuesto guardado = presupuestos.save(Presupuesto.builder().usuarioId(USUARIO).categoriaId(viajes)
                .periodo(PeriodoPresupuesto.ANUAL).importeLimite(new BigDecimal("1200.00"))
                .porcentajeAlerta(65).build());

        assertThat(presupuestos.findById(guardado.getId())).get()
                .extracting(Presupuesto::getPorcentajeAlerta).isEqualTo(65);
    }

    @Test
    @DisplayName("el resumen anual suma el gasto de todo el anio, sin el de otros anios")
    void resumenAnual() {
        insertarPresupuesto(Map.of("usuario_id", USUARIO, "categoria_id", viajes, "periodo", "ANUAL",
                "importe_limite", new BigDecimal("1000.00"), "porcentaje_alerta", 70));
        gasto("2025-12-31", "500.00");
        gasto("2026-01-01", "300.00");
        gasto("2026-12-31", "450.00");
        gasto("2027-01-01", "999.00");

        List<GastoAnualCategoria> filas = resumenAnual.get(USUARIO, 2026).getPresupuestos();

        assertThat(filas).singleElement().satisfies(f -> {
            assertThat(f.getCategoria().getNombre()).isEqualTo("Viajes");
            assertThat(f.getGastado()).isEqualByComparingTo("750.00");
            assertThat(f.getPorcentaje()).isEqualByComparingTo("75.0");
            assertThat(f.getEstado()).isEqualTo(EstadoPresupuesto.AVISO);
        });
    }
}
