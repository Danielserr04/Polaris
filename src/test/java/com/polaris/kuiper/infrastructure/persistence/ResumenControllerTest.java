package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.GetResumenAnualInterface;
import com.polaris.kuiper.application.in.GetResumenMensualInterface;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.EstadoPresupuesto;
import com.polaris.kuiper.domain.model.GastoAnualCategoria;
import com.polaris.kuiper.domain.model.GastoCategoria;
import com.polaris.kuiper.domain.model.ResumenAnual;
import com.polaris.kuiper.domain.model.ResumenMensual;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.kuiper.infrastructure.persistence.mapper.ResumenAnualDtoMapperImpl;
import com.polaris.kuiper.infrastructure.persistence.mapper.ResumenMensualDtoMapperImpl;
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
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sin filtros de seguridad: binding, codigos de estado y forma del JSON. */
@WebMvcTest(controllers = ResumenController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({ResumenMensualDtoMapperImpl.class, ResumenAnualDtoMapperImpl.class})
class ResumenControllerTest {

    private static final Long USUARIO = 7L;

    private static final Categoria COMIDA = Categoria.builder().id(10L).usuarioId(USUARIO).nombre("Comida")
            .color("#e0b04a").icono("utensils").tipo(TipoMovimiento.GASTO).build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetResumenMensualInterface getResumenMensual;

    @MockitoBean
    private GetResumenAnualInterface getResumenAnual;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @BeforeEach
    void usuarioAutenticado() {
        when(usuarioActual.id()).thenReturn(USUARIO);
    }

    @Test
    @DisplayName("GET mensual: cada fila lleva porcentaje, umbral y estado, y el resumen sus totales de presupuesto")
    void mensualConEstado() throws Exception {
        when(getResumenMensual.get(USUARIO, YearMonth.of(2026, 9))).thenReturn(ResumenMensual.builder()
                .periodo(YearMonth.of(2026, 9))
                .ingresos(new BigDecimal("1500.00")).gastos(new BigDecimal("220.00"))
                .balance(new BigDecimal("1280.00"))
                .gastoPorCategoria(List.of(GastoCategoria.builder().categoria(COMIDA)
                        .gastado(new BigDecimal("220.00")).limiteMensual(new BigDecimal("250.00"))
                        .restante(new BigDecimal("30.00")).porcentaje(new BigDecimal("88.0"))
                        .porcentajeAlerta(80).estado(EstadoPresupuesto.AVISO).build()))
                .presupuestoTotal(new BigDecimal("250.00")).categoriasEnAviso(1).categoriasExcedidas(0)
                .build());

        mockMvc.perform(get("/api/kuiper/resumen").param("periodo", "2026-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo").value("2026-09"))
                .andExpect(jsonPath("$.presupuestoTotal").value(250.00))
                .andExpect(jsonPath("$.categoriasEnAviso").value(1))
                .andExpect(jsonPath("$.categoriasExcedidas").value(0))
                .andExpect(jsonPath("$.gastoPorCategoria[0].categoriaNombre").value("Comida"))
                .andExpect(jsonPath("$.gastoPorCategoria[0].porcentaje").value(88.0))
                .andExpect(jsonPath("$.gastoPorCategoria[0].porcentajeAlerta").value(80))
                .andExpect(jsonPath("$.gastoPorCategoria[0].estado").value("AVISO"));
    }

    @Test
    @DisplayName("GET anual: 200 con una fila por presupuesto anual para el usuario actual")
    void anual() throws Exception {
        when(getResumenAnual.get(USUARIO, 2026)).thenReturn(ResumenAnual.builder().anio(2026)
                .presupuestos(List.of(GastoAnualCategoria.builder().categoria(COMIDA)
                        .gastado(new BigDecimal("3100.00")).limite(new BigDecimal("3000.00"))
                        .restante(new BigDecimal("-100.00")).porcentaje(new BigDecimal("103.3"))
                        .porcentajeAlerta(80).estado(EstadoPresupuesto.EXCEDIDO).build()))
                .build());

        mockMvc.perform(get("/api/kuiper/resumen/anual").param("anio", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anio").value(2026))
                .andExpect(jsonPath("$.presupuestos.length()").value(1))
                .andExpect(jsonPath("$.presupuestos[0].categoriaId").value(10))
                .andExpect(jsonPath("$.presupuestos[0].categoriaNombre").value("Comida"))
                .andExpect(jsonPath("$.presupuestos[0].categoriaColor").value("#e0b04a"))
                .andExpect(jsonPath("$.presupuestos[0].gastado").value(3100.00))
                .andExpect(jsonPath("$.presupuestos[0].limite").value(3000.00))
                .andExpect(jsonPath("$.presupuestos[0].restante").value(-100.00))
                .andExpect(jsonPath("$.presupuestos[0].porcentaje").value(103.3))
                .andExpect(jsonPath("$.presupuestos[0].porcentajeAlerta").value(80))
                .andExpect(jsonPath("$.presupuestos[0].estado").value("EXCEDIDO"));
    }

    @Test
    @DisplayName("GET anual sin anio usa el anio actual")
    void anualPorDefecto() throws Exception {
        int actual = Year.now().getValue();
        when(getResumenAnual.get(USUARIO, actual))
                .thenReturn(ResumenAnual.builder().anio(actual).presupuestos(List.of()).build());

        mockMvc.perform(get("/api/kuiper/resumen/anual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anio").value(actual))
                .andExpect(jsonPath("$.presupuestos.length()").value(0));
    }

    @Test
    @DisplayName("GET anual: 400 si anio no es un numero o esta fuera de rango")
    void anualInvalido() throws Exception {
        mockMvc.perform(get("/api/kuiper/resumen/anual").param("anio", "dosmil"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/kuiper/resumen/anual").param("anio", "20260"))
                .andExpect(status().isBadRequest());

        verify(getResumenAnual, never()).get(any(), anyInt());
    }
}
