package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.GetComparativaCategoriasInterface;
import com.polaris.kuiper.application.in.GetEvolucionInterface;
import com.polaris.kuiper.application.in.GetProyeccionInterface;
import com.polaris.kuiper.application.in.ListComerciosFrecuentesInterface;
import com.polaris.kuiper.application.in.ListInsightsInterface;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaComparada;
import com.polaris.kuiper.domain.model.ComercioFrecuente;
import com.polaris.kuiper.domain.model.ComparativaCategorias;
import com.polaris.kuiper.domain.model.EstadoProyeccion;
import com.polaris.kuiper.domain.model.EvolucionMensual;
import com.polaris.kuiper.domain.model.Insight;
import com.polaris.kuiper.domain.model.ProyeccionMensual;
import com.polaris.kuiper.domain.model.SeveridadInsight;
import com.polaris.kuiper.domain.model.TipoInsight;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.kuiper.infrastructure.persistence.mapper.ComercioFrecuenteDtoMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.ComparativaCategoriasDtoMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.EvolucionMensualDtoMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.InsightDtoMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.ProyeccionMensualDtoMapperImpl;
import com.polaris.shared.error.ValidationException;
import com.polaris.shared.security.UsuarioActual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sin filtros de seguridad: se prueba el binding de los query params, los
 * valores por defecto, los 400 y la forma del JSON.
 */
