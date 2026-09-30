package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.GastoCategoria;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.domain.model.PresupuestoFilter;
import com.polaris.kuiper.domain.model.ResumenMensual;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo que mas importa: que las sumas y el balance cuadren, que el presupuesto
 * mensual se compare con lo gastado, y que se pida solo el mes correcto.
 * Ver docs/decisiones/014-resumen-mensual-agregado-en-servicio.md.
 */
@ExtendWith(MockitoExtension.class)
class ResumenServiceTest {

    private static final Long USUARIO = 1L;
    private static final YearMonth SEPTIEMBRE = YearMonth.of(2026, 9);

    private static final Categoria COMIDA = Categoria.builder().id(10L).usuarioId(USUARIO).nombre("Comida")
            .tipo(TipoMovimiento.GASTO).build();
    private static final Categoria OCIO = Categoria.builder().id(11L).usuarioId(USUARIO).nombre("Ocio")
            .tipo(TipoMovimiento.GASTO).build();
    private static final Categoria NOMINA = Categoria.builder().id(12L).usuarioId(USUARIO).nombre("Nomina")
            .tipo(TipoMovimiento.INGRESO).build();

    @Mock
    private MovimientoRepositoryPort movimientoRepository;

    @Mock
    private PresupuestoRepositoryPort presupuestoRepository;

    @InjectMocks
    private ResumenService service;

    @BeforeEach
    void sinPresupuestosPorDefecto() {
        // Cada test que necesite presupuestos lo sobreescribe.
        org.mockito.Mockito.lenient().when(presupuestoRepository.findAll(eq(USUARIO), any(PresupuestoFilter.class)))
                .thenReturn(List.of());
    }

    private static Movimiento mov(Categoria categoria, String importe) {
        return Movimiento.builder().usuarioId(USUARIO).fecha(LocalDate.of(2026, 9, 10))
                .importe(new BigDecimal(importe)).tipo(categoria.getTipo())
                .categoriaId(categoria.getId()).categoria(categoria).build();
    }

    private static Presupuesto presupuesto(Categoria categoria, String limite) {
        return Presupuesto.builder().usuarioId(USUARIO).categoriaId(categoria.getId()).categoria(categoria)
                .periodo(PeriodoPresupuesto.MENSUAL).importeLimite(new BigDecimal(limite)).build();
    }

    private void conMovimientos(Movimiento... movimientos) {
        when(movimientoRepository.findAll(eq(USUARIO), any(MovimientoFilter.class))).thenReturn(List.of(movimientos));
    }

    @Test
    @DisplayName("un mes sin nada da todo a cero con dos decimales y sin categorias")
    void mesVacio() {
        conMovimientos();

        ResumenMensual resumen = service.get(USUARIO, SEPTIEMBRE);

        assertThat(resumen.getPeriodo()).isEqualTo(SEPTIEMBRE);
        assertThat(resumen.getIngresos()).isEqualTo(new BigDecimal("0.00"));
        assertThat(resumen.getGastos()).isEqualTo(new BigDecimal("0.00"));
        assertThat(resumen.getBalance()).isEqualTo(new BigDecimal("0.00"));
        assertThat(resumen.getGastoPorCategoria()).isEmpty();
    }

    @Test
    @DisplayName("suma ingresos y gastos por separado y el balance es ingresos menos gastos")
    void totalesYBalance() {
        conMovimientos(mov(NOMINA, "1500.00"), mov(COMIDA, "12.50"), mov(OCIO, "30.00"));

        ResumenMensual resumen = service.get(USUARIO, SEPTIEMBRE);

        assertThat(resumen.getIngresos()).isEqualByComparingTo("1500.00");
        assertThat(resumen.getGastos()).isEqualByComparingTo("42.50");
        assertThat(resumen.getBalance()).isEqualByComparingTo("1457.50");
    }

    @Test
    @DisplayName("el balance es negativo cuando se gasta mas de lo ingresado")
    void balanceNegativo() {
        conMovimientos(mov(NOMINA, "100.00"), mov(COMIDA, "250.00"));

        assertThat(service.get(USUARIO, SEPTIEMBRE).getBalance()).isEqualByComparingTo("-150.00");
    }

