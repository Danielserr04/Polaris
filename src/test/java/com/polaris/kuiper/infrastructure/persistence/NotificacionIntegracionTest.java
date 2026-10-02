package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.CrearNotificacionInterface;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.NotificacionRepositoryPort;
import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.domain.model.NotificacionFilter;
import com.polaris.kuiper.domain.model.TipoNotificacion;
import com.polaris.shared.testing.IntegracionBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Notificaciones contra MySQL: la migracion V21 casa con la Entity, el unique
 * (usuario_id, clave) hace de red contra duplicados, las operaciones masivas
 * solo tocan al usuario, y un fallo al guardar un aviso no deja marcada para
 * rollback la transaccion de quien lo llama.
 */
class NotificacionIntegracionTest extends IntegracionBase {

    private static final Long A = 9411L;
    private static final Long B = 9412L;

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private NotificacionRepositoryPort notificaciones;
    @Autowired
    private CrearNotificacionInterface crearNotificacion;
    @Autowired
    private MovimientoRepositoryPort movimientos;
    @Autowired
    private TransactionTemplate transaccion;

    @BeforeEach
    void limpiar() {
        jdbc.update("delete from notificacion where usuario_id in (?, ?)", A, B);
    }

    private static Notificacion aviso(Long usuarioId, String clave) {
        return Notificacion.builder().usuarioId(usuarioId).tipo(TipoNotificacion.CARGO_PROXIMO).clave(clave)
                .titulo("Cargo próximo: Netflix").texto("12,99 € el 05/10/2026.").enlace("recurrentes").build();
    }

    /** Como lo deja NotificacionService antes de llamar al puerto. */
    private static Notificacion listo(Long usuarioId, String clave) {
        Notificacion n = aviso(usuarioId, clave);
        n.setLeida(false);
        n.setCreadaEn(LocalDateTime.now());
        return n;
    }

    @Test
    @DisplayName("crear es idempotente por clave y por usuario; la misma clave en otro usuario si se crea")
    void crearIdempotente() {
        assertThat(crearNotificacion.crear(aviso(A, "proximo-1-2026-10-05"))).isPresent();
        assertThat(crearNotificacion.crear(aviso(A, "proximo-1-2026-10-05"))).isEmpty();
        assertThat(crearNotificacion.crear(aviso(B, "proximo-1-2026-10-05"))).isPresent();

        List<Notificacion> deA = notificaciones.findAll(A, NotificacionFilter.builder().build());
        assertThat(deA).singleElement().satisfies(n -> {
            assertThat(n.isLeida()).isFalse();
            assertThat(n.getCreadaEn()).isNotNull();
            assertThat(n.getTitulo()).isEqualTo("Cargo próximo: Netflix");
            assertThat(n.getTexto()).contains("€");
        });
    }

    @Test
    @DisplayName("El unique salta si se salta la comprobacion, y no arrastra a la transaccion que llama")
    void uniqueNoEnvenenaLaTransaccionExterna() {
        notificaciones.save(listo(A, "dup"));

        transaccion.executeWithoutResult(estado -> {
            jdbc.update("insert into notificacion (usuario_id, tipo, clave, titulo, texto, leida, creada_en) "
                    + "values (?, 'RESUMEN_MENSUAL', 'fuera', 't', 't', 0, now())", A);
            assertThatThrownBy(() -> notificaciones.save(
                    listo(A, "dup")))
                    .isInstanceOf(RuntimeException.class);
        });

        assertThat(jdbc.queryForObject("select count(*) from notificacion where usuario_id = ? and clave = 'fuera'",
                Integer.class, A)).isEqualTo(1);
    }

    @Test
    @DisplayName("Contar, leer todas, filtrar no leidas y borrar leidas: solo del usuario")
    void operacionesMasivas() {
        crearNotificacion.crear(aviso(A, "a1"));
        crearNotificacion.crear(aviso(A, "a2"));
        crearNotificacion.crear(aviso(A, "a3"));
        crearNotificacion.crear(aviso(B, "b1"));

        Notificacion primera = notificaciones.findAll(A, NotificacionFilter.builder().build()).get(0);
        primera.setLeida(true);
        notificaciones.save(primera);

        assertThat(notificaciones.countNoLeidas(A)).isEqualTo(2);
        assertThat(notificaciones.findAll(A, NotificacionFilter.builder().soloNoLeidas(true).build())).hasSize(2);

        assertThat(notificaciones.marcarTodasLeidas(A)).isEqualTo(2);
        assertThat(notificaciones.countNoLeidas(A)).isZero();
        assertThat(notificaciones.countNoLeidas(B)).isEqualTo(1);

        assertThat(notificaciones.deleteLeidas(A)).isEqualTo(3);
        assertThat(notificaciones.findAll(A, NotificacionFilter.builder().build())).isEmpty();
        assertThat(notificaciones.findAll(B, NotificacionFilter.builder().build())).hasSize(1);
    }

    @Test
    @DisplayName("findUsuarioIdsConMovimientos devuelve cada usuario una vez, con el rango inclusivo")
    void usuariosConMovimientos() {
        jdbc.update("delete from movimiento where usuario_id in (?, ?)", A, B);
        jdbc.update("delete from categoria where usuario_id in (?, ?)", A, B);
        jdbc.update("insert into categoria (usuario_id, nombre, tipo) values (?, 'Comida', 'GASTO')", A);
        Long cat = jdbc.queryForObject("select id from categoria where usuario_id = ?", Long.class, A);
        for (String fecha : List.of("2026-09-01", "2026-09-30", "2026-10-01")) {
            jdbc.update("insert into movimiento (usuario_id, fecha, importe, tipo, categoria_id, recurrente) "
                    + "values (?, ?, 1.00, 'GASTO', ?, 0)", A, fecha, cat);
        }

        List<Long> septiembre = movimientos.findUsuarioIdsConMovimientos(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        assertThat(septiembre).contains(A).doesNotContain(B);
        assertThat(septiembre.stream().filter(A::equals).count()).isEqualTo(1);
    }
}
