package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.application.in.ListRecordatorioPendienteInterface;
import com.polaris.nucleo.application.out.ClavePushPort;
import com.polaris.nucleo.application.out.RecordatorioRepositoryPort;
import com.polaris.nucleo.application.out.SuscripcionPushRepositoryPort;
import com.polaris.nucleo.domain.model.AvisoRecordatorio;
import com.polaris.nucleo.domain.model.Recordatorio;
import com.polaris.nucleo.domain.model.SuscripcionPush;
import com.polaris.nucleo.domain.model.TipoRecordatorio;
import com.polaris.shared.testing.IntegracionBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Recordatorios y suscripciones contra MySQL: V35 y V36 casan con las
 * Entities, los dias y la hora hacen el viaje de ida y vuelta, los uniques
 * hacen de red, las claves VAPID se generan una vez y los comprobadores de
 * Fusion, Kuiper y Atlas estan cableados.
 */
class RecordatorioIntegracionTest extends IntegracionBase {

    private static final Long A = 9511L;
    private static final Long B = 9512L;

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private RecordatorioRepositoryPort recordatorios;
    @Autowired
    private SuscripcionPushRepositoryPort suscripciones;
    @Autowired
    private ClavePushPort clave;
    @Autowired
    private ListRecordatorioPendienteInterface pendientes;

    @BeforeEach
    void limpiar() {
        jdbc.update("delete from recordatorio where usuario_id in (?, ?)", A, B);
        jdbc.update("delete from suscripcion_push where usuario_id in (?, ?)", A, B);
    }

    private static Recordatorio recordatorio(Long usuarioId, TipoRecordatorio tipo) {
        return Recordatorio.builder().usuarioId(usuarioId).tipo(tipo).activo(true).hora(LocalTime.of(7, 45))
                .dias(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.SUNDAY)).avisadoEn(LocalDate.of(2026, 10, 2)).build();
    }

    @Test
    @DisplayName("save y find: dias, hora y marcas de dia vuelven igual; solo los del usuario")
    void idaYVuelta() {
        recordatorios.save(recordatorio(A, TipoRecordatorio.COMIDAS));
        recordatorios.save(recordatorio(B, TipoRecordatorio.COMIDAS));

        Recordatorio leido = recordatorios.findByUsuarioIdAndTipo(A, TipoRecordatorio.COMIDAS).orElseThrow();
        assertThat(leido.getDias()).containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.SUNDAY);
        assertThat(leido.getHora()).isEqualTo(LocalTime.of(7, 45));
        assertThat(leido.getAvisadoEn()).isEqualTo(LocalDate.of(2026, 10, 2));
        assertThat(jdbc.queryForObject("select dias from recordatorio where id = ?", String.class, leido.getId()))
                .isEqualTo("1,7");
        assertThat(recordatorios.findAllByUsuarioId(A)).hasSize(1);
    }

    @Test
    @DisplayName("el unique (usuario_id, tipo) impide un segundo recordatorio del mismo tipo")
    void uniquePorTipo() {
        recordatorios.save(recordatorio(A, TipoRecordatorio.GASTOS));

        assertThatThrownBy(() -> recordatorios.save(recordatorio(A, TipoRecordatorio.GASTOS)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("suscripciones: endpoint unico y usuarios con dispositivo para el job")
    void suscripciones() {
        String endpoint = "https://fcm.googleapis.com/fcm/send/" + "x".repeat(300);
        suscripciones.save(SuscripcionPush.builder().usuarioId(A).endpoint(endpoint).p256dh("p").auth("a")
                .creadaEn(LocalDateTime.now()).build());

        assertThat(suscripciones.findByEndpoint(endpoint)).isPresent();
        assertThat(suscripciones.findUsuarioIds()).contains(A).doesNotContain(B);
        assertThatThrownBy(() -> suscripciones.save(SuscripcionPush.builder().usuarioId(B).endpoint(endpoint)
                .p256dh("p").auth("a").creadaEn(LocalDateTime.now()).build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("la clave VAPID se genera una vez y se mantiene")
    void claveVapidEstable() {
        String primera = clave.clavePublica();

        assertThat(primera).hasSizeGreaterThan(80);
        assertThat(jdbc.queryForObject("select publica from push_vapid where id = 1", String.class)).isEqualTo(primera);
        assertThat(clave.clavePublica()).isEqualTo(primera);
    }

    @Test
    @DisplayName("un usuario sin nada apuntado tiene pendientes comidas, gastos y entreno a ultima hora")
    void pendientesConLosModulos() {
        recordatorios.save(Recordatorio.builder().usuarioId(A).tipo(TipoRecordatorio.ENTRENO).activo(true)
                .hora(LocalTime.of(18, 0)).dias(EnumSet.allOf(DayOfWeek.class)).build());

        assertThat(pendientes.pendientes(A, LocalDateTime.of(2026, 10, 2, 23, 59)))
                .extracting(AvisoRecordatorio::tipo)
                .containsExactly(TipoRecordatorio.COMIDAS, TipoRecordatorio.GASTOS, TipoRecordatorio.ENTRENO);
    }
}
