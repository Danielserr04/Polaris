package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.application.out.RecurrenteRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaComparada;
import com.polaris.kuiper.domain.model.ComercioFrecuente;
import com.polaris.kuiper.domain.model.ComparativaCategorias;
import com.polaris.kuiper.domain.model.EstadoProyeccion;
import com.polaris.kuiper.domain.model.EvolucionMensual;
import com.polaris.kuiper.domain.model.FrecuenciaRecurrente;
import com.polaris.kuiper.domain.model.Insight;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.domain.model.PresupuestoFilter;
import com.polaris.kuiper.domain.model.ProyeccionMensual;
import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.domain.model.RecurrenteFilter;
import com.polaris.kuiper.domain.model.SeveridadInsight;
import com.polaris.kuiper.domain.model.TipoInsight;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Las cuentas a mano: sumas, porcentajes, el rango anterior, la agrupacion de
 * conceptos, cada insight con sus umbrales y la proyeccion con recurrentes.
 * El repositorio falso filtra por fecha como lo haria el real, asi que se
 * prueba tambien que el servicio pide el rango correcto.
 * Ver docs/decisiones/037-analisis-calculado-en-servicio.md.
 */
@ExtendWith(MockitoExtension.class)
class AnalisisServiceTest {

    private static final Long USUARIO = 1L;
    private static final YearMonth SEPTIEMBRE = YearMonth.of(2026, 9);

    private static final Categoria COMIDA = categoria(10L, "Comida", TipoMovimiento.GASTO);
    private static final Categoria OCIO = categoria(11L, "Ocio", TipoMovimiento.GASTO);
    private static final Categoria CASA = categoria(13L, "Casa", TipoMovimiento.GASTO);
    private static final Categoria NOMINA = categoria(12L, "Nómina", TipoMovimiento.INGRESO);

    @Mock
    private MovimientoRepositoryPort movimientoRepository;

    @Mock
    private PresupuestoRepositoryPort presupuestoRepository;

    @Mock
    private RecurrenteRepositoryPort recurrenteRepository;

    @InjectMocks
    private AnalisisService service;

    private final List<Movimiento> datos = new ArrayList<>();

    @BeforeEach
    void repositoriosFalsos() {
        // El falso filtra por rango como el adapter real: lo que no se pide no llega.
        lenient().when(movimientoRepository.findAll(eq(USUARIO), any(MovimientoFilter.class))).thenAnswer(inv -> {
            MovimientoFilter f = inv.getArgument(1);
            return datos.stream()
                    .filter(m -> f.getDesde() == null || !m.getFecha().isBefore(f.getDesde()))
                    .filter(m -> f.getHasta() == null || !m.getFecha().isAfter(f.getHasta()))
                    .toList();
        });
        lenient().when(presupuestoRepository.findAll(eq(USUARIO), any(PresupuestoFilter.class))).thenReturn(List.of());
        lenient().when(recurrenteRepository.findAll(eq(USUARIO), any(RecurrenteFilter.class))).thenReturn(List.of());
    }

    // ------------------------------------------------------------ fixtures

    private static Categoria categoria(Long id, String nombre, TipoMovimiento tipo) {
        return Categoria.builder().id(id).usuarioId(USUARIO).nombre(nombre).tipo(tipo).build();
    }

    private static Movimiento mov(LocalDate fecha, Categoria categoria, String importe, String concepto) {
        return Movimiento.builder().usuarioId(USUARIO).fecha(fecha).importe(new BigDecimal(importe))
                .tipo(categoria.getTipo()).categoriaId(categoria.getId()).categoria(categoria)
                .concepto(concepto).build();
    }

    private void con(Movimiento... movimientos) {
        datos.addAll(Arrays.asList(movimientos));
    }

    private static LocalDate dia(int mes, int dia) {
        return LocalDate.of(2026, mes, dia);
    }

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    private void conPresupuestos(Presupuesto... presupuestos) {
        when(presupuestoRepository.findAll(eq(USUARIO), any(PresupuestoFilter.class))).thenReturn(List.of(presupuestos));
    }

    private static Presupuesto presupuesto(Categoria categoria, String limite) {
        return Presupuesto.builder().usuarioId(USUARIO).categoriaId(categoria.getId()).categoria(categoria)
                .periodo(PeriodoPresupuesto.MENSUAL).importeLimite(bd(limite)).build();
    }

    private void conRecurrentes(Recurrente... recurrentes) {
        when(recurrenteRepository.findAll(eq(USUARIO), any(RecurrenteFilter.class))).thenReturn(List.of(recurrentes));
    }

    private static Recurrente recurrente(String concepto, String importe, FrecuenciaRecurrente frecuencia,
                                         LocalDate inicio, LocalDate proxima) {
        return Recurrente.builder().usuarioId(USUARIO).concepto(concepto).importe(bd(importe))
                .tipo(TipoMovimiento.GASTO).categoriaId(CASA.getId()).frecuencia(frecuencia)
                .fechaInicio(inicio).proximaFecha(proxima).activo(true).build();
    }

