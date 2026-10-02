package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.application.in.DescartarRecordatorioInterface;
import com.polaris.nucleo.application.in.GetRecordatorioInterface;
import com.polaris.nucleo.application.in.ListRecordatorioInterface;
import com.polaris.nucleo.application.in.ListRecordatorioPendienteInterface;
import com.polaris.nucleo.application.in.UpdateRecordatorioInterface;
import com.polaris.nucleo.domain.model.AvisoRecordatorio;
import com.polaris.nucleo.domain.model.Recordatorio;
import com.polaris.nucleo.domain.model.TipoRecordatorio;
import com.polaris.nucleo.infrastructure.persistence.mapper.RecordatorioFormDtoMapperImpl;
import com.polaris.nucleo.infrastructure.persistence.mapper.RecordatorioListDtoMapperImpl;
import com.polaris.nucleo.infrastructure.persistence.mapper.RecordatorioPendienteDtoMapperImpl;
import com.polaris.nucleo.infrastructure.persistence.mapper.RecordatorioRequestDtoMapperImpl;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Binding y forma del JSON: el tipo en la ruta, la hora como HH:mm, los dias
 * como numeros 1-7 y nada de usuarioId ni marcas internas.
 */
@WebMvcTest(controllers = RecordatorioController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({RecordatorioRequestDtoMapperImpl.class, RecordatorioFormDtoMapperImpl.class,
        RecordatorioListDtoMapperImpl.class, RecordatorioPendienteDtoMapperImpl.class})
class RecordatorioControllerTest {

    private static final Long USUARIO = 7L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListRecordatorioInterface listRecordatorio;
    @MockitoBean
    private GetRecordatorioInterface getRecordatorio;
    @MockitoBean
    private UpdateRecordatorioInterface updateRecordatorio;
    @MockitoBean
    private ListRecordatorioPendienteInterface listPendientes;
    @MockitoBean
    private DescartarRecordatorioInterface descartar;
    @MockitoBean
    private UsuarioActual usuarioActual;

    @BeforeEach
    void usuarioAutenticado() {
        when(usuarioActual.id()).thenReturn(USUARIO);
    }

    @Test
    @DisplayName("GET lista: hora HH:mm, dias en numeros y sin campos internos")
    void lista() throws Exception {
        when(listRecordatorio.list(USUARIO)).thenReturn(List.of(Recordatorio.builder().id(1L).usuarioId(USUARIO)
                .tipo(TipoRecordatorio.ENTRENO).activo(true).hora(LocalTime.of(18, 0))
                .dias(EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY)).avisadoEn(LocalDate.of(2026, 10, 2)).build()));

        mockMvc.perform(get("/api/nucleo/recordatorio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("ENTRENO"))
                .andExpect(jsonPath("$[0].hora").value("18:00"))
                .andExpect(jsonPath("$[0].dias[0]").value(1))
                .andExpect(jsonPath("$[0].dias[1]").value(5))
                .andExpect(jsonPath("$[0].usuarioId").doesNotExist())
                .andExpect(jsonPath("$[0].avisadoEn").doesNotExist());
    }

    @Test
    @DisplayName("PUT /{tipo}: el tipo sale de la ruta y la hora acepta HH:mm")
    void guardar() throws Exception {
        when(updateRecordatorio.update(eq(USUARIO), any(Recordatorio.class))).thenAnswer(inv -> inv.getArgument(1));

        mockMvc.perform(put("/api/nucleo/recordatorio/COMIDAS").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":true,\"hora\":\"20:30\",\"dias\":[3,1,3]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("COMIDAS"))
                .andExpect(jsonPath("$.hora").value("20:30"))
                .andExpect(jsonPath("$.dias.length()").value(2));

        ArgumentCaptor<Recordatorio> captor = ArgumentCaptor.forClass(Recordatorio.class);
        verify(updateRecordatorio).update(eq(USUARIO), captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoRecordatorio.COMIDAS);
        assertThat(captor.getValue().getDias()).containsExactly(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
    }

    @Test
    @DisplayName("PUT con un dia fuera de 1-7, sin dias o con tipo desconocido: 400")
    void validaciones() throws Exception {
        mockMvc.perform(put("/api/nucleo/recordatorio/COMIDAS").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":true,\"hora\":\"20:30\",\"dias\":[8]}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/nucleo/recordatorio/COMIDAS").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":true,\"hora\":\"20:30\",\"dias\":[]}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/nucleo/recordatorio/SIESTA").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":true,\"hora\":\"20:30\",\"dias\":[1]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /pendientes no se confunde con /{tipo}")
    void pendientes() throws Exception {
        when(listPendientes.pendientes(eq(USUARIO), any())).thenReturn(List.of(
                new AvisoRecordatorio(TipoRecordatorio.GASTOS, "¿Has gastado algo hoy?", "Nada hoy.", "/kuiper")));

        mockMvc.perform(get("/api/nucleo/recordatorio/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("GASTOS"))
                .andExpect(jsonPath("$[0].enlace").value("/kuiper"));
    }

    @Test
    @DisplayName("POST /{tipo}/descartar: 204")
    void descartar() throws Exception {
        mockMvc.perform(post("/api/nucleo/recordatorio/ENTRENO/descartar"))
                .andExpect(status().isNoContent());

        verify(descartar).descartar(eq(USUARIO), eq(TipoRecordatorio.ENTRENO), any(LocalDate.class));
    }
}
