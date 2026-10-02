package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.EstadoPresupuesto;
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
        return presupuesto(categoria, limite, 80);
    }

    private static Presupuesto presupuesto(Categoria categoria, String limite, int alerta) {
        return Presupuesto.builder().usuarioId(USUARIO).categoriaId(categoria.getId()).categoria(categoria)
                .periodo(PeriodoPresupuesto.MENSUAL).importeLimite(new BigDecimal(limite))
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
    @DisplayName("un mes sin nada da todo a cero con dos decimales y sin categorias")
    void mesVacio() {
        conMovimientos();

        ResumenMensual resumen = service.get(USUARIO, SEPTIEMBRE);

        assertThat(resumen.getPeriodo()).isEqualTo(SEPTIEMBRE);
        assertThat(resumen.getIngresos()).isEqualTo(new BigDecimal("0.00"));
        assertThat(resumen.getGastos()).isEqualTo(new BigDecimal("0.00"));
        assertThat(resumen.getBalance()).isEqualTo(new BigDecimal("0.00"));
        assertThat(resumen.getGastoPorCategoria()).isEmpty();
        assertThat(resumen.getPresupuestoTotal()).isEqualTo(new BigDecimal("0.00"));
        assertThat(resumen.getCategoriasEnAviso()).isZero();
        assertThat(resumen.getCategoriasExcedidas()).isZero();
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
    @DisplayName("el desempate por nombre no distingue mayusculas de minusculas")
    void empateOrdenaPorNombreSinDistinguirMayusculas() {
        Categoria ahorro = Categoria.builder().id(20L).usuarioId(USUARIO).nombre("ahorro")
                .tipo(TipoMovimiento.GASTO).build();
        Categoria zapatos = Categoria.builder().id(21L).usuarioId(USUARIO).nombre("Zapatos")
                .tipo(TipoMovimiento.GASTO).build();
        conMovimientos(mov(zapatos, "10.00"), mov(ahorro, "10.00"));

        List<GastoCategoria> filas = service.get(USUARIO, SEPTIEMBRE).getGastoPorCategoria();

        assertThat(filas).extracting(f -> f.getCategoria().getNombre()).containsExactly("ahorro", "Zapatos");
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

    @Test
    @DisplayName("sin presupuesto la fila sale SIN_PRESUPUESTO, con porcentaje y umbral nulos")
    void estadoSinPresupuesto() {
        conMovimientos(mov(COMIDA, "12.50"));

        GastoCategoria fila = service.get(USUARIO, SEPTIEMBRE).getGastoPorCategoria().get(0);

        assertThat(fila.getEstado()).isEqualTo(EstadoPresupuesto.SIN_PRESUPUESTO);
        assertThat(fila.getPorcentaje()).isNull();
        assertThat(fila.getPorcentajeAlerta()).isNull();
    }

    @Test
    @DisplayName("por debajo del umbral la fila esta OK y el porcentaje sale con un decimal")
    void estadoOk() {
        conMovimientos(mov(COMIDA, "100.00"));
        conPresupuestos(presupuesto(COMIDA, "300.00"));

        GastoCategoria fila = service.get(USUARIO, SEPTIEMBRE).getGastoPorCategoria().get(0);

        assertThat(fila.getEstado()).isEqualTo(EstadoPresupuesto.OK);
        assertThat(fila.getPorcentaje()).isEqualTo(new BigDecimal("33.3"));
        assertThat(fila.getPorcentajeAlerta()).isEqualTo(80);
    }

    @Test
    @DisplayName("justo en el umbral la fila pasa a AVISO")
    void estadoAvisoEnElUmbral() {
        conMovimientos(mov(COMIDA, "200.00"));
        conPresupuestos(presupuesto(COMIDA, "250.00"));

        GastoCategoria fila = service.get(USUARIO, SEPTIEMBRE).getGastoPorCategoria().get(0);

        assertThat(fila.getPorcentaje()).isEqualTo(new BigDecimal("80.0"));
        assertThat(fila.getEstado()).isEqualTo(EstadoPresupuesto.AVISO);
    }

    @Test
    @DisplayName("un pelo por debajo del umbral sigue OK aunque el porcentaje redondeado sea el umbral")
    void casiEnElUmbralSigueOk() {
        conMovimientos(mov(COMIDA, "79.96"));
        conPresupuestos(presupuesto(COMIDA, "100.00"));

        GastoCategoria fila = service.get(USUARIO, SEPTIEMBRE).getGastoPorCategoria().get(0);

        assertThat(fila.getPorcentaje()).isEqualTo(new BigDecimal("80.0"));
        assertThat(fila.getEstado()).isEqualTo(EstadoPresupuesto.OK);
    }

    @Test
    @DisplayName("el umbral es el de cada presupuesto, no un 80 fijo")
    void umbralPropio() {
        conMovimientos(mov(COMIDA, "60.00"), mov(OCIO, "60.00"));
        conPresupuestos(presupuesto(COMIDA, "100.00", 50), presupuesto(OCIO, "100.00", 90));

        List<GastoCategoria> filas = service.get(USUARIO, SEPTIEMBRE).getGastoPorCategoria();

        assertThat(filas).extracting(GastoCategoria::getEstado)
                .containsExactly(EstadoPresupuesto.AVISO, EstadoPresupuesto.OK);
    }

    @Test
    @DisplayName("gastar exactamente el limite es AVISO; pasarse es EXCEDIDO")
    void limiteExactoYExceso() {
        conMovimientos(mov(COMIDA, "250.00"), mov(OCIO, "80.01"));
        conPresupuestos(presupuesto(COMIDA, "250.00"), presupuesto(OCIO, "80.00"));

        List<GastoCategoria> filas = service.get(USUARIO, SEPTIEMBRE).getGastoPorCategoria();

        assertThat(filas.get(0).getEstado()).isEqualTo(EstadoPresupuesto.AVISO);
        assertThat(filas.get(0).getPorcentaje()).isEqualTo(new BigDecimal("100.0"));
        assertThat(filas.get(1).getEstado()).isEqualTo(EstadoPresupuesto.EXCEDIDO);
        assertThat(filas.get(1).getPorcentaje()).isEqualTo(new BigDecimal("100.0"));
    }

    @Test
    @DisplayName("una categoria con presupuesto y sin gasto esta OK al 0 %")
    void presupuestoSinGastoOk() {
        conMovimientos();
        conPresupuestos(presupuesto(OCIO, "80.00"));

        GastoCategoria fila = service.get(USUARIO, SEPTIEMBRE).getGastoPorCategoria().get(0);

        assertThat(fila.getEstado()).isEqualTo(EstadoPresupuesto.OK);
        assertThat(fila.getPorcentaje()).isEqualTo(new BigDecimal("0.0"));
    }

    @Test
    @DisplayName("los totales suman los limites mensuales y cuentan las categorias en aviso y excedidas")
    void totalesDePresupuesto() {
        Categoria casa = Categoria.builder().id(13L).usuarioId(USUARIO).nombre("Casa")
                .tipo(TipoMovimiento.GASTO).build();
        Categoria ropa = Categoria.builder().id(14L).usuarioId(USUARIO).nombre("Ropa")
                .tipo(TipoMovimiento.GASTO).build();
        conMovimientos(mov(COMIDA, "90.00"), mov(OCIO, "150.00"), mov(casa, "10.00"), mov(ropa, "40.00"));
        conPresupuestos(presupuesto(COMIDA, "100.00"), presupuesto(OCIO, "100.00"), presupuesto(casa, "500.50"));

        ResumenMensual resumen = service.get(USUARIO, SEPTIEMBRE);

        assertThat(resumen.getPresupuestoTotal()).isEqualByComparingTo("700.50");
        assertThat(resumen.getCategoriasEnAviso()).isEqualTo(1);
        assertThat(resumen.getCategoriasExcedidas()).isEqualTo(1);
    }
}