    private static Optional<Insight> deTipo(List<Insight> insights, TipoInsight tipo) {
        return insights.stream().filter(i -> i.getTipo() == tipo).findFirst();
    }

    // ------------------------------------------------------------ evolucion

    @Nested
    @DisplayName("evolucion")
    class Evolucion {

        @Test
        @DisplayName("un mes por fila, del mas antiguo al mas reciente, con los meses vacios a cero")
        void serieConHuecos() {
            con(mov(dia(7, 3), NOMINA, "2000.00", null),
                    mov(dia(7, 10), COMIDA, "500.00", null),
                    mov(dia(9, 1), NOMINA, "2000.00", null),
                    mov(dia(9, 5), COMIDA, "1500.00", null),
                    mov(dia(9, 6), OCIO, "100.00", null));

            List<EvolucionMensual> serie = service.get(USUARIO, SEPTIEMBRE, 3);

            assertThat(serie).extracting(EvolucionMensual::getPeriodo)
                    .containsExactly(YearMonth.of(2026, 7), YearMonth.of(2026, 8), SEPTIEMBRE);
            assertThat(serie.get(0).getIngresos()).isEqualByComparingTo("2000");
            assertThat(serie.get(0).getGastos()).isEqualByComparingTo("500");
            assertThat(serie.get(0).getBalance()).isEqualByComparingTo("1500");
            assertThat(serie.get(0).getTasaAhorro()).isEqualByComparingTo("75.00");

            EvolucionMensual agosto = serie.get(1);
            assertThat(agosto.getIngresos()).isEqualTo(bd("0.00"));
            assertThat(agosto.getGastos()).isEqualTo(bd("0.00"));
            assertThat(agosto.getTasaAhorro()).as("sin ingresos no hay tasa").isNull();

            assertThat(serie.get(2).getBalance()).isEqualByComparingTo("400");
            assertThat(serie.get(2).getTasaAhorro()).isEqualByComparingTo("20.00");
        }

        @Test
        @DisplayName("pide una sola vez el rango completo: del dia 1 del primer mes al ultimo del ultimo")
        void pideElRangoCompleto() {
            service.get(USUARIO, SEPTIEMBRE, 6);

            ArgumentCaptor<MovimientoFilter> filtro = ArgumentCaptor.forClass(MovimientoFilter.class);
            verify(movimientoRepository).findAll(eq(USUARIO), filtro.capture());
            assertThat(filtro.getValue().getDesde()).isEqualTo(dia(4, 1));
            assertThat(filtro.getValue().getHasta()).isEqualTo(dia(9, 30));
        }

        @Test
        @DisplayName("tasa de ahorro negativa si se gasta mas de lo ingresado")
        void tasaNegativa() {
            con(mov(dia(9, 1), NOMINA, "1000.00", null), mov(dia(9, 2), COMIDA, "1250.00", null));

            EvolucionMensual mes = service.get(USUARIO, SEPTIEMBRE, 1).get(0);

            assertThat(mes.getBalance()).isEqualByComparingTo("-250");
            assertThat(mes.getTasaAhorro()).isEqualByComparingTo("-25.00");
        }

        @Test
        @DisplayName("cruza de año sin problema")
        void cruzaDeAnio() {
            List<EvolucionMensual> serie = service.get(USUARIO, YearMonth.of(2026, 1), 3);

            assertThat(serie).extracting(EvolucionMensual::getPeriodo).containsExactly(
                    YearMonth.of(2025, 11), YearMonth.of(2025, 12), YearMonth.of(2026, 1));
        }

        @Test
        @DisplayName("400 con meses fuera de 1..24, sin consultar nada")
        void mesesFueraDeRango() {
            assertThatThrownBy(() -> service.get(USUARIO, SEPTIEMBRE, 0)).isInstanceOf(ValidationException.class);
            assertThatThrownBy(() -> service.get(USUARIO, SEPTIEMBRE, 25)).isInstanceOf(ValidationException.class);
            assertThat(service.get(USUARIO, SEPTIEMBRE, 24)).hasSize(24);
            verify(movimientoRepository).findAll(eq(USUARIO), any());
        }
    }

    // ----------------------------------------------------------- categorias

    @Nested
    @DisplayName("comparativa por categorias")
    class Categorias {

        @Test
        @DisplayName("un mes entero se compara con el mes anterior entero, no con 30 dias")
        void mesEnteroContraMesAnterior() {
            ComparativaCategorias c = service.get(USUARIO, dia(9, 1), dia(9, 30));

            assertThat(c.getAnteriorDesde()).isEqualTo(dia(8, 1));
            assertThat(c.getAnteriorHasta()).isEqualTo(dia(8, 31));
            ArgumentCaptor<MovimientoFilter> filtro = ArgumentCaptor.forClass(MovimientoFilter.class);
            verify(movimientoRepository).findAll(eq(USUARIO), filtro.capture());
            assertThat(filtro.getValue().getDesde()).isEqualTo(dia(8, 1));
            assertThat(filtro.getValue().getHasta()).isEqualTo(dia(9, 30));
        }

