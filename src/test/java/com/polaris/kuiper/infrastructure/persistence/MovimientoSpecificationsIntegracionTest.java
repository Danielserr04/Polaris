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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Las Specifications de Movimiento ejecutadas contra MySQL: lo que los tests
 * unitarios solo ven como un arbol de Criteria falso. Rango de fechas inclusivo,
 * tipo, categoria, aislamiento entre usuarios, orden (fecha e id descendentes) y
 * que el DECIMAL(10,2) y el BIT(1) vuelven bien.
 *
 * <p>Tambien la papelera (docs/decisiones/038-movimiento-papelera-y-duplicar.md):
 * hay un movimiento de A en la papelera, en marzo, que ningun listado normal debe
 * devolver, y los metodos del puerto que la manejan.
 */
class MovimientoSpecificationsIntegracionTest extends IntegracionBase {

    private static final Long A = 9301L;
    private static final Long B = 9302L;

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private MovimientoRepositoryPort movimientos;

    private static final LocalDateTime BORRADO = LocalDateTime.of(2026, 9, 1, 10, 0);

    private Long comida;
    private Long nomina;
    private Long enPapelera;

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
        enPapelera = movimiento(A, "2026-03-20", "42.00", "GASTO", comida, false, BORRADO);
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

    private Long movimiento(Long usuarioId, String fecha, String importe, String tipo, Long categoriaId,
                            boolean recurrente) {
        return movimiento(usuarioId, fecha, importe, tipo, categoriaId, recurrente, null);
    }

    private Long movimiento(Long usuarioId, String fecha, String importe, String tipo, Long categoriaId,
                            boolean recurrente, LocalDateTime borradoEn) {
        Map<String, Object> fila = new HashMap<>(Map.of("usuario_id", usuarioId, "fecha", LocalDate.parse(fecha),
                "importe", new BigDecimal(importe), "tipo", tipo, "categoria_id", categoriaId,
                "recurrente", recurrente));
        fila.put("borrado_en", borradoEn);
        return new SimpleJdbcInsert(jdbc).withTableName("movimiento").usingGeneratedKeyColumns("id")
                .executeAndReturnKey(fila).longValue();
    }

    private Long idDe(Long usuarioId, String importe) {
        return jdbc.queryForObject("select id from movimiento where usuario_id = ? and importe = ?", Long.class,
                usuarioId, new BigDecimal(importe));
    }

    private LocalDateTime borradoEnDe(Long id) {
        return jdbc.queryForObject("select borrado_en from movimiento where id = ?", LocalDateTime.class, id);
    }

    @Test
    @DisplayName("Papelera: findById no ve lo borrado y findEnPapeleraById solo ve lo borrado")
    void findByIdYEnPapelera() {
        Long normal = idDe(A, "10.00");

        assertThat(movimientos.findById(enPapelera)).isEmpty();
        assertThat(movimientos.findEnPapeleraById(enPapelera)).get()
                .satisfies(m -> assertThat(m.getBorradoEn()).isEqualTo(BORRADO));
        assertThat(movimientos.findById(normal)).isPresent();
        assertThat(movimientos.findEnPapeleraById(normal)).isEmpty();
    }

    @Test
    @DisplayName("Papelera: findPapelera trae solo la del usuario, el borrado mas reciente primero, con su categoria")
    void findPapeleraOrdenada() {
        Long otro = movimiento(A, "2026-02-01", "8.00", "GASTO", comida, false, BORRADO.plusDays(1));
        movimiento(B, "2026-02-01", "9.00", "GASTO",
                jdbc.queryForObject("select id from categoria where usuario_id = ?", Long.class, B), false, BORRADO);

        List<Movimiento> papelera = movimientos.findPapelera(A);

        assertThat(papelera).extracting(Movimiento::getId).containsExactly(otro, enPapelera);
        assertThat(papelera.get(0).getCategoria().getNombre()).isEqualTo("Comida");
        assertThat(movimientos.findPapelera(9999L)).isEmpty();
    }

    @Test
    @DisplayName("Papelera: moverAPapelera solo mueve los del usuario que no estaban ya en ella")
    void moverAPapeleraSoloPropiosYActivos() {
        Long propio = idDe(A, "10.00");
        Long ajeno = idDe(B, "555.00");
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 2, 12, 30);

        int movidos = movimientos.moverAPapelera(A, List.of(propio, ajeno, enPapelera), ahora);

        assertThat(movidos).isEqualTo(1);
        assertThat(borradoEnDe(propio)).isEqualTo(ahora);
        assertThat(borradoEnDe(ajeno)).isNull();
        assertThat(borradoEnDe(enPapelera)).isEqualTo(BORRADO);
        assertThat(movimientos.moverAPapelera(A, List.of(), ahora)).isZero();
    }

    @Test
    @DisplayName("Papelera: guardar con borradoEn a null lo restaura")
    void saveRestaura() {
        Movimiento m = movimientos.findEnPapeleraById(enPapelera).orElseThrow();
        m.setBorradoEn(null);

        movimientos.save(m);

        assertThat(borradoEnDe(enPapelera)).isNull();
        assertThat(buscar(A, MovimientoFilter.builder().build())).hasSize(6);
    }

    @Test
    @DisplayName("Papelera: vaciar borra solo la papelera del usuario")
    void vaciarSoloDelUsuario() {
        Long deB = movimiento(B, "2026-02-01", "9.00", "GASTO",
                jdbc.queryForObject("select id from categoria where usuario_id = ?", Long.class, B), false, BORRADO);

        assertThat(movimientos.vaciarPapelera(A)).isEqualTo(1);

        assertThat(movimientos.findEnPapeleraById(enPapelera)).isEmpty();
        assertThat(movimientos.findEnPapeleraById(deB)).isPresent();
        assertThat(buscar(A, MovimientoFilter.builder().build())).hasSize(5);
    }

    @Test
    @DisplayName("Papelera: purgar borra lo que entro antes del limite, de cualquier usuario, y nada mas")
    void purgarAntesDelLimite() {
        Long reciente = movimiento(A, "2026-02-01", "8.00", "GASTO", comida, false, BORRADO.plusDays(10));

        int borrados = movimientos.purgarPapelera(BORRADO.plusDays(1));

        assertThat(borrados).isGreaterThanOrEqualTo(1);
        assertThat(movimientos.findEnPapeleraById(enPapelera)).isEmpty();
        assertThat(movimientos.findEnPapeleraById(reciente)).isPresent();
        assertThat(buscar(A, MovimientoFilter.builder().build())).hasSize(5);
    }

    @Test
    @DisplayName("Papelera: existsByCategoriaId ignora la papelera y deleteEnPapeleraByCategoriaId solo borra de ella")
    void categoriaYPapelera() {
        Long soloEnPapelera = categoria(A, "Viajes", "GASTO");
        Long enViajes = movimiento(A, "2026-02-01", "300.00", "GASTO", soloEnPapelera, false, BORRADO);

        assertThat(movimientos.existsByCategoriaId(soloEnPapelera)).isFalse();
        assertThat(movimientos.existsByCategoriaId(comida)).isTrue();

        assertThat(movimientos.deleteEnPapeleraByCategoriaId(comida)).isEqualTo(1);
        assertThat(movimientos.findEnPapeleraById(enPapelera)).isEmpty();
        assertThat(buscar(A, MovimientoFilter.builder().categoriaId(comida).build())).hasSize(4);
        assertThat(movimientos.findEnPapeleraById(enViajes)).isPresent();
    }
}
