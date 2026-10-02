package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.GetPatrimonioInterface;
import com.polaris.kuiper.application.in.GetResumenMensualInterface;
import com.polaris.kuiper.application.in.ListCuentaInterface;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.TransferenciaRepositoryPort;
import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.domain.model.CuentaFilter;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.ResumenMensual;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.kuiper.domain.model.Transferencia;
import com.polaris.kuiper.domain.model.TransferenciaFilter;
import com.polaris.shared.testing.IntegracionBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Las consultas SUM agrupadas por cuenta de movimiento y transferencia,
 * ejecutadas contra MySQL, y el saldo actual que CuentaService monta con
 * ellas. Tambien: el filtro por cuenta de movimientos y transferencias, que
 * los movimientos sin cuenta no cuentan en ningun saldo, que el resumen
 * mensual no ve las transferencias, el aislamiento entre usuarios y que la FK
 * impide borrar una cuenta en uso. Ver docs/decisiones/039-cuentas-y-transferencias.md.
 */
class CuentaSaldoIntegracionTest extends IntegracionBase {

    private static final Long A = 9391L;
    private static final Long B = 9392L;

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private MovimientoRepositoryPort movimientos;
    @Autowired
    private TransferenciaRepositoryPort transferencias;
    @Autowired
    private ListCuentaInterface listCuenta;
    @Autowired
    private GetPatrimonioInterface patrimonio;
    @Autowired
    private GetResumenMensualInterface resumen;

    private Long ing;
    private Long hucha;
    private Long efectivo;
    private Long deB;

    @BeforeEach
    void datos() {
        jdbc.update("delete from transferencia where usuario_id in (?, ?)", A, B);
        jdbc.update("delete from movimiento where usuario_id in (?, ?)", A, B);
        jdbc.update("delete from recurrente where usuario_id in (?, ?)", A, B);
        jdbc.update("delete from cuenta where usuario_id in (?, ?)", A, B);
        jdbc.update("delete from categoria where usuario_id in (?, ?)", A, B);

        Long comida = categoria(A, "Comida", "GASTO");
        Long nomina = categoria(A, "Nomina", "INGRESO");
        Long comidaB = categoria(B, "Comida", "GASTO");

        ing = cuenta(A, "ING", "CORRIENTE", "100.00", false);
        hucha = cuenta(A, "Hucha", "AHORRO", "-50.50", false);
        efectivo = cuenta(A, "Cartera", "EFECTIVO", "0.00", true);
        deB = cuenta(B, "ING", "CORRIENTE", "1000.00", false);

        movimiento(A, "2026-09-01", "1500.00", "INGRESO", nomina, ing);
        movimiento(A, "2026-09-02", "300.25", "GASTO", comida, ing);
        movimiento(A, "2026-09-03", "10.00", "GASTO", comida, hucha);
        movimiento(A, "2026-09-04", "99.99", "GASTO", comida, null);
        movimiento(B, "2026-09-05", "555.00", "GASTO", comidaB, deB);

        transferencia(A, ing, hucha, "200.00", "2026-09-10");
        transferencia(A, ing, hucha, "25.00", "2026-10-01");
        transferencia(A, hucha, efectivo, "40.00", "2026-09-15");
        transferencia(B, deB, cuenta(B, "Hucha B", "AHORRO", "0.00", false), "1.00", "2026-09-10");
    }

    @Test
    @DisplayName("SUM de movimientos por cuenta y tipo: sin los que no tienen cuenta ni los de otro usuario")
    void sumaDeMovimientos() {
        Map<Long, BigDecimal> ingresos = movimientos.sumarPorCuenta(A, TipoMovimiento.INGRESO);
        Map<Long, BigDecimal> gastos = movimientos.sumarPorCuenta(A, TipoMovimiento.GASTO);

        assertThat(texto(ingresos)).containsExactlyInAnyOrderEntriesOf(Map.of(ing, "1500.00"));
        assertThat(texto(gastos)).containsExactlyInAnyOrderEntriesOf(Map.of(ing, "300.25", hucha, "10.00"));
    }

    @Test
    @DisplayName("SUM de transferencias entrantes y salientes por cuenta, solo del usuario")
    void sumaDeTransferencias() {
        assertThat(texto(transferencias.sumarEntrantesPorCuenta(A)))
                .containsExactlyInAnyOrderEntriesOf(Map.of(hucha, "225.00", efectivo, "40.00"));
        assertThat(texto(transferencias.sumarSalientesPorCuenta(A)))
                .containsExactlyInAnyOrderEntriesOf(Map.of(ing, "225.00", hucha, "40.00"));
        assertThat(transferencias.sumarEntrantesPorCuenta(9999L)).isEmpty();
    }