        @Test
        @DisplayName("marzo se compara con febrero entero; dos meses, con los dos anteriores")
        void variosMesesEnteros() {
            ComparativaCategorias marzo = service.get(USUARIO, dia(3, 1), dia(3, 31));
            assertThat(marzo.getAnteriorDesde()).isEqualTo(dia(2, 1));
            assertThat(marzo.getAnteriorHasta()).isEqualTo(dia(2, 28));

            ComparativaCategorias dos = service.get(USUARIO, dia(8, 1), dia(9, 30));
            assertThat(dos.getAnteriorDesde()).isEqualTo(dia(6, 1));
            assertThat(dos.getAnteriorHasta()).isEqualTo(dia(7, 31));
        }

        @Test
        @DisplayName("un rango suelto se compara con los mismos dias justo antes")
        void rangoSuelto() {
            ComparativaCategorias c = service.get(USUARIO, dia(9, 11), dia(9, 20));

            assertThat(c.getAnteriorDesde()).isEqualTo(dia(9, 1));
            assertThat(c.getAnteriorHasta()).isEqualTo(dia(9, 10));
        }

        @Test
        @DisplayName("peso, diferencia y variacion por categoria; las que solo gastaron antes tambien salen")
        void calculos() {
            con(mov(dia(8, 5), COMIDA, "200.00", null),
                    mov(dia(8, 6), CASA, "100.00", null),
                    mov(dia(9, 5), COMIDA, "250.00", null),
                    mov(dia(9, 6), COMIDA, "50.00", null),
                    mov(dia(9, 7), OCIO, "100.00", null),
                    mov(dia(9, 8), NOMINA, "3000.00", null));

            ComparativaCategorias c = service.get(USUARIO, dia(9, 1), dia(9, 30));

            assertThat(c.getTotal()).isEqualByComparingTo("400");
            assertThat(c.getTotalAnterior()).isEqualByComparingTo("300");
            assertThat(c.getCategorias()).extracting(cc -> cc.getCategoria().getNombre())
                    .as("ingresos fuera; mayor gasto primero; la que solo gasto antes al final")
                    .containsExactly("Comida", "Ocio", "Casa");

            CategoriaComparada comida = c.getCategorias().get(0);
            assertThat(comida.getGastado()).isEqualByComparingTo("300");
            assertThat(comida.getPorcentaje()).isEqualByComparingTo("75.00");
            assertThat(comida.getGastadoAnterior()).isEqualByComparingTo("200");
            assertThat(comida.getDiferencia()).isEqualByComparingTo("100");
            assertThat(comida.getVariacion()).isEqualByComparingTo("50.00");

            CategoriaComparada ocio = c.getCategorias().get(1);
            assertThat(ocio.getGastadoAnterior()).isEqualTo(bd("0.00"));
            assertThat(ocio.getVariacion()).as("sin gasto anterior no hay variacion").isNull();

            CategoriaComparada casa = c.getCategorias().get(2);
            assertThat(casa.getGastado()).isEqualTo(bd("0.00"));
            assertThat(casa.getPorcentaje()).isEqualByComparingTo("0");
            assertThat(casa.getDiferencia()).isEqualByComparingTo("-100");
            assertThat(casa.getVariacion()).isEqualByComparingTo("-100.00");
        }

        @Test
        @DisplayName("sin gastos: totales a 0.00 y lista vacia")
        void vacio() {
            ComparativaCategorias c = service.get(USUARIO, dia(9, 1), dia(9, 30));

            assertThat(c.getTotal()).isEqualTo(bd("0.00"));
            assertThat(c.getCategorias()).isEmpty();
        }

        @Test
        @DisplayName("400 si desde es posterior a hasta o falta alguna")
        void rangoInvalido() {
            assertThatThrownBy(() -> service.get(USUARIO, dia(9, 30), dia(9, 1)))
                    .isInstanceOf(ValidationException.class);
            assertThatThrownBy(() -> service.get(USUARIO, (LocalDate) null, dia(9, 1)))
                    .isInstanceOf(ValidationException.class);
            verifyNoInteractions(movimientoRepository);
        }
    }

    // ------------------------------------------------------------ comercios

    @Nested
    @DisplayName("comercios frecuentes")
    class Comercios {