@WebMvcTest(controllers = AnalisisController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({EvolucionMensualDtoMapperImpl.class, ComparativaCategoriasDtoMapperImpl.class,
        ComercioFrecuenteDtoMapperImpl.class, InsightDtoMapperImpl.class, ProyeccionMensualDtoMapperImpl.class})
class AnalisisControllerTest {

    private static final Long USUARIO = 7L;
    private static final String BASE = "/api/kuiper/analisis";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetEvolucionInterface getEvolucion;

    @MockitoBean
    private GetComparativaCategoriasInterface getComparativaCategorias;

    @MockitoBean
    private ListComerciosFrecuentesInterface listComerciosFrecuentes;

    @MockitoBean
    private ListInsightsInterface listInsights;

    @MockitoBean
    private GetProyeccionInterface getProyeccion;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @BeforeEach
    void usuarioAutenticado() {
        when(usuarioActual.id()).thenReturn(USUARIO);
    }

    @Test
    @DisplayName("evolucion: periodo como texto, importes como numeros y tasa nula omitida")
    void evolucion() throws Exception {
        when(getEvolucion.get(USUARIO, YearMonth.of(2026, 9), 2)).thenReturn(List.of(
                EvolucionMensual.builder().periodo(YearMonth.of(2026, 8)).ingresos(new BigDecimal("0.00"))
                        .gastos(new BigDecimal("50.00")).balance(new BigDecimal("-50.00")).build(),
                EvolucionMensual.builder().periodo(YearMonth.of(2026, 9)).ingresos(new BigDecimal("2000.00"))
                        .gastos(new BigDecimal("1500.00")).balance(new BigDecimal("500.00"))
                        .tasaAhorro(new BigDecimal("25.00")).build()));

        mockMvc.perform(get(BASE + "/evolucion").param("meses", "2").param("hasta", "2026-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].periodo").value("2026-08"))
                .andExpect(jsonPath("$[0].tasaAhorro").doesNotExist())
                .andExpect(jsonPath("$[1].ingresos").value(2000.0))
                .andExpect(jsonPath("$[1].tasaAhorro").value(25.0));
    }

    @Test
    @DisplayName("evolucion: por defecto 6 meses hasta el mes actual")
    void evolucionPorDefecto() throws Exception {
        when(getEvolucion.get(eq(USUARIO), any(), anyInt())).thenReturn(List.of());

        mockMvc.perform(get(BASE + "/evolucion")).andExpect(status().isOk());

        verify(getEvolucion).get(USUARIO, YearMonth.now(), 6);
    }

    @Test
    @DisplayName("evolucion: 400 si el servicio rechaza meses y si hasta no es yyyy-MM")
    void evolucion400() throws Exception {
        when(getEvolucion.get(eq(USUARIO), any(), eq(30))).thenThrow(new ValidationException("meses"));

        mockMvc.perform(get(BASE + "/evolucion").param("meses", "30")).andExpect(status().isBadRequest());
        mockMvc.perform(get(BASE + "/evolucion").param("hasta", "09-2026")).andExpect(status().isBadRequest());
        mockMvc.perform(get(BASE + "/evolucion").param("meses", "seis")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("categorias: rango anterior y una fila con la categoria aplanada")
    void categorias() throws Exception {
        Categoria ocio = Categoria.builder().id(11L).nombre("Ocio").color("#ff0000").icono("film")
                .tipo(TipoMovimiento.GASTO).build();
        when(getComparativaCategorias.get(USUARIO, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
                .thenReturn(ComparativaCategorias.builder()
                        .desde(LocalDate.of(2026, 9, 1)).hasta(LocalDate.of(2026, 9, 30))
                        .anteriorDesde(LocalDate.of(2026, 8, 1)).anteriorHasta(LocalDate.of(2026, 8, 31))
                        .total(new BigDecimal("100.00")).totalAnterior(new BigDecimal("0.00"))
                        .categorias(List.of(CategoriaComparada.builder().categoria(ocio)
                                .gastado(new BigDecimal("100.00")).porcentaje(new BigDecimal("100.00"))
                                .gastadoAnterior(new BigDecimal("0.00")).diferencia(new BigDecimal("100.00"))
                                .build()))
                        .build());

        mockMvc.perform(get(BASE + "/categorias").param("desde", "2026-09-01").param("hasta", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anteriorDesde").value("2026-08-01"))
                .andExpect(jsonPath("$.anteriorHasta").value("2026-08-31"))
                .andExpect(jsonPath("$.total").value(100.0))
                .andExpect(jsonPath("$.categorias[0].categoriaId").value(11))
                .andExpect(jsonPath("$.categorias[0].categoriaNombre").value("Ocio"))
                .andExpect(jsonPath("$.categorias[0].categoriaColor").value("#ff0000"))
                .andExpect(jsonPath("$.categorias[0].porcentaje").value(100.0))
                .andExpect(jsonPath("$.categorias[0].variacion").doesNotExist());
    }

    @Test
    @DisplayName("categorias: sin fechas usa el mes actual entero; fecha invalida da 400")
    void categoriasPorDefecto() throws Exception {
        when(getComparativaCategorias.get(eq(USUARIO), any(), any()))
                .thenReturn(ComparativaCategorias.builder().categorias(List.of()).build());

        mockMvc.perform(get(BASE + "/categorias")).andExpect(status().isOk());
        verify(getComparativaCategorias).get(USUARIO, YearMonth.now().atDay(1), YearMonth.now().atEndOfMonth());

        mockMvc.perform(get(BASE + "/categorias").param("desde", "2026-02-30")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("comercios: limite por defecto 10 y la lista en el JSON")
    void comercios() throws Exception {
        when(listComerciosFrecuentes.list(eq(USUARIO), any(), any(), eq(10))).thenReturn(List.of(
                ComercioFrecuente.builder().nombre("Mercadona").total(new BigDecimal("60.00")).veces(3)
                        .ticketMedio(new BigDecimal("20.00")).build()));

        mockMvc.perform(get(BASE + "/comercios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Mercadona"))
                .andExpect(jsonPath("$[0].veces").value(3))
                .andExpect(jsonPath("$[0].ticketMedio").value(20.0));
    }

    @Test
    @DisplayName("comercios: pasa desde, hasta y limite tal cual")
    void comerciosConParametros() throws Exception {
        when(listComerciosFrecuentes.list(any(), any(), any(), anyInt())).thenReturn(List.of());

        mockMvc.perform(get(BASE + "/comercios").param("desde", "2026-01-01").param("hasta", "2026-06-30")
                        .param("limite", "5"))
                .andExpect(status().isOk());

        verify(listComerciosFrecuentes).list(USUARIO, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 5);
    }

    @Test
    @DisplayName("insights: enums como texto y hoy del servidor")
    void insights() throws Exception {
        when(listInsights.list(eq(USUARIO), eq(YearMonth.of(2026, 9)), any())).thenReturn(List.of(
                Insight.builder().tipo(TipoInsight.TASA_AHORRO).severidad(SeveridadInsight.BIEN)
                        .titulo("Ahorras el 25 % de tus ingresos").texto("Bien.").build()));

        mockMvc.perform(get(BASE + "/insights").param("periodo", "2026-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("TASA_AHORRO"))
                .andExpect(jsonPath("$[0].severidad").value("BIEN"))
                .andExpect(jsonPath("$[0].titulo").value("Ahorras el 25 % de tus ingresos"));

        verify(listInsights).list(USUARIO, YearMonth.of(2026, 9), LocalDate.now());
    }

    @Test
    @DisplayName("insights: periodo invalido da 400 y no llega al servicio")
    void insights400() throws Exception {
        mockMvc.perform(get(BASE + "/insights").param("periodo", "2026-13")).andExpect(status().isBadRequest());

        verify(listInsights, never()).list(any(), any(), any());
    }

    @Test
    @DisplayName("proyeccion: sin periodo usa el mes actual; estado como texto")
    void proyeccion() throws Exception {
        when(getProyeccion.get(eq(USUARIO), any(), any())).thenReturn(ProyeccionMensual.builder()
                .periodo(YearMonth.of(2026, 9)).estado(EstadoProyeccion.EN_CURSO).diasMes(30).diasTranscurridos(10)
                .gastoActual(new BigDecimal("350.00")).gastoVariable(new BigDecimal("300.00"))
                .ritmoDiario(new BigDecimal("30.00")).proyeccionVariable(new BigDecimal("600.00"))
                .recurrentesPendientes(new BigDecimal("75.00")).cargosPendientes(5)
                .gastoProyectado(new BigDecimal("1025.00")).build());

        mockMvc.perform(get(BASE + "/proyeccion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo").value("2026-09"))
                .andExpect(jsonPath("$.estado").value("EN_CURSO"))
                .andExpect(jsonPath("$.gastoProyectado").value(1025.0))
                .andExpect(jsonPath("$.cargosPendientes").value(5))
                .andExpect(jsonPath("$.presupuestoMensual").doesNotExist());

        LocalDate hoy = LocalDate.now();
        verify(getProyeccion).get(USUARIO, YearMonth.from(hoy), hoy);
    }
}
