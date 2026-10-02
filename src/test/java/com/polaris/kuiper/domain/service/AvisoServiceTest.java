package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.CrearNotificacionInterface;
import com.polaris.kuiper.application.in.GetResumenMensualInterface;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.application.out.RecurrenteRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.FrecuenciaRecurrente;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.domain.model.ResumenMensual;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.kuiper.domain.model.TipoNotificacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Lo que mas importa: los umbrales del presupuesto (aviso desde el 80 %,
 * excedido solo al pasar del 100 %), que la clave lleve el mes para no
 * repetir, que los cargos proximos sean solo los de los proximos 3 dias, que
 * el resumen salga solo el dia 1 y que nada de esto lance.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AvisoServiceTest {

    private static final Long USUARIO = 1L;
    private static final LocalDate HOY = LocalDate.of(2026, 10, 2);

    @Mock
    private CrearNotificacionInterface crearNotificacion;

    @Mock
    private PresupuestoRepositoryPort presupuestoRepository;

    @Mock
    private MovimientoRepositoryPort movimientoRepository;

    @Mock
    private RecurrenteRepositoryPort recurrenteRepository;

    @Mock
    private GetResumenMensualInterface resumenMensual;

    @InjectMocks
    private AvisoService service;

    private void crearDevuelveLaNotificacion() {
        when(crearNotificacion.crear(any(Notificacion.class))).thenAnswer(inv -> Optional.of(inv.getArgument(0)));
    }

    private static Presupuesto presupuesto(String limite) {
        return Presupuesto.builder().id(7L).usuarioId(USUARIO).categoriaId(10L)
                .categoria(Categoria.builder().id(10L).nombre("Comida").tipo(TipoMovimiento.GASTO).build())
                .periodo(PeriodoPresupuesto.MENSUAL).importeLimite(new BigDecimal(limite)).build();
    }

    private void gastado(String... importes) {
        List<Movimiento> movimientos = Arrays.stream(importes)
                .map(i -> Movimiento.builder().importe(new BigDecimal(i)).tipo(TipoMovimiento.GASTO).build())
                .toList();
        when(movimientoRepository.findAll(eq(USUARIO), any(MovimientoFilter.class))).thenReturn(movimientos);
    }

    private List<Notificacion> creadas() {
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(crearNotificacion, atLeast(0)).crear(captor.capture());
        return captor.getAllValues();
    }

    private static Recurrente recurrente(Long id, TipoMovimiento tipo, LocalDate proxima) {
        return Recurrente.builder().id(id).usuarioId(USUARIO).concepto("Netflix").importe(new BigDecimal("12.99"))
                .tipo(tipo).categoriaId(10L).frecuencia(FrecuenciaRecurrente.MENSUAL).fechaInicio(proxima)
                .proximaFecha(proxima).activo(true).build();
    }

    // --- Presupuestos -------------------------------------------------------

    @ParameterizedTest(name = "gastado {0} de 100 -> {1}")
    @CsvSource({
            "79.99, NINGUNA",
            "80.00, PRESUPUESTO_AVISO",
            "100.00, PRESUPUESTO_AVISO",
            "100.01, PRESUPUESTO_EXCEDIDO",
            "250.00, PRESUPUESTO_EXCEDIDO"
    })
    @DisplayName("Umbrales: aviso desde el 80 % incluido, excedido solo al pasar del limite")
    void umbrales(String gastado, String esperado) {
        crearDevuelveLaNotificacion();
        when(presupuestoRepository.findAllByPeriodo(PeriodoPresupuesto.MENSUAL)).thenReturn(List.of(presupuesto("100.00")));
        gastado(gastado);

        service.generar(HOY);

        List<Notificacion> creadas = creadas();
        if ("NINGUNA".equals(esperado)) {
            assertThat(creadas).isEmpty();
        } else {
            assertThat(creadas).singleElement().satisfies(n -> {
                assertThat(n.getTipo()).isEqualTo(TipoNotificacion.valueOf(esperado));
                assertThat(n.getUsuarioId()).isEqualTo(USUARIO);
                assertThat(n.getEnlace()).isEqualTo(Notificacion.ENLACE_PRESUPUESTOS);
                assertThat(n.getTitulo()).contains("Comida");
            });
        }
    }

    @Test
    @DisplayName("La clave lleva presupuesto y mes, y el gasto se mide en el mes de hoy y solo en esa categoria")
    void claveYFiltroDelMes() {
        crearDevuelveLaNotificacion();
        when(presupuestoRepository.findAllByPeriodo(PeriodoPresupuesto.MENSUAL)).thenReturn(List.of(presupuesto("100.00")));
        gastado("60.00", "30.00");

        service.generar(HOY);

        assertThat(creadas()).singleElement().satisfies(n -> {
            assertThat(n.getClave()).isEqualTo("presupuesto-aviso-7-2026-10");
            assertThat(n.getTexto()).contains("90,00").contains("100,00").contains("octubre de 2026");
        });
        ArgumentCaptor<MovimientoFilter> filtro = ArgumentCaptor.forClass(MovimientoFilter.class);
        verify(movimientoRepository).findAll(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getDesde()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(filtro.getValue().getHasta()).isEqualTo(LocalDate.of(2026, 10, 31));
        assertThat(filtro.getValue().getCategoriaId()).isEqualTo(10L);
        assertThat(filtro.getValue().getTipo()).isEqualTo(TipoMovimiento.GASTO);
    }

    @Test
    @DisplayName("umbralAviso es 80 mientras los presupuestos no tengan el suyo")
    void umbralPorDefecto() {
        assertThat(service.umbralAviso(presupuesto("100.00"))).isEqualTo(80);
    }

    @Test
    @DisplayName("comprobar tras un gasto del mes en curso avisa con el presupuesto mensual de su categoria")
    void comprobarAvisaEnElMesEnCurso() {
        crearDevuelveLaNotificacion();
        when(presupuestoRepository.findByUsuarioIdAndCategoriaIdAndPeriodo(USUARIO, 10L, PeriodoPresupuesto.MENSUAL))
                .thenReturn(Optional.of(presupuesto("50.00")));
        gastado("60.00");

        service.comprobar(USUARIO, 10L, LocalDate.now());

        assertThat(creadas()).singleElement().satisfies(n -> {
            assertThat(n.getTipo()).isEqualTo(TipoNotificacion.PRESUPUESTO_EXCEDIDO);
            assertThat(n.getClave()).isEqualTo("presupuesto-excedido-7-" + YearMonth.now());
        });
    }

    @Test
    @DisplayName("comprobar no hace nada con un gasto de otro mes")
    void comprobarIgnoraOtrosMeses() {
        service.comprobar(USUARIO, 10L, LocalDate.now().minusMonths(1));

        verifyNoInteractions(presupuestoRepository, crearNotificacion);
    }

    @Test
    @DisplayName("comprobar sin presupuesto mensual no avisa")
    void comprobarSinPresupuesto() {
        when(presupuestoRepository.findByUsuarioIdAndCategoriaIdAndPeriodo(USUARIO, 10L, PeriodoPresupuesto.MENSUAL))
                .thenReturn(Optional.empty());

        service.comprobar(USUARIO, 10L, LocalDate.now());

        verifyNoInteractions(crearNotificacion);
    }

    @Test
    @DisplayName("comprobar nunca lanza, aunque falle la lectura")
    void comprobarNoLanza() {
        when(presupuestoRepository.findByUsuarioIdAndCategoriaIdAndPeriodo(any(), any(), any()))
                .thenThrow(new IllegalStateException("BD caida"));

        service.comprobar(USUARIO, 10L, LocalDate.now());

        verifyNoInteractions(crearNotificacion);
    }

    // --- Cargos proximos ----------------------------------------------------

    @Test
    @DisplayName("Cargos proximos: gastos de manana a dentro de 3 dias; ni ingresos, ni los de hoy, ni atrasados")
    void cargosProximos() {
        crearDevuelveLaNotificacion();
        when(recurrenteRepository.findPendientes(HOY.plusDays(3))).thenReturn(List.of(
                recurrente(1L, TipoMovimiento.GASTO, HOY.plusDays(1)),
                recurrente(2L, TipoMovimiento.GASTO, HOY.plusDays(3)),
                recurrente(3L, TipoMovimiento.INGRESO, HOY.plusDays(2)),
                recurrente(4L, TipoMovimiento.GASTO, HOY),
                recurrente(5L, TipoMovimiento.GASTO, HOY.minusDays(4))));

        int creadas = service.generar(HOY);

        assertThat(creadas).isEqualTo(2);
        assertThat(creadas()).extracting(Notificacion::getClave)
                .containsExactly("proximo-1-2026-10-03", "proximo-2-2026-10-05");
        assertThat(creadas()).allSatisfy(n -> {
            assertThat(n.getTipo()).isEqualTo(TipoNotificacion.CARGO_PROXIMO);
            assertThat(n.getEnlace()).isEqualTo(Notificacion.ENLACE_RECURRENTES);
        });
    }

    @Test
    @DisplayName("generar solo cuenta las que se crean de verdad: una ya avisada no suma")
    void generarCuentaSoloLasNuevas() {
        when(crearNotificacion.crear(any(Notificacion.class))).thenReturn(Optional.empty());
        when(recurrenteRepository.findPendientes(HOY.plusDays(3)))
                .thenReturn(List.of(recurrente(1L, TipoMovimiento.GASTO, HOY.plusDays(1))));

        assertThat(service.generar(HOY)).isZero();
    }

    // --- Resumen mensual ----------------------------------------------------

    @Test
    @DisplayName("El dia 1 avisa del resumen del mes anterior a cada usuario con movimientos")
    void resumenElDiaUno() {
        crearDevuelveLaNotificacion();
        LocalDate dia1 = LocalDate.of(2026, 10, 1);
        when(movimientoRepository.findUsuarioIdsConMovimientos(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
                .thenReturn(List.of(USUARIO));
        when(resumenMensual.get(USUARIO, YearMonth.of(2026, 9))).thenReturn(ResumenMensual.builder()
                .periodo(YearMonth.of(2026, 9)).ingresos(new BigDecimal("2000.00")).gastos(new BigDecimal("1234.50"))
                .balance(new BigDecimal("765.50")).gastoPorCategoria(List.of()).build());

        int creadas = service.generar(dia1);

        assertThat(creadas).isEqualTo(1);
        assertThat(creadas()).singleElement().satisfies(n -> {
            assertThat(n.getTipo()).isEqualTo(TipoNotificacion.RESUMEN_MENSUAL);
            assertThat(n.getClave()).isEqualTo("resumen-2026-09");
            assertThat(n.getTitulo()).isEqualTo("Resumen de septiembre de 2026");
            assertThat(n.getTexto()).contains("2.000,00").contains("1.234,50").contains("765,50");
            assertThat(n.getEnlace()).isEqualTo(Notificacion.ENLACE_RESUMEN);
        });
    }

    @Test
    @DisplayName("Fuera del dia 1 no hay resumen")
    void sinResumenOtrosDias() {
        service.generar(HOY);

        verify(movimientoRepository, never()).findUsuarioIdsConMovimientos(any(), any());
        verifyNoInteractions(resumenMensual);
    }

    // --- Robustez -----------------------------------------------------------

    @Test
    @DisplayName("Si falla un bloque, los demas avisan igual y generar no lanza")
    void unFalloNoParaLosDemas() {
        crearDevuelveLaNotificacion();
        when(recurrenteRepository.findPendientes(any())).thenThrow(new IllegalStateException("BD caida"));
        when(presupuestoRepository.findAllByPeriodo(PeriodoPresupuesto.MENSUAL)).thenReturn(List.of(presupuesto("100.00")));
        gastado("120.00");

        assertThat(service.generar(HOY)).isEqualTo(1);
        assertThat(creadas()).extracting(Notificacion::getTipo).containsExactly(TipoNotificacion.PRESUPUESTO_EXCEDIDO);
    }
}