        @Test
        @DisplayName("agrupa sin espacios, mayusculas ni tildes y muestra la forma mas repetida")
        void agrupaNormalizado() {
            con(mov(dia(9, 1), COMIDA, "30.00", "Mercadona"),
                    mov(dia(9, 2), COMIDA, "20.00", "  mercadona "),
                    mov(dia(9, 3), COMIDA, "10.00", "Mercadona"),
                    mov(dia(9, 4), OCIO, "3.00", "Cafetería  Sol"),
                    mov(dia(9, 5), OCIO, "4.50", "CAFETERIA SOL"),
                    mov(dia(9, 6), OCIO, "100.00", "Concierto"),
                    mov(dia(9, 7), OCIO, "999.00", "   "),
                    mov(dia(9, 8), OCIO, "999.00", null),
                    mov(dia(9, 9), NOMINA, "5000.00", "Mercadona"));

            List<ComercioFrecuente> top = service.list(USUARIO, dia(9, 1), dia(9, 30), 10);

            assertThat(top).extracting(ComercioFrecuente::getNombre)
                    .containsExactly("Concierto", "Mercadona", "Cafetería Sol");
            ComercioFrecuente mercadona = top.get(1);
            assertThat(mercadona.getTotal()).isEqualByComparingTo("60");
            assertThat(mercadona.getVeces()).isEqualTo(3);
            assertThat(mercadona.getTicketMedio()).isEqualByComparingTo("20.00");
            ComercioFrecuente cafeteria = top.get(2);
            assertThat(cafeteria.getVeces()).isEqualTo(2);
            assertThat(cafeteria.getTicketMedio()).isEqualByComparingTo("3.75");
        }

        @Test
        @DisplayName("respeta el limite y en un empate de total gana el mas frecuente")
        void limiteYEmpates() {
            con(mov(dia(9, 1), COMIDA, "10.00", "A"),
                    mov(dia(9, 2), COMIDA, "5.00", "B"),
                    mov(dia(9, 3), COMIDA, "5.00", "B"),
                    mov(dia(9, 4), COMIDA, "1.00", "C"));

            List<ComercioFrecuente> top = service.list(USUARIO, dia(9, 1), dia(9, 30), 2);

            assertThat(top).extracting(ComercioFrecuente::getNombre).containsExactly("B", "A");
        }

        @Test
        @DisplayName("normaliza: trim, espacios internos, mayusculas y tildes")
        void normalizacion() {
            assertThat(AnalisisService.limpiar("  Café   del  Barrio ")).isEqualTo("Café del Barrio");
            assertThat(AnalisisService.clave("Café del Barrio")).isEqualTo("cafe del barrio");
            assertThat(AnalisisService.clave("ÁÉÍÓÚ Ñandú")).isEqualTo("aeiou nandu");
            assertThat(AnalisisService.limpiar(null)).isEmpty();
        }

        @Test
        @DisplayName("400 con limite fuera de 1..50")
        void limiteInvalido() {
            assertThatThrownBy(() -> service.list(USUARIO, dia(9, 1), dia(9, 30), 0))
                    .isInstanceOf(ValidationException.class);
            assertThatThrownBy(() -> service.list(USUARIO, dia(9, 1), dia(9, 30), 51))
                    .isInstanceOf(ValidationException.class);
            verifyNoInteractions(movimientoRepository);
        }
    }

    // ------------------------------------------------------------- insights

    @Nested
    @DisplayName("insights")
    class Insights {

        private final LocalDate octubre2 = dia(10, 2);

        @Test
        @DisplayName("un mes sin datos ni historial no genera nada")
        void vacio() {
            assertThat(service.list(USUARIO, SEPTIEMBRE, octubre2)).isEmpty();
        }

        @Test
        @DisplayName("mes cerrado: gasto un 50 % por encima de la media de 3 meses es AVISO")
        void gastoPorEncimaDeLaMedia() {
            con(mov(dia(6, 10), COMIDA, "100.00", null),
                    mov(dia(7, 10), COMIDA, "200.00", null),
                    mov(dia(8, 10), COMIDA, "300.00", null),
                    mov(dia(9, 10), COMIDA, "300.00", null));

            Insight i = deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.GASTO_VS_MEDIA).orElseThrow();

            assertThat(i.getSeveridad()).isEqualTo(SeveridadInsight.AVISO);
            assertThat(i.getTitulo()).isEqualTo("Gastas más que de costumbre");
            assertThat(i.getTexto()).isEqualTo("Gastaste 300,00 € en septiembre, un 50 % más que la media "
                    + "de los 3 meses anteriores (200,00 €).");
        }

        @Test
        @DisplayName("la media ignora meses sin movimientos; con uno solo compara con el mes anterior")
        void mediaSoloConMesesConDatos() {
            con(mov(dia(8, 10), COMIDA, "200.00", null), mov(dia(9, 10), COMIDA, "210.00", null));

            Insight i = deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.GASTO_VS_MEDIA).orElseThrow();

