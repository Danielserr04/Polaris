package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.CrearNotificacionInterface;
import com.polaris.kuiper.application.out.MetaAhorroRepositoryPort;
import com.polaris.kuiper.domain.model.AportacionMeta;
import com.polaris.kuiper.domain.model.AportacionMetaNotFoundException;
import com.polaris.kuiper.domain.model.MetaAhorro;
import com.polaris.kuiper.domain.model.MetaAhorroFilter;
import com.polaris.kuiper.domain.model.MetaAhorroNotFoundException;
import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.domain.model.TipoNotificacion;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo que mas importa: que lo ahorrado nunca baje de 0 (ni retirando ni
 * borrando aportaciones), que las metas de otro usuario den 404 y que los
 * derivados (porcentaje, restante, ahorro mensual) salgan bien. Ver
 * docs/decisiones/036-meta-ahorro-con-aportaciones.md.
 */
@ExtendWith(MockitoExtension.class)
class MetaAhorroServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;

    @Mock
    private MetaAhorroRepositoryPort repository;

    @Mock
    private CrearNotificacionInterface crearNotificacion;

    @InjectMocks
    private MetaAhorroService service;

    private static MetaAhorro meta(String objetivo, String actual) {
        return MetaAhorro.builder().id(5L).usuarioId(USUARIO).nombre("Viaje").importeObjetivo(new BigDecimal(objetivo))
                .importeActual(new BigDecimal(actual)).creadaEn(Instant.parse("2026-01-01T10:00:00Z")).build();
    }

    private static AportacionMeta aportacion(String importe) {
        return AportacionMeta.builder().importe(new BigDecimal(importe)).build();
    }

    // ---- Derivados del modelo ----

    @Test
    @DisplayName("Porcentaje truncado a un decimal, restante nunca negativo y completada al llegar al objetivo")
    void derivados() {
        assertThat(meta("3000.00", "750.00").getPorcentaje()).isEqualByComparingTo("25.0");
        assertThat(meta("3000.00", "2999.99").getPorcentaje()).isEqualByComparingTo("99.9");
        assertThat(meta("3000.00", "2999.99").isCompletada()).isFalse();
        assertThat(meta("3000.00", "3000.00").isCompletada()).isTrue();
        assertThat(meta("3000.00", "3500.00").getRestante()).isEqualByComparingTo("0");
        assertThat(meta("3000.00", "3500.00").getPorcentaje()).isEqualByComparingTo("116.6");
        assertThat(meta("3000.00", "1000.50").getRestante()).isEqualByComparingTo("1999.50");
    }

    @Test
    @DisplayName("Ahorro mensual: cuenta el mes en curso y redondea al centimo hacia arriba")
    void ahorroMensualConFecha() {
        MetaAhorro m = meta("1000.00", "0.00");
        m.setFechaLimite(LocalDate.of(2026, 12, 31));

        m.calcularPlazo(LocalDate.of(2026, 10, 2));

        // octubre, noviembre y diciembre: 1000 / 3 = 333,333... -> 333,34
        assertThat(m.getAhorroMensualNecesario()).isEqualByComparingTo("333.34");
        assertThat(m.getDiasRestantes()).isEqualTo(90L);
    }

    @Test
    @DisplayName("Ahorro mensual: el mismo dia del mes son meses exactos; vencida o de hoy, todo lo que falta")
    void ahorroMensualBordes() {
        MetaAhorro exacta = meta("600.00", "0.00");
        exacta.setFechaLimite(LocalDate.of(2027, 4, 2));
        exacta.calcularPlazo(LocalDate.of(2026, 10, 2));
        assertThat(exacta.getAhorroMensualNecesario()).isEqualByComparingTo("100.00");

        MetaAhorro vencida = meta("600.00", "100.00");
        vencida.setFechaLimite(LocalDate.of(2026, 9, 1));
        vencida.calcularPlazo(LocalDate.of(2026, 10, 2));
        assertThat(vencida.getAhorroMensualNecesario()).isEqualByComparingTo("500.00");
        assertThat(vencida.getDiasRestantes()).isEqualTo(-31L);

        MetaAhorro hoy = meta("600.00", "100.00");
        hoy.setFechaLimite(LocalDate.of(2026, 10, 2));
        hoy.calcularPlazo(LocalDate.of(2026, 10, 2));
        assertThat(hoy.getAhorroMensualNecesario()).isEqualByComparingTo("500.00");
        assertThat(hoy.getDiasRestantes()).isZero();
    }

    @Test
    @DisplayName("Ahorro mensual nulo sin fecha limite o con la meta completada")
    void ahorroMensualNulo() {
        MetaAhorro sinFecha = meta("600.00", "0.00");
        sinFecha.calcularPlazo(LocalDate.of(2026, 10, 2));
        assertThat(sinFecha.getAhorroMensualNecesario()).isNull();
        assertThat(sinFecha.getDiasRestantes()).isNull();

        MetaAhorro completada = meta("600.00", "600.00");
        completada.setFechaLimite(LocalDate.of(2027, 1, 1));
        completada.calcularPlazo(LocalDate.of(2026, 10, 2));
        assertThat(completada.getAhorroMensualNecesario()).isNull();
        assertThat(completada.getDiasRestantes()).isEqualTo(91L);
    }

    // ---- CRUD ----

    @Test
    @DisplayName("Crear: pone usuario y creadaEn del servidor, ignora el id que llegue y calcula el plazo")
    void crear() {
        MetaAhorro nueva = MetaAhorro.builder().id(99L).nombre("Coche").importeObjetivo(new BigDecimal("6000.00"))
                .fechaLimite(LocalDate.now().plusMonths(6)).build();
        when(repository.findByUsuarioIdAndNombre(USUARIO, "Coche")).thenReturn(Optional.empty());
        when(repository.save(any(MetaAhorro.class))).thenAnswer(inv -> {
            MetaAhorro m = inv.getArgument(0);
            m.setImporteActual(BigDecimal.ZERO);
            return m;
        });

        MetaAhorro creada = service.create(USUARIO, nueva);

        assertThat(creada.getId()).isNull();
        assertThat(creada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(creada.getCreadaEn()).isNotNull();
        assertThat(creada.getAhorroMensualNecesario()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Crear con un nombre que ya tienes: 409 y no se guarda")
    void crearNombreRepetido() {
        when(repository.findByUsuarioIdAndNombre(USUARIO, "Viaje")).thenReturn(Optional.of(meta("1.00", "0")));

        assertThatThrownBy(() -> service.create(USUARIO,
                MetaAhorro.builder().nombre("Viaje").importeObjetivo(BigDecimal.TEN).build()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Editar: conserva usuario y creadaEn, y su propio nombre no cuenta como repetido")
    void editar() {
        MetaAhorro existente = meta("1000.00", "200.00");
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(repository.findByUsuarioIdAndNombre(USUARIO, "Viaje")).thenReturn(Optional.of(existente));
        when(repository.save(any(MetaAhorro.class))).thenAnswer(inv -> inv.getArgument(0));

        MetaAhorro cambios = MetaAhorro.builder().nombre("Viaje").importeObjetivo(new BigDecimal("1500.00")).build();
        MetaAhorro editada = service.update(USUARIO, 5L, cambios);

        assertThat(editada.getId()).isEqualTo(5L);
        assertThat(editada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(editada.getCreadaEn()).isEqualTo(existente.getCreadaEn());
        assertThat(editada.getImporteObjetivo()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("Editar con el nombre de otra meta tuya: 409")
    void editarNombreDeOtra() {
        when(repository.findById(5L)).thenReturn(Optional.of(meta("1000.00", "0")));
        MetaAhorro otra = meta("1.00", "0");
        otra.setId(6L);
        when(repository.findByUsuarioIdAndNombre(USUARIO, "Coche")).thenReturn(Optional.of(otra));

        assertThatThrownBy(() -> service.update(USUARIO, 5L,
                MetaAhorro.builder().nombre("Coche").importeObjetivo(BigDecimal.TEN).build()))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("La meta de otro usuario es 404 al leer, editar, borrar, aportar y ver el historial")
    void metaDeOtroUsuario() {
        MetaAhorro ajena = meta("1000.00", "0");
        ajena.setUsuarioId(OTRO_USUARIO);
        when(repository.findById(5L)).thenReturn(Optional.of(ajena));

        assertThatThrownBy(() -> service.get(USUARIO, 5L)).isInstanceOf(MetaAhorroNotFoundException.class);
        assertThatThrownBy(() -> service.update(USUARIO, 5L, meta("1.00", "0")))
                .isInstanceOf(MetaAhorroNotFoundException.class);
        assertThatThrownBy(() -> service.delete(USUARIO, 5L)).isInstanceOf(MetaAhorroNotFoundException.class);
        assertThatThrownBy(() -> service.aportar(USUARIO, 5L, aportacion("10")))
                .isInstanceOf(MetaAhorroNotFoundException.class);
        assertThatThrownBy(() -> service.listAportaciones(USUARIO, 5L))
                .isInstanceOf(MetaAhorroNotFoundException.class);
        assertThatThrownBy(() -> service.deleteAportacion(USUARIO, 5L, 1L))
                .isInstanceOf(MetaAhorroNotFoundException.class);
        verify(repository, never()).deleteById(any());
        verify(repository, never()).saveAportacion(any());
    }

    @Test
    @DisplayName("Listar calcula el plazo de cada meta")
    void listar() {
        MetaAhorro m = meta("1200.00", "0.00");
        m.setFechaLimite(LocalDate.now().plusYears(1));
        when(repository.findAll(USUARIO, null)).thenReturn(List.of(m));

        List<MetaAhorro> metas = service.list(USUARIO, null);

        assertThat(metas).hasSize(1);
        assertThat(metas.get(0).getAhorroMensualNecesario()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Listar pasa el filtro tal cual al repositorio")
    void listarConFiltro() {
        MetaAhorroFilter filtro = MetaAhorroFilter.builder().completada(false).build();
        when(repository.findAll(USUARIO, filtro)).thenReturn(List.of());

        assertThat(service.list(USUARIO, filtro)).isEmpty();
    }

    // ---- Aportaciones ----

    @Test
    @DisplayName("Aportar sin fecha: hoy, con usuario y meta de la ruta, y devuelve la meta con la nueva suma")
    void aportar() {
        when(repository.findById(5L)).thenReturn(Optional.of(meta("1000.00", "100.00")));
        when(repository.sumaAportaciones(5L)).thenReturn(new BigDecimal("250.00"));
        AportacionMeta nueva = aportacion("150.00");
        nueva.setId(77L);
        nueva.setMetaId(123L);

        MetaAhorro resultado = service.aportar(USUARIO, 5L, nueva);

        ArgumentCaptor<AportacionMeta> guardada = ArgumentCaptor.forClass(AportacionMeta.class);
        verify(repository).saveAportacion(guardada.capture());
        assertThat(guardada.getValue().getId()).isNull();
        assertThat(guardada.getValue().getMetaId()).isEqualTo(5L);
        assertThat(guardada.getValue().getUsuarioId()).isEqualTo(USUARIO);
        assertThat(guardada.getValue().getFecha()).isEqualTo(LocalDate.now());
        assertThat(resultado.getImporteActual()).isEqualByComparingTo("250.00");
        assertThat(resultado.getPorcentaje()).isEqualByComparingTo("25.0");
    }

    @Test
    @DisplayName("Cruzar el objetivo con una aportacion avisa una vez con META_ALCANZADA; si ya estaba completada, no")
    void avisaAlAlcanzarLaMeta() {
        when(repository.findById(5L)).thenReturn(Optional.of(meta("1000.00", "900.00")));
        when(repository.sumaAportaciones(5L)).thenReturn(new BigDecimal("1000.00"));

        service.aportar(USUARIO, 5L, aportacion("100.00"));

        ArgumentCaptor<Notificacion> aviso = ArgumentCaptor.forClass(Notificacion.class);
        verify(crearNotificacion).crear(aviso.capture());
        assertThat(aviso.getValue().getTipo()).isEqualTo(TipoNotificacion.META_ALCANZADA);
        assertThat(aviso.getValue().getClave()).isEqualTo("meta-5");
        assertThat(aviso.getValue().getUsuarioId()).isEqualTo(USUARIO);

        when(repository.findById(5L)).thenReturn(Optional.of(meta("1000.00", "1000.00")));
        when(repository.sumaAportaciones(5L)).thenReturn(new BigDecimal("1100.00"));
        service.aportar(USUARIO, 5L, aportacion("100.00"));
        verify(crearNotificacion, times(1)).crear(any());
    }

    @Test
    @DisplayName("Retirar justo todo lo ahorrado se permite: el total queda en 0")
    void retirarTodo() {
        when(repository.findById(5L)).thenReturn(Optional.of(meta("1000.00", "100.00")));
        when(repository.sumaAportaciones(5L)).thenReturn(new BigDecimal("0.00"));

        MetaAhorro resultado = service.aportar(USUARIO, 5L, aportacion("-100.00"));

        assertThat(resultado.getImporteActual()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Retirar mas de lo ahorrado: 400 y no se guarda")
    void retirarDeMas() {
        when(repository.findById(5L)).thenReturn(Optional.of(meta("1000.00", "100.00")));

        assertThatThrownBy(() -> service.aportar(USUARIO, 5L, aportacion("-100.01")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("retirar");
        verify(repository, never()).saveAportacion(any());
    }

    @Test
    @DisplayName("Importe 0 o fecha futura: 400")
    void aportacionInvalida() {
        when(repository.findById(5L)).thenReturn(Optional.of(meta("1000.00", "100.00")));

        assertThatThrownBy(() -> service.aportar(USUARIO, 5L, aportacion("0.00")))
                .isInstanceOf(ValidationException.class);
        AportacionMeta futura = aportacion("10.00");
        futura.setFecha(LocalDate.now().plusDays(1));
        assertThatThrownBy(() -> service.aportar(USUARIO, 5L, futura))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("futura");
        verify(repository, never()).saveAportacion(any());
    }

    @Test
    @DisplayName("Borrar una aportacion que dejaria el total en negativo: 400")
    void borrarAportacionDejaNegativo() {
        // Se aportaron 100 y luego se retiraron 80: quedan 20. Borrar la de 100 dejaria -80.
        when(repository.findById(5L)).thenReturn(Optional.of(meta("1000.00", "20.00")));
        when(repository.findAportacionById(1L)).thenReturn(Optional.of(
                AportacionMeta.builder().id(1L).metaId(5L).importe(new BigDecimal("100.00")).build()));

        assertThatThrownBy(() -> service.deleteAportacion(USUARIO, 5L, 1L))
                .isInstanceOf(ValidationException.class);
        verify(repository, never()).deleteAportacionById(any());
    }

    @Test
    @DisplayName("Borrar una retirada o una aportacion que cabe: se borra")
    void borrarAportacion() {
        when(repository.findById(5L)).thenReturn(Optional.of(meta("1000.00", "20.00")));
        when(repository.findAportacionById(2L)).thenReturn(Optional.of(
                AportacionMeta.builder().id(2L).metaId(5L).importe(new BigDecimal("-80.00")).build()));

        service.deleteAportacion(USUARIO, 5L, 2L);

        verify(repository).deleteAportacionById(2L);
    }

    @Test
    @DisplayName("Borrar una aportacion de otra meta (aunque sea tuya) es 404")
    void borrarAportacionDeOtraMeta() {
        when(repository.findById(5L)).thenReturn(Optional.of(meta("1000.00", "20.00")));
        when(repository.findAportacionById(3L)).thenReturn(Optional.of(
                AportacionMeta.builder().id(3L).metaId(6L).importe(BigDecimal.ONE).build()));

        assertThatThrownBy(() -> service.deleteAportacion(USUARIO, 5L, 3L))
                .isInstanceOf(AportacionMetaNotFoundException.class);
        verify(repository, never()).deleteAportacionById(any());
    }
}
