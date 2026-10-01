package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.shared.testing.IntegracionBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Las Specifications de Movimiento ejecutadas contra MySQL: lo que los tests
 * unitarios solo ven como un arbol de Criteria falso. Rango de fechas inclusivo,
 * tipo, categoria, aislamiento entre usuarios, orden (fecha e id descendentes) y
 * que el DECIMAL(10,2) y el BIT(1) vuelven bien.
 */
class MovimientoSpecificationsIntegracionTest extends IntegracionBase {

    private static final Long A = 9301L;
    private static final Long B = 9302L;

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private MovimientoRepositoryPort movimientos;

    private Long comida;
    private Long nomina;

    @BeforeEach
    void datos() {
        jdbc.update("delete from movimiento where usuario_id in (?, ?)", A, B);
        jdbc.update("delete from categoria where usuario_id in (?, ?)", A, B);

        comida = categoria(A, "Comida", "GASTO");
        nomina = categoria(A, "Nomina", "INGRESO");
        Long deB = categoria(B, "Comida", "GASTO");

        movimiento(A, "2026-03-01", "10.00", "GASTO", comida, false);
        movimiento(A, "2026-03-15", "1500.50", "INGRESO", nomina, true);
        movimiento(A, "2026-03-15", "7.25", "GASTO", comida, false);
        movimiento(A, "2026-03-31", "99999999.99", "GASTO", comida, false);
        movimiento(A, "2026-04-01", "3.00", "GASTO", comida, false);
        movimiento(B, "2026-03-15", "555.00", "GASTO", deB, false);
    }

    private List<Movimiento> buscar(Long usuario, MovimientoFilter filtro) {
        return movimientos.findAll(usuario, filtro);
    }

    @Test
    @DisplayName("Sin filtros: solo los del usuario, de la fecha mas reciente a la mas antigua y, a igual fecha, el id mayor primero")
    void soloLosDelUsuarioEnOrden() {
        List<Movimiento> deA = buscar(A, MovimientoFilter.builder().build());

        assertThat(deA).extracting(m -> m.getFecha() + " " + m.getImporte().toPlainString())
                .containsExactly("2026-04-01 3.00", "2026-03-31 99999999.99", "2026-03-15 7.25",
                        "2026-03-15 1500.50", "2026-03-01 10.00");
        assertThat(buscar(B, MovimientoFilter.builder().build())).hasSize(1);
        assertThat(buscar(9999L, MovimientoFilter.builder().build())).isEmpty();
    }

    @Test
    @DisplayName("El rango desde/hasta es inclusivo por los dos extremos")
    void rangoInclusivo() {
        List<Movimiento> marzo = buscar(A, MovimientoFilter.builder()
                .desde(LocalDate.of(2026, 3, 1)).hasta(LocalDate.of(2026, 3, 31)).build());
        List<Movimiento> solo15 = buscar(A, MovimientoFilter.builder()
                .desde(LocalDate.of(2026, 3, 15)).hasta(LocalDate.of(2026, 3, 15)).build());

        assertThat(marzo).hasSize(4);
        assertThat(solo15).extracting(m -> m.getImporte().toPlainString()).containsExactly("7.25", "1500.50");
    }

    @Test
    @DisplayName("Tipo y categoria filtran, se combinan con el rango y llevan la categoria y el recurrente leidos de la base")
    void tipoCategoriaYCombinados() {
        List<Movimiento> ingresos = buscar(A, MovimientoFilter.builder().tipo(TipoMovimiento.INGRESO).build());
        List<Movimiento> deNomina = buscar(A, MovimientoFilter.builder().categoriaId(nomina).build());
        List<Movimiento> gastosDeComidaEnMarzo = buscar(A, MovimientoFilter.builder().tipo(TipoMovimiento.GASTO)
                .categoriaId(comida).desde(LocalDate.of(2026, 3, 10)).hasta(LocalDate.of(2026, 3, 31)).build());

        assertThat(ingresos).hasSize(1);
        assertThat(ingresos.get(0).isRecurrente()).isTrue();
        assertThat(ingresos.get(0).getCategoria().getNombre()).isEqualTo("Nomina");
        assertThat(deNomina).extracting(Movimiento::getId).isEqualTo(
                ingresos.stream().map(Movimiento::getId).toList());
        assertThat(gastosDeComidaEnMarzo).extracting(m -> m.getImporte().toPlainString())
                .containsExactly("99999999.99", "7.25");
        assertThat(gastosDeComidaEnMarzo).noneMatch(Movimiento::isRecurrente);
    }

    @Test
    @DisplayName("Aislamiento: la categoria de otro usuario no devuelve nada aunque se pida su id")
    void categoriaDeOtroUsuario() {
        Long categoriaDeB = jdbc.queryForObject("select id from categoria where usuario_id = ?", Long.class, B);

        assertThat(buscar(A, MovimientoFilter.builder().categoriaId(categoriaDeB).build())).isEmpty();
        assertThat(buscar(B, MovimientoFilter.builder().categoriaId(categoriaDeB).build())).hasSize(1);
    }

    private Long categoria(Long usuarioId, String nombre, String tipo) {
        return new SimpleJdbcInsert(jdbc).withTableName("categoria").usingGeneratedKeyColumns("id")
                .executeAndReturnKey(Map.of("usuario_id", usuarioId, "nombre", nombre, "tipo", tipo)).longValue();
    }

    private void movimiento(Long usuarioId, String fecha, String importe, String tipo, Long categoriaId,
                            boolean recurrente) {
        new SimpleJdbcInsert(jdbc).withTableName("movimiento").usingGeneratedKeyColumns("id")
                .execute(Map.of("usuario_id", usuarioId, "fecha", LocalDate.parse(fecha),
                        "importe", new BigDecimal(importe), "tipo", tipo, "categoria_id", categoriaId,
                        "recurrente", recurrente));
    }
}
