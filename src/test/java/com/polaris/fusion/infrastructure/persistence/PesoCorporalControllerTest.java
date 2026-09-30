package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.in.CreatePesoCorporalInterface;
import com.polaris.fusion.application.in.ListPesoCorporalInterface;
import com.polaris.fusion.domain.model.PesoCorporal;
import com.polaris.fusion.domain.model.PesoCorporalFilter;
import com.polaris.fusion.infrastructure.persistence.mapper.PesoCorporalDtoMapperImpl;
import com.polaris.fusion.infrastructure.persistence.mapper.PesoCorporalFilterMapperImpl;
import com.polaris.fusion.infrastructure.persistence.mapper.PesoCorporalRequestDtoMapperImpl;
import com.polaris.shared.security.UsuarioActual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sin filtros de seguridad, como el resto de slices: se prueba el binding, la
 * validacion (400) y la forma del JSON. El 401 sin token lo decide
 * SecurityConfig (anyRequest().authenticated()) y se comprueba contra la app
 * real, no en este slice.
 */
@WebMvcTest(controllers = PesoCorporalController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({PesoCorporalDtoMapperImpl.class, PesoCorporalFilterMapperImpl.class, PesoCorporalRequestDtoMapperImpl.class})
class PesoCorporalControllerTest {

    private static final Long USUARIO = 7L;
    private static final LocalDate DIA = LocalDate.of(2026, 9, 30);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListPesoCorporalInterface listPesoCorporal;

    @MockitoBean
    private CreatePesoCorporalInterface createPesoCorporal;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @BeforeEach
    void usuarioAutenticado() {
        when(usuarioActual.id()).thenReturn(USUARIO);
    }

    private static PesoCorporal peso(LocalDate fecha, String kg, String grasa, String notas) {
        return PesoCorporal.builder().fecha(fecha).pesoKg(new BigDecimal(kg))
                .grasaPct(grasa == null ? null : new BigDecimal(grasa)).notas(notas).build();
    }

    @Test
    @DisplayName("GET 200: lista con el rango pedido y el usuario autenticado, pesos como numeros")
    void listaConRango() throws Exception {
        when(listPesoCorporal.list(eq(USUARIO), any())).thenReturn(List.of(
                peso(DIA, "78.50", "15.2", "tras entrenar"),
                peso(DIA.minusDays(1), "78.90", null, null)));

        mockMvc.perform(get("/api/fusion/peso").param("desde", "2026-09-01").param("hasta", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fecha").value("2026-09-30"))
                .andExpect(jsonPath("$[0].pesoKg").value(78.5))
                .andExpect(jsonPath("$[0].grasaPct").value(15.2))
                .andExpect(jsonPath("$[0].notas").value("tras entrenar"))
                .andExpect(jsonPath("$[1].grasaPct").doesNotExist())
                .andExpect(jsonPath("$[1].notas").doesNotExist());

        ArgumentCaptor<PesoCorporalFilter> filtro = ArgumentCaptor.forClass(PesoCorporalFilter.class);
        verify(listPesoCorporal).list(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getDesde()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(filtro.getValue().getHasta()).isEqualTo(DIA);
    }

    @Test
    @DisplayName("GET 200 sin filtros: rango abierto y lista vacia si no hay pesos")
    void listaSinFiltros() throws Exception {
        when(listPesoCorporal.list(eq(USUARIO), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/fusion/peso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        ArgumentCaptor<PesoCorporalFilter> filtro = ArgumentCaptor.forClass(PesoCorporalFilter.class);
        verify(listPesoCorporal).list(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getDesde()).isNull();
        assertThat(filtro.getValue().getHasta()).isNull();
    }

    @Test
    @DisplayName("GET 400 con una fecha invalida, y no llega al servicio")
    void fechaInvalidaDa400() throws Exception {
        mockMvc.perform(get("/api/fusion/peso").param("desde", "30-09-2026"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/fusion/peso").param("hasta", "ayer"))
                .andExpect(status().isBadRequest());

        verify(listPesoCorporal, never()).list(any(), any());
    }

    @Test
    @DisplayName("POST 201: apunta el peso del usuario autenticado y devuelve el DTO de Fusion")
    void apuntaPeso() throws Exception {
        when(createPesoCorporal.create(eq(USUARIO), any())).thenReturn(peso(DIA, "78.50", "15.2", "ayuno"));

        mockMvc.perform(post("/api/fusion/peso").contentType(MediaType.APPLICATION_JSON).content("""
                        {"fecha":"2026-09-30","pesoKg":78.50,"grasaPct":15.2,"notas":"ayuno"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fecha").value("2026-09-30"))
                .andExpect(jsonPath("$.pesoKg").value(78.5))
                .andExpect(jsonPath("$.grasaPct").value(15.2))
                .andExpect(jsonPath("$.notas").value("ayuno"))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.usuarioId").doesNotExist());

        ArgumentCaptor<PesoCorporal> apuntado = ArgumentCaptor.forClass(PesoCorporal.class);
        verify(createPesoCorporal).create(eq(USUARIO), apuntado.capture());
        assertThat(apuntado.getValue().getFecha()).isEqualTo(DIA);
        assertThat(apuntado.getValue().getPesoKg()).isEqualByComparingTo("78.50");
        assertThat(apuntado.getValue().getGrasaPct()).isEqualByComparingTo("15.2");
    }

    @Test
    @DisplayName("POST 201 con solo fecha y peso: grasa y notas opcionales")
    void apuntaSoloFechaYPeso() throws Exception {
        when(createPesoCorporal.create(eq(USUARIO), any())).thenReturn(peso(DIA, "78.50", null, null));

        mockMvc.perform(post("/api/fusion/peso").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2026-09-30\",\"pesoKg\":78.5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.grasaPct").doesNotExist());
    }

    @Test
    @DisplayName("POST 400 si falta fecha o peso, el peso no es positivo, no cabe, la grasa pasa de 100 o la fecha es futura")
    void datosInvalidosDan400() throws Exception {
        String futura = LocalDate.now().plusDays(1).toString();
        List<String> cuerpos = List.of(
                "{\"pesoKg\":78.5}",
                "{\"fecha\":\"2026-09-30\"}",
                "{\"fecha\":\"2026-09-30\",\"pesoKg\":0}",
                "{\"fecha\":\"2026-09-30\",\"pesoKg\":-5}",
                "{\"fecha\":\"2026-09-30\",\"pesoKg\":1000.5}",
                "{\"fecha\":\"2026-09-30\",\"pesoKg\":78.555}",
                "{\"fecha\":\"2026-09-30\",\"pesoKg\":78.5,\"grasaPct\":100.5}",
                "{\"fecha\":\"2026-09-30\",\"pesoKg\":78.5,\"grasaPct\":-1}",
                "{\"fecha\":\"" + futura + "\",\"pesoKg\":78.5}",
                "{\"fecha\":\"hoy\",\"pesoKg\":78.5}",
                "{\"fecha\":\"2026-09-30\",\"pesoKg\":\"pesado\"}");

        for (String cuerpo : cuerpos) {
            mockMvc.perform(post("/api/fusion/peso").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest());
        }

        verify(createPesoCorporal, never()).create(any(), any());
    }

    @Test
    @DisplayName("un usuarioId en el body se ignora: siempre el del JWT")
    void ignoraUsuarioIdDelBody() throws Exception {
        when(createPesoCorporal.create(eq(USUARIO), any())).thenReturn(peso(DIA, "78.50", null, null));

        mockMvc.perform(post("/api/fusion/peso").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":99,\"fecha\":\"2026-09-30\",\"pesoKg\":78.5}"))
                .andExpect(status().isCreated());

        verify(createPesoCorporal).create(eq(USUARIO), any());
    }
}
