package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.EstadoPresupuesto;
import com.polaris.kuiper.domain.model.GastoAnualCategoria;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.domain.model.PresupuestoFilter;
import com.polaris.kuiper.domain.model.ResumenAnual;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo que mas importa: que solo cuenten los presupuestos ANUALES, que se sume
 * el gasto del anio entero de cada categoria y que el estado use el umbral de
 * cada presupuesto. Ver docs/decisiones/035-presupuesto-umbral-de-alerta.md.
 */
@ExtendWith(MockitoExtension.class)
class ResumenAnualServiceTest {

    private static final Long USUARIO = 1L;

    private static final Categoria VIAJES = Categoria.builder().id(10L).usuarioId(USUARIO).nombre("Viajes")
            .tipo(TipoMovimiento.GASTO).build();
    private static final Categoria ROPA = Categoria.builder().id(11L).usuarioId(USUARIO).nombre("Ropa")
            .tipo(TipoMovimiento.GASTO).build();
    private static final Categoria SEGUROS = Categoria.builder().id(12L).usuarioId(USUARIO).nombre("seguros")
            .tipo(TipoMovimiento.GASTO).build();

    @Mock
    private MovimientoRepositoryPort movimientoRepository;

    @Mock
    private PresupuestoRepositoryPort presupuestoRepository;

    @InjectMocks
    private ResumenAnualService service;

    private static Movimiento gasto(Categoria categoria, LocalDate fecha, String importe) {
        return Movimiento.builder().usuarioId(USUARIO).fecha(fecha).importe(new BigDecimal(importe))
                .tipo(TipoMovimiento.GASTO).categoriaId(categoria.getId()).categoria(categoria).build();
    }

    private static Presupuesto anual(Categoria categoria, String limite, int alerta) {
        return Presupuesto.builder().usuarioId(USUARIO).categoriaId(categoria.getId()).categoria(categoria)
                .periodo(PeriodoPresupuesto.ANUAL).importeLimite(new BigDecimal(limite))
                .porcentajeAlerta(alerta).build();
    }

    private void conPresupuestos(Presupuesto... presupuestos) {
        when(presupuestoRepository.findAll(eq(USUARIO), any(PresupuestoFilter.class)))
                .thenReturn(List.of(presupuestos));
    }

    private void conMovimientos(Movimiento... movimientos) {
        when(movimientoRepository.findAll(eq(USUARIO), any(MovimientoFilter.class))).thenReturn(List.of(movimientos));
    }

    @Test
    @DisplayName("sin presupuestos anuales devuelve la lista vacia y ni pide los movimientos")
    void sinPresupuestos() {
        conPresupuestos();

        ResumenAnual resumen = service.get(USUARIO, 2026);

        assertThat(resumen.getAnio()).isEqualTo(2026);
        assertThat(resumen.getPresupuestos()).isEmpty();
        verify(movimientoRepository, never()).findAll(any(), any());
    }