    @Test
    @DisplayName("agrupa el gasto por categoria, del mayor al menor, sin contar los ingresos")
    void gastoPorCategoriaOrdenado() {
        conMovimientos(mov(COMIDA, "12.50"), mov(OCIO, "30.00"), mov(COMIDA, "20.00"), mov(NOMINA, "1500.00"));

        List<GastoCategoria> filas = service.get(USUARIO, SEPTIEMBRE).getGastoPorCategoria();

        assertThat(filas).hasSize(2);
        assertThat(filas.get(0).getCategoria().getNombre()).isEqualTo("Comida");
        assertThat(filas.get(0).getGastado()).isEqualByComparingTo("32.50");
        assertThat(filas.get(1).getCategoria().getNombre()).isEqualTo("Ocio");
        assertThat(filas.get(1).getGastado()).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("en un empate de gasto ordena por nombre de categoria")
    void empateOrdenaPorNombre() {
        conMovimientos(mov(OCIO, "10.00"), mov(COMIDA, "10.00"));

        List<GastoCategoria> filas = service.get(USUARIO, SEPTIEMBRE).getGastoPorCategoria();

        assertThat(filas).extracting(f -> f.getCategoria().getNombre()).containsExactly("Comida", "Ocio");
    }

    @Test
    @DisplayName("compara lo gastado con el presupuesto mensual: limite y restante")
    void presupuestoMensualConRestante() {
        conMovimientos(mov(COMIDA, "80.00"));
        when(presupuestoRepository.findAll(eq(USUARIO), any(PresupuestoFilter.class)))
                .thenReturn(List.of(presupuesto(COMIDA, "250.00")));

        GastoCategoria fila = service.get(USUARIO, SEPTIEMBRE).getGastoPorCategoria().get(0);

        assertThat(fila.getLimiteMensual()).isEqualByComparingTo("250.00");
        assertThat(fila.getRestante()).isEqualByComparingTo("170.00");
    }

    @Test
    @DisplayName("el restante es negativo cuando se excede el presupuesto")
    void restanteNegativoSiSeExcede() {
        conMovimientos(mov(COMIDA, "300.00"));
        when(presupuestoRepository.findAll(eq(USUARIO), any(PresupuestoFilter.class)))
                .thenReturn(List.of(presupuesto(COMIDA, "250.00")));

        assertThat(service.get(USUARIO, SEPTIEMBRE).getGastoPorCategoria().get(0).getRestante())
                .isEqualByComparingTo("-50.00");
    }

    @Test
    @DisplayName("una categoria con presupuesto pero sin gasto aparece con gastado 0 y restante igual al limite")
    void presupuestoSinGasto() {
        conMovimientos();
        when(presupuestoRepository.findAll(eq(USUARIO), any(PresupuestoFilter.class)))
                .thenReturn(List.of(presupuesto(OCIO, "80.00")));

        GastoCategoria fila = service.get(USUARIO, SEPTIEMBRE).getGastoPorCategoria().get(0);

        assertThat(fila.getCategoria().getNombre()).isEqualTo("Ocio");
        assertThat(fila.getGastado()).isEqualTo(new BigDecimal("0.00"));
        assertThat(fila.getRestante()).isEqualByComparingTo("80.00");
    }

    @Test
    @DisplayName("una categoria con gasto pero sin presupuesto tiene limite y restante nulos")
    void gastoSinPresupuesto() {
        conMovimientos(mov(COMIDA, "12.50"));

        GastoCategoria fila = service.get(USUARIO, SEPTIEMBRE).getGastoPorCategoria().get(0);

        assertThat(fila.getLimiteMensual()).isNull();
        assertThat(fila.getRestante()).isNull();
    }

    @Test
    @DisplayName("pide los movimientos del mes completo, del dia 1 al ultimo, incluso en febrero bisiesto")
    void pideElRangoDelMes() {
        conMovimientos();

        service.get(USUARIO, YearMonth.of(2028, 2));

        ArgumentCaptor<MovimientoFilter> filtro = ArgumentCaptor.forClass(MovimientoFilter.class);
        verify(movimientoRepository).findAll(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getDesde()).isEqualTo(LocalDate.of(2028, 2, 1));
        assertThat(filtro.getValue().getHasta()).isEqualTo(LocalDate.of(2028, 2, 29));
    }

    @Test
    @DisplayName("solo pide los presupuestos mensuales, no los anuales")
    void soloPresupuestosMensuales() {
        conMovimientos();

        service.get(USUARIO, SEPTIEMBRE);

        ArgumentCaptor<PresupuestoFilter> filtro = ArgumentCaptor.forClass(PresupuestoFilter.class);
        verify(presupuestoRepository).findAll(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getPeriodo()).isEqualTo(PeriodoPresupuesto.MENSUAL);
    }
}