            assertThat(i.getSeveridad()).isEqualTo(SeveridadInsight.INFO);
            assertThat(i.getTexto()).isEqualTo("Gastaste 210,00 € en septiembre, parecido a lo que gastaste "
                    + "el mes anterior (200,00 €).");
        }

        @Test
        @DisplayName("mes en curso: la media se prorratea a los dias que han pasado")
        void mesEnCursoProrrateado() {
            con(mov(dia(8, 10), COMIDA, "300.00", null), mov(dia(9, 3), COMIDA, "100.00", null));

            // 15 de 30 dias: la referencia es 150, y 100 es un 33 % menos.
            Insight i = deTipo(service.list(USUARIO, SEPTIEMBRE, dia(9, 15)), TipoInsight.GASTO_VS_MEDIA)
                    .orElseThrow();

            assertThat(i.getSeveridad()).isEqualTo(SeveridadInsight.BIEN);
            assertThat(i.getTexto()).isEqualTo("Llevas 100,00 € este mes, un 33 % menos que lo que gastaste "
                    + "el mes anterior a estas alturas (150,00 €).");
        }

        @Test
        @DisplayName("categoria que mas sube frente al mes anterior")
        void categoriaQueMasSube() {
            con(mov(dia(8, 5), OCIO, "60.00", null),
                    mov(dia(8, 6), COMIDA, "200.00", null),
                    mov(dia(9, 5), OCIO, "105.00", null),
                    mov(dia(9, 6), COMIDA, "210.00", null));

            Insight i = deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.CATEGORIA_SUBE).orElseThrow();

            assertThat(i.getSeveridad()).isEqualTo(SeveridadInsight.AVISO);
            assertThat(i.getTitulo()).isEqualTo("Ocio es la categoría que más sube");
            assertThat(i.getTexto()).isEqualTo("+45,00 € respecto a agosto (de 60,00 € a 105,00 €, un 75 % más).");
        }

        @Test
        @DisplayName("una categoria nueva puede ser la que mas sube; si todo baja no hay insight")
        void categoriaNuevaYTodoBaja() {
            con(mov(dia(8, 5), COMIDA, "200.00", null), mov(dia(9, 5), OCIO, "40.00", null));
            Insight nueva = deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.CATEGORIA_SUBE)
                    .orElseThrow();
            assertThat(nueva.getTexto()).isEqualTo("40,00 € en septiembre; en agosto no tuvo gastos.");

            datos.clear();
            con(mov(dia(8, 5), COMIDA, "200.00", null), mov(dia(9, 5), COMIDA, "100.00", null));
            assertThat(deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.CATEGORIA_SUBE)).isEmpty();
        }

        @Test
        @DisplayName("sin movimientos el mes anterior no hay categoria que suba")
        void sinMesAnterior() {
            con(mov(dia(9, 5), OCIO, "40.00", null));

            assertThat(deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.CATEGORIA_SUBE)).isEmpty();
        }

        @Test
        @DisplayName("presupuesto superado es AVISO con lo gastado y el limite")
        void presupuestoSuperado() {
            con(mov(dia(9, 5), OCIO, "130.00", null), mov(dia(9, 6), COMIDA, "50.00", null));
            conPresupuestos(presupuesto(OCIO, "100.00"), presupuesto(COMIDA, "300.00"));

            Insight i = deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.PRESUPUESTO).orElseThrow();

            assertThat(i.getSeveridad()).isEqualTo(SeveridadInsight.AVISO);
            assertThat(i.getTitulo()).isEqualTo("Has superado el presupuesto de Ocio");
            assertThat(i.getTexto()).isEqualTo("Llevas 130,00 € de 100,00 € (130 %).");
        }

        @Test
        @DisplayName("varios presupuestos superados se enumeran del mas excedido al menos")
        void variosSuperados() {
            con(mov(dia(9, 5), OCIO, "130.00", null), mov(dia(9, 6), COMIDA, "330.00", null));
            conPresupuestos(presupuesto(OCIO, "100.00"), presupuesto(COMIDA, "300.00"));

            Insight i = deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.PRESUPUESTO).orElseThrow();

            assertThat(i.getTitulo()).isEqualTo("Has superado 2 presupuestos");
            assertThat(i.getTexto()).isEqualTo("Ocio (130 %) y Comida (110 %).");
        }

        @Test
        @DisplayName("sin superar ninguno, avisa del mas ajustado a partir del 80 %; por debajo, nada")
        void presupuestoAjustado() {
            con(mov(dia(9, 5), OCIO, "85.00", null), mov(dia(9, 6), COMIDA, "50.00", null));
            conPresupuestos(presupuesto(OCIO, "100.00"), presupuesto(COMIDA, "300.00"));

            Insight i = deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.PRESUPUESTO).orElseThrow();
            assertThat(i.getTitulo()).isEqualTo("Ocio, al 85 % de su presupuesto");
            assertThat(i.getTexto()).isEqualTo("Llevas 85,00 € de 100,00 €; te quedan 15,00 €.");

            datos.clear();
            con(mov(dia(9, 5), OCIO, "79.00", null));
            assertThat(deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.PRESUPUESTO)).isEmpty();
        }

        @Test
        @DisplayName("tasa de ahorro: BIEN desde el 20 %, INFO por debajo, AVISO si es negativa, nada sin ingresos")
        void tasaDeAhorro() {
            con(mov(dia(9, 1), NOMINA, "2000.00", null), mov(dia(9, 2), COMIDA, "1500.00", null));
            Insight bien = deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.TASA_AHORRO).orElseThrow();
            assertThat(bien.getSeveridad()).isEqualTo(SeveridadInsight.BIEN);
            assertThat(bien.getTitulo()).isEqualTo("Ahorras el 25 % de tus ingresos");
            assertThat(bien.getTexto()).isEqualTo("En septiembre has ingresado 2.000,00 € y gastado 1.500,00 €: "
                    + "te quedan 500,00 €.");

            datos.clear();
            con(mov(dia(9, 1), NOMINA, "2000.00", null), mov(dia(9, 2), COMIDA, "1900.00", null));
            Insight info = deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.TASA_AHORRO).orElseThrow();
            assertThat(info.getSeveridad()).isEqualTo(SeveridadInsight.INFO);

            datos.clear();
            con(mov(dia(9, 1), NOMINA, "1000.00", null), mov(dia(9, 2), COMIDA, "1200.00", null));
            Insight aviso = deTipo(service.list(USUARIO, SEPTIEMBRE, dia(9, 20)), TipoInsight.TASA_AHORRO)
                    .orElseThrow();
            assertThat(aviso.getSeveridad()).isEqualTo(SeveridadInsight.AVISO);
            assertThat(aviso.getTitulo()).isEqualTo("Gastas más de lo que ingresas");
            assertThat(aviso.getTexto()).isEqualTo("Este mes los gastos (1.200,00 €) superan a los ingresos "
                    + "(1.000,00 €) en 200,00 €.");

            datos.clear();
            con(mov(dia(9, 2), COMIDA, "100.00", null));
            assertThat(deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.TASA_AHORRO)).isEmpty();
        }

        @Test
        @DisplayName("mayor gasto individual con concepto, fecha y categoria")
        void mayorGasto() {
            con(mov(dia(9, 3), COMIDA, "45.00", "Mercadona"),
                    mov(dia(9, 12), OCIO, "120.00", "  Concierto "),
                    mov(dia(9, 1), NOMINA, "3000.00", "Nómina"));

            Insight i = deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.MAYOR_GASTO).orElseThrow();

            assertThat(i.getSeveridad()).isEqualTo(SeveridadInsight.INFO);
            assertThat(i.getTitulo()).isEqualTo("Tu mayor gasto: 120,00 €");
            assertThat(i.getTexto()).isEqualTo("Concierto, el 12 de septiembre, en Ocio.");
        }

        @Test
        @DisplayName("mayor gasto sin concepto empieza por la fecha")
        void mayorGastoSinConcepto() {
            con(mov(dia(9, 12), OCIO, "120.00", null));

            Insight i = deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.MAYOR_GASTO).orElseThrow();

            assertThat(i.getTexto()).isEqualTo("El 12 de septiembre, en Ocio.");
        }

        @Test
        @DisplayName("recurrentes de los proximos 7 dias en el mes en curso, con su total")
        void recurrentesProximos() {
            LocalDate hoy = dia(10, 2); // viernes
            Recurrente netflix = recurrente("Netflix", "12.99", FrecuenciaRecurrente.MENSUAL,
                    dia(1, 5), dia(10, 5));
            Recurrente gimnasio = recurrente("Gimnasio", "10.00", FrecuenciaRecurrente.SEMANAL,
                    dia(9, 25), dia(10, 2));
            Recurrente seguro = recurrente("Seguro", "300.00", FrecuenciaRecurrente.ANUAL,
                    dia(1, 15), LocalDate.of(2027, 1, 15));
            Recurrente movil = recurrente("Móvil", "20.00", FrecuenciaRecurrente.MENSUAL, dia(1, 4), dia(10, 4));
            movil.setCuotasTotal(10);
            movil.setCuotasPagadas(10);
            conRecurrentes(netflix, gimnasio, seguro, movil);

            Insight i = deTipo(service.list(USUARIO, YearMonth.of(2026, 10), hoy), TipoInsight.RECURRENTES_PROXIMOS)
                    .orElseThrow();

            assertThat(i.getSeveridad()).isEqualTo(SeveridadInsight.INFO);
            assertThat(i.getTitulo()).isEqualTo("3 cargos en los próximos 7 días");
            assertThat(i.getTexto()).isEqualTo("Gimnasio (10,00 €) hoy, Netflix (12,99 €) el lunes 5 y "
                    + "Gimnasio (10,00 €) el viernes 9. Total: 32,99 €.");

            ArgumentCaptor<RecurrenteFilter> filtro = ArgumentCaptor.forClass(RecurrenteFilter.class);
            verify(recurrenteRepository).findAll(eq(USUARIO), filtro.capture());
            assertThat(filtro.getValue().getActivo()).isTrue();
            assertThat(filtro.getValue().getTipo()).isEqualTo(TipoMovimiento.GASTO);
        }

        @Test
        @DisplayName("mas de 4 cargos: se nombran 4 y el resto se cuenta")
        void muchosCargos() {
            LocalDate hoy = dia(10, 2);
            conRecurrentes(
                    recurrente("A", "1.00", FrecuenciaRecurrente.MENSUAL, dia(1, 3), dia(10, 3)),
                    recurrente("B", "1.00", FrecuenciaRecurrente.MENSUAL, dia(1, 3), dia(10, 3)),
                    recurrente("C", "1.00", FrecuenciaRecurrente.MENSUAL, dia(1, 3), dia(10, 3)),
                    recurrente("D", "1.00", FrecuenciaRecurrente.MENSUAL, dia(1, 3), dia(10, 3)),
                    recurrente("E", "1.00", FrecuenciaRecurrente.MENSUAL, dia(1, 3), dia(10, 3)),
                    recurrente("F", "1.00", FrecuenciaRecurrente.MENSUAL, dia(1, 3), dia(10, 3)));

            Insight i = deTipo(service.list(USUARIO, YearMonth.of(2026, 10), hoy), TipoInsight.RECURRENTES_PROXIMOS)
                    .orElseThrow();

            assertThat(i.getTitulo()).isEqualTo("6 cargos en los próximos 7 días");
            assertThat(i.getTexto()).endsWith("D (1,00 €) mañana y 2 más. Total: 6,00 €.");
        }

        @Test
        @DisplayName("un mes que no es el actual no mira los recurrentes")
        void mesPasadoSinRecurrentes() {
            service.list(USUARIO, SEPTIEMBRE, octubre2);

            verify(recurrenteRepository, never()).findAll(any(), any());
        }

        @Test
        @DisplayName("dia de la semana con mas gasto, con al menos 5 gastos")
        void diaDeLaSemana() {
            // 5, 12 y 19 de septiembre de 2026 son sabado.
            con(mov(dia(9, 5), OCIO, "50.00", null),
                    mov(dia(9, 12), OCIO, "50.00", null),
                    mov(dia(9, 19), OCIO, "20.00", null),
                    mov(dia(9, 7), COMIDA, "40.00", null),
                    mov(dia(9, 8), COMIDA, "40.00", null));

            Insight i = deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.DIA_SEMANA).orElseThrow();

            assertThat(i.getTitulo()).isEqualTo("Los sábados es cuando más gastas");
            assertThat(i.getTexto()).isEqualTo("Concentran el 60 % de tu gasto en septiembre (120,00 €).");

            datos.remove(4);
            assertThat(deTipo(service.list(USUARIO, SEPTIEMBRE, octubre2), TipoInsight.DIA_SEMANA)).isEmpty();
        }

        @Test
        @DisplayName("ordenados por severidad: AVISO, BIEN, INFO")
        void ordenPorSeveridad() {
            con(mov(dia(8, 10), COMIDA, "100.00", null),
                    mov(dia(9, 1), NOMINA, "2000.00", null),
                    mov(dia(9, 10), COMIDA, "300.00", "Súper"));

            List<Insight> insights = service.list(USUARIO, SEPTIEMBRE, octubre2);

            assertThat(insights).extracting(Insight::getSeveridad).isSorted();
            assertThat(insights).extracting(Insight::getTipo).containsExactly(
                    TipoInsight.GASTO_VS_MEDIA, TipoInsight.CATEGORIA_SUBE,
                    TipoInsight.TASA_AHORRO, TipoInsight.MAYOR_GASTO);
        }
    }

    // ----------------------------------------------------------- proyeccion

    @Nested
    @DisplayName("proyeccion")
    class Proyeccion {

        @Test
        @DisplayName("mes en curso: actual + ritmo del gasto variable por los dias que faltan + recurrentes pendientes")
        void enCurso() {
            Movimiento yaCobrado = mov(dia(9, 1), CASA, "50.00", "Alquiler");
            yaCobrado.setRecurrente(true);
            con(yaCobrado,
                    mov(dia(9, 3), COMIDA, "100.00", null),
                    mov(dia(9, 8), OCIO, "200.00", null),
                    mov(dia(9, 9), NOMINA, "2000.00", null));
            Recurrente luz = recurrente("Luz", "40.00", FrecuenciaRecurrente.MENSUAL, dia(1, 20), dia(9, 20));
            Recurrente gimnasio = recurrente("Gimnasio", "10.00", FrecuenciaRecurrente.SEMANAL,
                    dia(9, 14), dia(9, 14));
            Recurrente plazos = recurrente("Tele", "5.00", FrecuenciaRecurrente.SEMANAL, dia(6, 6), dia(9, 12));
            plazos.setCuotasTotal(15);
            plazos.setCuotasPagadas(14);
            Recurrente octubre = recurrente("Seguro", "99.00", FrecuenciaRecurrente.ANUAL, dia(10, 1), dia(10, 1));
            conRecurrentes(luz, gimnasio, plazos, octubre);
            conPresupuestos(presupuesto(COMIDA, "300.00"), presupuesto(OCIO, "200.00"));

            ProyeccionMensual p = service.get(USUARIO, SEPTIEMBRE, dia(9, 10));

            assertThat(p.getEstado()).isEqualTo(EstadoProyeccion.EN_CURSO);
            assertThat(p.getDiasMes()).isEqualTo(30);
            assertThat(p.getDiasTranscurridos()).isEqualTo(10);
            assertThat(p.getGastoActual()).isEqualByComparingTo("350");
            assertThat(p.getGastoVariable()).as("el recurrente ya cobrado no se extrapola").isEqualByComparingTo("300");
            assertThat(p.getRitmoDiario()).isEqualByComparingTo("30.00");
            assertThat(p.getProyeccionVariable()).isEqualByComparingTo("600.00");
            // Luz 40 + gimnasio 14, 21 y 28 (30) + la ultima cuota de la tele (5).
            assertThat(p.getRecurrentesPendientes()).isEqualByComparingTo("75.00");
            assertThat(p.getCargosPendientes()).isEqualTo(5);
            assertThat(p.getGastoProyectado()).isEqualByComparingTo("1025.00");
            assertThat(p.getPresupuestoMensual()).isEqualByComparingTo("500.00");
        }

        @Test
        @DisplayName("mes cerrado: la proyeccion es el gasto real y no mira recurrentes")
        void cerrado() {
            con(mov(dia(9, 3), COMIDA, "100.00", null), mov(dia(9, 8), OCIO, "200.00", null));

            ProyeccionMensual p = service.get(USUARIO, SEPTIEMBRE, dia(10, 2));

            assertThat(p.getEstado()).isEqualTo(EstadoProyeccion.CERRADO);
            assertThat(p.getDiasTranscurridos()).isEqualTo(30);
            assertThat(p.getRitmoDiario()).isEqualByComparingTo("10.00");
            assertThat(p.getProyeccionVariable()).isEqualTo(bd("0.00"));
            assertThat(p.getRecurrentesPendientes()).isEqualTo(bd("0.00"));
            assertThat(p.getGastoProyectado()).isEqualByComparingTo("300.00");
            assertThat(p.getPresupuestoMensual()).as("sin presupuestos es null").isNull();
            verifyNoInteractions(recurrenteRepository);
        }

        @Test
        @DisplayName("mes futuro: solo los recurrentes que caen en el")
        void futuro() {
            conRecurrentes(recurrente("Luz", "40.00", FrecuenciaRecurrente.MENSUAL, dia(1, 20), dia(9, 20)));

            ProyeccionMensual p = service.get(USUARIO, YearMonth.of(2026, 11), dia(9, 10));

            assertThat(p.getEstado()).isEqualTo(EstadoProyeccion.FUTURO);
            assertThat(p.getDiasTranscurridos()).isZero();
            assertThat(p.getRitmoDiario()).isEqualTo(bd("0.00"));
            assertThat(p.getCargosPendientes()).isEqualTo(1);
            assertThat(p.getGastoProyectado()).isEqualByComparingTo("40.00");
        }

        @Test
        @DisplayName("el ultimo dia del mes ya no proyecta gasto variable")
        void ultimoDia() {
            con(mov(dia(9, 3), COMIDA, "300.00", null));

            ProyeccionMensual p = service.get(USUARIO, SEPTIEMBRE, dia(9, 30));

            assertThat(p.getProyeccionVariable()).isEqualByComparingTo("0");
            assertThat(p.getGastoProyectado()).isEqualByComparingTo("300.00");
        }
    }

    // ------------------------------------------------------------- formatos

    @Test
    @DisplayName("importes y porcentajes en formato espanol")
    void formatos() {
        assertThat(AnalisisService.euros(bd("1234.5"))).isEqualTo("1.234,50 €");
        assertThat(AnalisisService.euros(bd("0"))).isEqualTo("0,00 €");
        assertThat(AnalisisService.pct(bd("-33.33"))).isEqualTo("33 %");
        assertThat(AnalisisService.porcentaje(bd("1"), bd("3"))).isEqualByComparingTo("33.33");
        assertThat(AnalisisService.porcentaje(bd("1"), BigDecimal.ZERO)).isNull();
    }

    @Test
    @DisplayName("cargos pendientes: desde proximaFecha, dentro del rango y sin pasarse de las cuotas")
    void cargosPendientes() {
        Recurrente semanal = recurrente("X", "1.00", FrecuenciaRecurrente.SEMANAL, dia(9, 1), dia(9, 8));
        assertThat(AnalisisService.cargosPendientes(semanal, dia(9, 10), dia(9, 30)))
                .containsExactly(dia(9, 15), dia(9, 22), dia(9, 29));

        semanal.setCuotasTotal(3);
        semanal.setCuotasPagadas(1);
        assertThat(AnalisisService.cargosPendientes(semanal, dia(9, 1), dia(9, 30)))
                .as("quedan 2 cuotas: 8 y 15").containsExactly(dia(9, 8), dia(9, 15));
    }
}