    @Test
    @DisplayName("Saldo actual = inicial + ingresos - gastos + entrantes - salientes; activas primero y por nombre")
    void saldoActual() {
        List<Cuenta> cuentas = listCuenta.list(A, CuentaFilter.builder().build());

        // ING: 100 + 1500 - 300.25 - 225 = 1074.75
        // Hucha: -50.50 - 10 + 225 - 40 = 124.50
        // Cartera (archivada): 0 + 40 = 40.00
        assertThat(cuentas).extracting(c -> c.getNombre() + " " + c.getSaldoActual().toPlainString())
                .containsExactly("Hucha 124.50", "ING 1074.75", "Cartera 40.00");
        assertThat(listCuenta.list(A, CuentaFilter.builder().archivada(false).build())).hasSize(2);
        assertThat(patrimonio.get(A).getTotal().toPlainString()).isEqualTo("1239.25");
        assertThat(patrimonio.get(A).getNumeroCuentas()).isEqualTo(3);
    }

    @Test
    @DisplayName("Movimientos y transferencias se filtran por cuenta; la de otro usuario no devuelve nada")
    void filtrosPorCuenta() {
        List<Movimiento> deIng = movimientos.findAll(A, MovimientoFilter.builder().cuentaId(ing).build());
        List<Transferencia> deHucha = transferencias.findAll(A, TransferenciaFilter.builder().cuentaId(hucha).build());
        List<Transferencia> deHuchaEnSeptiembre = transferencias.findAll(A, TransferenciaFilter.builder()
                .cuentaId(hucha).desde(LocalDate.of(2026, 9, 1)).hasta(LocalDate.of(2026, 9, 30)).build());

        assertThat(deIng).extracting(m -> m.getImporte().toPlainString()).containsExactly("300.25", "1500.00");
        assertThat(deIng.get(0).getCuenta().getNombre()).isEqualTo("ING");
        assertThat(deHucha).extracting(t -> t.getImporte().toPlainString()).containsExactly("25.00", "40.00", "200.00");
        assertThat(deHuchaEnSeptiembre).hasSize(2);
        assertThat(deHucha.get(0).getCuentaOrigen().getNombre()).isEqualTo("ING");
        assertThat(movimientos.findAll(A, MovimientoFilter.builder().cuentaId(deB).build())).isEmpty();
    }

    @Test
    @DisplayName("El resumen mensual no cuenta las transferencias como ingreso ni gasto")
    void resumenSinTransferencias() {
        ResumenMensual septiembre = resumen.get(A, YearMonth.of(2026, 9));

        assertThat(septiembre.getIngresos()).isEqualByComparingTo("1500.00");
        assertThat(septiembre.getGastos()).isEqualByComparingTo("410.24");
    }

    @Test
    @DisplayName("La FK impide borrar una cuenta con movimientos o transferencias; existsByCuentaId lo detecta")
    void cuentaEnUso() {
        assertThat(movimientos.existsByCuentaId(ing)).isTrue();
        assertThat(transferencias.existsByCuentaId(efectivo)).isTrue();
        assertThat(movimientos.existsByCuentaId(efectivo)).isFalse();
        assertThatThrownBy(() -> jdbc.update("delete from cuenta where id = ?", efectivo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Map<Long, String> texto(Map<Long, BigDecimal> sumas) {
        Map<Long, String> resultado = new HashMap<>();
        sumas.forEach((k, v) -> resultado.put(k, v.toPlainString()));
        return resultado;
    }

    private Long categoria(Long usuarioId, String nombre, String tipo) {
        return new SimpleJdbcInsert(jdbc).withTableName("categoria").usingGeneratedKeyColumns("id")
                .executeAndReturnKey(Map.of("usuario_id", usuarioId, "nombre", nombre, "tipo", tipo)).longValue();
    }

    private Long cuenta(Long usuarioId, String nombre, String tipo, String saldoInicial, boolean archivada) {
        return new SimpleJdbcInsert(jdbc).withTableName("cuenta").usingGeneratedKeyColumns("id")
                .executeAndReturnKey(Map.of("usuario_id", usuarioId, "nombre", nombre, "tipo", tipo,
                        "saldo_inicial", new BigDecimal(saldoInicial), "archivada", archivada)).longValue();
    }

    private void movimiento(Long usuarioId, String fecha, String importe, String tipo, Long categoriaId,
                            Long cuentaId) {
        Map<String, Object> fila = new HashMap<>(Map.of("usuario_id", usuarioId, "fecha", LocalDate.parse(fecha),
                "importe", new BigDecimal(importe), "tipo", tipo, "categoria_id", categoriaId, "recurrente", false));
        fila.put("cuenta_id", cuentaId);
        new SimpleJdbcInsert(jdbc).withTableName("movimiento").usingGeneratedKeyColumns("id").execute(fila);
    }

    private void transferencia(Long usuarioId, Long origen, Long destino, String importe, String fecha) {
        new SimpleJdbcInsert(jdbc).withTableName("transferencia").usingGeneratedKeyColumns("id")
                .execute(Map.of("usuario_id", usuarioId, "cuenta_origen_id", origen, "cuenta_destino_id", destino,
                        "importe", new BigDecimal(importe), "fecha", LocalDate.parse(fecha)));
    }
}