    @Test
    @DisplayName("solo pide presupuestos ANUALES y gastos del 1 de enero al 31 de diciembre")
    void pideLoCorrecto() {
        conPresupuestos(anual(VIAJES, "1200.00", 80));
        conMovimientos();

        service.get(USUARIO, 2026);

        ArgumentCaptor<PresupuestoFilter> filtroPresupuesto = ArgumentCaptor.forClass(PresupuestoFilter.class);
        verify(presupuestoRepository).findAll(eq(USUARIO), filtroPresupuesto.capture());
        assertThat(filtroPresupuesto.getValue().getPeriodo()).isEqualTo(PeriodoPresupuesto.ANUAL);

        ArgumentCaptor<MovimientoFilter> filtro = ArgumentCaptor.forClass(MovimientoFilter.class);
        verify(movimientoRepository).findAll(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getDesde()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(filtro.getValue().getHasta()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(filtro.getValue().getTipo()).isEqualTo(TipoMovimiento.GASTO);
    }

    @Test
    @DisplayName("suma el gasto de todo el anio por categoria y calcula restante, porcentaje y estado")
    void sumaYCalcula() {
        conPresupuestos(anual(VIAJES, "1200.00", 80));
        conMovimientos(gasto(VIAJES, LocalDate.of(2026, 2, 3), "300.00"),
                gasto(VIAJES, LocalDate.of(2026, 8, 20), "250.50"));

        GastoAnualCategoria fila = service.get(USUARIO, 2026).getPresupuestos().get(0);

        assertThat(fila.getCategoria().getNombre()).isEqualTo("Viajes");
        assertThat(fila.getGastado()).isEqualByComparingTo("550.50");
        assertThat(fila.getLimite()).isEqualByComparingTo("1200.00");
        assertThat(fila.getRestante()).isEqualByComparingTo("649.50");
        assertThat(fila.getPorcentaje()).isEqualTo(new BigDecimal("45.9"));
        assertThat(fila.getPorcentajeAlerta()).isEqualTo(80);
        assertThat(fila.getEstado()).isEqualTo(EstadoPresupuesto.OK);
    }

    @Test
    @DisplayName("una categoria sin gasto sale con 0.00, al 0 % y OK")
    void sinGasto() {
        conPresupuestos(anual(SEGUROS, "600.00", 80));
        conMovimientos();

        GastoAnualCategoria fila = service.get(USUARIO, 2026).getPresupuestos().get(0);

        assertThat(fila.getGastado()).isEqualTo(new BigDecimal("0.00"));
        assertThat(fila.getPorcentaje()).isEqualTo(new BigDecimal("0.0"));
        assertThat(fila.getEstado()).isEqualTo(EstadoPresupuesto.OK);
    }

    @Test
    @DisplayName("estado AVISO y EXCEDIDO segun el umbral de cada presupuesto, y del mas consumido al menos")
    void estadosYOrden() {
        conPresupuestos(anual(VIAJES, "1000.00", 60), anual(ROPA, "300.00", 80), anual(SEGUROS, "600.00", 80));
        conMovimientos(gasto(VIAJES, LocalDate.of(2026, 7, 1), "650.00"),
                gasto(ROPA, LocalDate.of(2026, 3, 1), "320.00"),
                gasto(SEGUROS, LocalDate.of(2026, 1, 15), "100.00"));

        List<GastoAnualCategoria> filas = service.get(USUARIO, 2026).getPresupuestos();

        assertThat(filas).extracting(f -> f.getCategoria().getNombre()).containsExactly("Ropa", "Viajes", "seguros");
        assertThat(filas).extracting(GastoAnualCategoria::getEstado).containsExactly(
                EstadoPresupuesto.EXCEDIDO, EstadoPresupuesto.AVISO, EstadoPresupuesto.OK);
        assertThat(filas.get(0).getRestante()).isEqualByComparingTo("-20.00");
    }

    @Test
    @DisplayName("ignora el gasto de categorias sin presupuesto anual")
    void ignoraCategoriasSinPresupuesto() {
        conPresupuestos(anual(VIAJES, "1000.00", 80));
        conMovimientos(gasto(ROPA, LocalDate.of(2026, 3, 1), "320.00"));

        List<GastoAnualCategoria> filas = service.get(USUARIO, 2026).getPresupuestos();

        assertThat(filas).hasSize(1);
        assertThat(filas.get(0).getGastado()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("en un empate de porcentaje ordena por nombre sin distinguir mayusculas")
    void empateOrdenaPorNombre() {
        conPresupuestos(anual(SEGUROS, "100.00", 80), anual(ROPA, "100.00", 80));
        conMovimientos();

        assertThat(service.get(USUARIO, 2026).getPresupuestos())
                .extracting(f -> f.getCategoria().getNombre()).containsExactly("Ropa", "seguros");
    }
}
