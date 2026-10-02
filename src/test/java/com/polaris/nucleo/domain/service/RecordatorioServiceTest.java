package com.polaris.nucleo.domain.service;

import com.polaris.nucleo.application.out.ComprobarRecordatorioPort;
import com.polaris.nucleo.application.out.EnviarPushPort;
import com.polaris.nucleo.application.out.RecordatorioRepositoryPort;
import com.polaris.nucleo.application.out.SuscripcionPushRepositoryPort;
import com.polaris.nucleo.domain.model.AvisoRecordatorio;
import com.polaris.nucleo.domain.model.Recordatorio;
import com.polaris.nucleo.domain.model.TipoRecordatorio;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo que importa: uno por tipo aunque no este guardado, los valores por
 * defecto, cuando "toca" un recordatorio y que al movil llegue como mucho uno
 * por tipo y dia. Ver docs/decisiones/044-recordatorios.md.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordatorioServiceTest {

    private static final Long USUARIO = 1L;
    /** Viernes 2 de octubre de 2026, 21:30. */
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 2, 21, 30);
    private static final LocalDate HOY = AHORA.toLocalDate();

    @Mock
    private RecordatorioRepositoryPort repository;
    @Mock
    private SuscripcionPushRepositoryPort suscripciones;
    @Mock
    private EnviarPushPort push;
    @Mock
    private ComprobarRecordatorioPort comidas;
    @Mock
    private ComprobarRecordatorioPort gastos;

    private RecordatorioService service;

    private static final AvisoRecordatorio AVISO_COMIDAS =
            new AvisoRecordatorio(TipoRecordatorio.COMIDAS, "Apunta tus comidas", "Nada hoy.", "/fusion");

    @BeforeEach
    void preparar() {
        when(comidas.tipo()).thenReturn(TipoRecordatorio.COMIDAS);
        when(gastos.tipo()).thenReturn(TipoRecordatorio.GASTOS);
        service = new RecordatorioService(repository, suscripciones, push, List.of(comidas, gastos));
        when(repository.save(any(Recordatorio.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Recordatorio guardado(TipoRecordatorio tipo, LocalTime hora) {
        return Recordatorio.builder().id(10L).usuarioId(USUARIO).tipo(tipo).activo(true).hora(hora)
                .dias(EnumSet.allOf(DayOfWeek.class)).build();
    }

    @Test
    @DisplayName("list devuelve los cuatro tipos, con los no guardados por defecto")
    void listRellenaPorDefecto() {
        when(repository.findAllByUsuarioId(USUARIO))
                .thenReturn(List.of(guardado(TipoRecordatorio.GASTOS, LocalTime.of(20, 0))));

        List<Recordatorio> lista = service.list(USUARIO);

        assertThat(lista).extracting(Recordatorio::getTipo).containsExactly(TipoRecordatorio.values());
        assertThat(lista.get(1).getHora()).isEqualTo(LocalTime.of(20, 0));
        Recordatorio comidasPorDefecto = lista.get(0);
        assertThat(comidasPorDefecto.getId()).isNull();
        assertThat(comidasPorDefecto.isActivo()).isTrue();
        assertThat(comidasPorDefecto.getHora()).isEqualTo(LocalTime.of(21, 0));
        Recordatorio entreno = lista.get(2);
        assertThat(entreno.isActivo()).isFalse();
        assertThat(entreno.getDias()).containsExactly(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    }

    @Test
    @DisplayName("update conserva id y marcas de dia del existente y quita los segundos")
    void updateConservaExistente() {
        Recordatorio existente = guardado(TipoRecordatorio.COMIDAS, LocalTime.of(21, 0));
        existente.setAvisadoEn(HOY);
        when(repository.findByUsuarioIdAndTipo(USUARIO, TipoRecordatorio.COMIDAS)).thenReturn(Optional.of(existente));

        Recordatorio cambios = Recordatorio.builder().id(999L).usuarioId(999L).tipo(TipoRecordatorio.COMIDAS)
                .activo(false).hora(LocalTime.of(20, 15, 42)).dias(EnumSet.of(DayOfWeek.MONDAY)).build();
        Recordatorio r = service.update(USUARIO, cambios);

        assertThat(r.getId()).isEqualTo(10L);
        assertThat(r.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(r.getAvisadoEn()).isEqualTo(HOY);
        assertThat(r.getHora()).isEqualTo(LocalTime.of(20, 15));
    }

    @Test
    @DisplayName("update sin dias es 400")
    void updateSinDias() {
        Recordatorio sinDias = Recordatorio.builder().tipo(TipoRecordatorio.COMIDAS).hora(LocalTime.NOON)
                .dias(EnumSet.noneOf(DayOfWeek.class)).build();

        assertThatThrownBy(() -> service.update(USUARIO, sinDias)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("tocaEn: encendido, dia elegido, hora pasada (incluida) y no descartado hoy")
    void tocaEn() {
        Recordatorio r = guardado(TipoRecordatorio.COMIDAS, LocalTime.of(21, 30));
        assertThat(r.tocaEn(HOY, LocalTime.of(21, 30))).isTrue();
        assertThat(r.tocaEn(HOY, LocalTime.of(21, 29))).isFalse();

        r.setDias(EnumSet.of(DayOfWeek.MONDAY));
        assertThat(r.tocaEn(HOY, LocalTime.of(22, 0))).isFalse();

        r.setDias(EnumSet.allOf(DayOfWeek.class));
        r.setDescartadoEn(HOY);
        assertThat(r.tocaEn(HOY, LocalTime.of(22, 0))).isFalse();
        assertThat(r.tocaEn(HOY.plusDays(1), LocalTime.of(22, 0))).isTrue();

        r.setActivo(false);
        assertThat(r.tocaEn(HOY.plusDays(1), LocalTime.of(22, 0))).isFalse();
    }

    @Test
    @DisplayName("pendientes solo trae los que tocan y el modulo dice que siguen sin hacer")
    void pendientes() {
        when(repository.findAllByUsuarioId(USUARIO)).thenReturn(List.of(
                guardado(TipoRecordatorio.COMIDAS, LocalTime.of(21, 0)),
                guardado(TipoRecordatorio.GASTOS, LocalTime.of(21, 0))));
        when(comidas.pendiente(USUARIO, HOY)).thenReturn(Optional.of(AVISO_COMIDAS));
        when(gastos.pendiente(USUARIO, HOY)).thenReturn(Optional.empty());

        assertThat(service.pendientes(USUARIO, AHORA)).containsExactly(AVISO_COMIDAS);
    }

    @Test
    @DisplayName("un comprobador que falla no rompe la lista: ese tipo no sale")
    void comprobadorQueFalla() {
        when(repository.findAllByUsuarioId(USUARIO)).thenReturn(List.of(
                guardado(TipoRecordatorio.COMIDAS, LocalTime.of(21, 0)),
                guardado(TipoRecordatorio.GASTOS, LocalTime.of(21, 0))));
        when(comidas.pendiente(USUARIO, HOY)).thenReturn(Optional.of(AVISO_COMIDAS));
        when(gastos.pendiente(USUARIO, HOY)).thenThrow(new IllegalStateException("caido"));

        assertThat(service.pendientes(USUARIO, AHORA)).containsExactly(AVISO_COMIDAS);
    }

    @Test
    @DisplayName("enviar manda el push y marca avisadoEn; la pasada siguiente no lo repite")
    void enviarUnaVezAlDia() {
        Recordatorio r = guardado(TipoRecordatorio.COMIDAS, LocalTime.of(21, 0));
        when(suscripciones.findUsuarioIds()).thenReturn(List.of(USUARIO));
        when(repository.findAllByUsuarioId(USUARIO)).thenReturn(List.of(r));
        when(comidas.pendiente(USUARIO, HOY)).thenReturn(Optional.of(AVISO_COMIDAS));

        assertThat(service.enviar(AHORA)).isEqualTo(1);
        verify(push).enviar(USUARIO, AVISO_COMIDAS);
        assertThat(r.getAvisadoEn()).isEqualTo(HOY);

        assertThat(service.enviar(AHORA.plusMinutes(1))).isZero();
        verify(push).enviar(eq(USUARIO), any());
    }

    @Test
    @DisplayName("enviar guarda la fila aunque fuera un recordatorio por defecto sin guardar")
    void enviarGuardaPorDefecto() {
        when(suscripciones.findUsuarioIds()).thenReturn(List.of(USUARIO));
        when(repository.findAllByUsuarioId(USUARIO)).thenReturn(List.of());
        when(comidas.pendiente(USUARIO, HOY)).thenReturn(Optional.of(AVISO_COMIDAS));
        when(gastos.pendiente(USUARIO, HOY)).thenReturn(Optional.empty());

        service.enviar(AHORA);

        ArgumentCaptor<Recordatorio> captor = ArgumentCaptor.forClass(Recordatorio.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoRecordatorio.COMIDAS);
        assertThat(captor.getValue().getAvisadoEn()).isEqualTo(HOY);
    }

    @Test
    @DisplayName("enviar no manda nada antes de la hora")
    void enviarAntesDeLaHora() {
        when(suscripciones.findUsuarioIds()).thenReturn(List.of(USUARIO));
        when(repository.findAllByUsuarioId(USUARIO))
                .thenReturn(List.of(guardado(TipoRecordatorio.COMIDAS, LocalTime.of(22, 0))));

        assertThat(service.enviar(AHORA)).isZero();
        verify(push, never()).enviar(any(), any());
    }

    @Test
    @DisplayName("descartar marca descartadoEn y deja de salir en pendientes")
    void descartar() {
        Recordatorio r = guardado(TipoRecordatorio.COMIDAS, LocalTime.of(21, 0));
        when(repository.findByUsuarioIdAndTipo(USUARIO, TipoRecordatorio.COMIDAS)).thenReturn(Optional.of(r));
        when(repository.findAllByUsuarioId(USUARIO)).thenReturn(List.of(r));
        when(comidas.pendiente(USUARIO, HOY)).thenReturn(Optional.of(AVISO_COMIDAS));

        service.descartar(USUARIO, TipoRecordatorio.COMIDAS, HOY);

        assertThat(r.getDescartadoEn()).isEqualTo(HOY);
        assertThat(service.pendientes(USUARIO, AHORA)).isEmpty();
    }
}
