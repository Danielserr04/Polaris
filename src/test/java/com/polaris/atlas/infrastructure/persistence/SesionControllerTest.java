package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.CreateSesionInterface;
import com.polaris.atlas.application.in.DeleteSesionInterface;
import com.polaris.atlas.application.in.GetSesionInterface;
import com.polaris.atlas.application.in.ListSesionInterface;
import com.polaris.atlas.application.in.UpdateSesionInterface;
import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.SerieRegistro;
import com.polaris.atlas.domain.model.Sesion;
import com.polaris.atlas.domain.model.SesionFilter;
import com.polaris.atlas.domain.model.SesionNotFoundException;
import com.polaris.atlas.infrastructure.persistence.mapper.SerieRegistroFormDtoMapperImpl;
import com.polaris.atlas.infrastructure.persistence.mapper.SesionFilterMapperImpl;
import com.polaris.atlas.infrastructure.persistence.mapper.SesionFormDtoMapperImpl;
import com.polaris.atlas.infrastructure.persistence.mapper.SesionListDtoMapperImpl;
import com.polaris.atlas.infrastructure.persistence.mapper.SesionRequestDtoMapperImpl;
import com.polaris.shared.error.ValidationException;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sin filtros de seguridad: se prueba el binding, los codigos de estado y la
 * forma del JSON, no quien puede llamar (eso lo decide SecurityConfig). Que
 * el usuario sea siempre el actual y que nunca salga el usuarioId son parte
 * del contrato.
 */
@WebMvcTest(controllers = SesionController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({SesionRequestDtoMapperImpl.class, SesionFilterMapperImpl.class, SesionFormDtoMapperImpl.class,
        SerieRegistroFormDtoMapperImpl.class, SesionListDtoMapperImpl.class})
class SesionControllerTest {

    private static final Long USUARIO = 7L;
    private static final String HOY = LocalDate.now().toString();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateSesionInterface createSesion;

    @MockitoBean
    private GetSesionInterface getSesion;

    @MockitoBean
    private ListSesionInterface listSesion;

    @MockitoBean
    private UpdateSesionInterface updateSesion;

    @MockitoBean
    private DeleteSesionInterface deleteSesion;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @BeforeEach
    void usuarioAutenticado() {
        when(usuarioActual.id()).thenReturn(USUARIO);
    }

    private static Sesion sesion() {
        Ejercicio press = Ejercicio.builder().id(2L).nombre("Press banca").grupoMuscular("Pecho").build();
        Ejercicio dominadas = Ejercicio.builder().id(3L).usuarioId(USUARIO).nombre("Dominadas")
                .grupoMuscular("Espalda").build();
        return Sesion.builder().id(10L).usuarioId(USUARIO).rutinaId(4L).rutinaNombre("Push")
                .fecha(LocalDate.of(2026, 9, 30)).duracionMin(65).notas("Buen dia")
                .series(List.of(
                        SerieRegistro.builder().id(20L).usuarioId(USUARIO).ejercicioId(2L).ejercicio(press)
                                .numeroSerie(1).reps(8).pesoKg(new BigDecimal("82.50")).rpe(new BigDecimal("8.5")).build(),
                        SerieRegistro.builder().id(21L).usuarioId(USUARIO).ejercicioId(3L).ejercicio(dominadas)
                                .numeroSerie(1).reps(10).pesoKg(new BigDecimal("0.00")).build()))
                .build();
    }

    private static final String SERIE_VALIDA =
            "{\"ejercicioId\":2,\"numeroSerie\":1,\"reps\":8,\"pesoKg\":82.5,\"rpe\":8.5}";

    private static String cuerpo(String cabecera, String series) {
        return "{" + cabecera + "\"series\":" + series + "}";
    }

    private static String cuerpoConSerie(String serie) {
        return cuerpo("\"fecha\":\"" + HOY + "\",", "[" + serie + "]");
    }

    private static final String CUERPO_VALIDO = "{\"rutinaId\":4,\"fecha\":\"" + HOY + "\",\"duracionMin\":65,"
            + "\"notas\":\"Buen dia\",\"series\":[" + SERIE_VALIDA + ","
            + "{\"ejercicioId\":3,\"numeroSerie\":1,\"reps\":10,\"pesoKg\":0}]}";

    @Test
    @DisplayName("GET lista: 200, version ligera con rutina, duracion, numeroSeries, numeroEjercicios y volumen, sin series ni usuarioId, y los filtros llegan al caso de uso")
    void listaConFiltros() throws Exception {
        when(listSesion.list(eq(USUARIO), any(SesionFilter.class))).thenReturn(List.of(sesion()));

        mockMvc.perform(get("/api/atlas/sesion")
                        .param("desde", "2026-09-01").param("hasta", "2026-09-30").param("rutinaId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].fecha").value("2026-09-30"))
                .andExpect(jsonPath("$[0].rutinaId").value(4))
                .andExpect(jsonPath("$[0].rutinaNombre").value("Push"))
                .andExpect(jsonPath("$[0].duracionMin").value(65))
                .andExpect(jsonPath("$[0].numeroSeries").value(2))
                .andExpect(jsonPath("$[0].numeroEjercicios").value(2))
                .andExpect(jsonPath("$[0].volumen").value(660.0))
                .andExpect(jsonPath("$[0].series").doesNotExist())
                .andExpect(jsonPath("$[0].usuarioId").doesNotExist());

        ArgumentCaptor<SesionFilter> filtro = ArgumentCaptor.forClass(SesionFilter.class);
        verify(listSesion).list(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getDesde()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(filtro.getValue().getHasta()).isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(filtro.getValue().getRutinaId()).isEqualTo(4L);
    }

    @Test
    @DisplayName("GET lista: sin filtros llegan vacios, y un entreno libre sale sin rutina")
    void listaSinFiltros() throws Exception {
        Sesion libre = sesion();
        libre.setRutinaId(null);
        libre.setRutinaNombre(null);
        libre.setDuracionMin(null);
        when(listSesion.list(eq(USUARIO), any(SesionFilter.class))).thenReturn(List.of(libre));

        mockMvc.perform(get("/api/atlas/sesion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rutinaId").doesNotExist())
                .andExpect(jsonPath("$[0].rutinaNombre").doesNotExist())
                .andExpect(jsonPath("$[0].duracionMin").doesNotExist())
                .andExpect(jsonPath("$[0].numeroSeries").value(2));

        ArgumentCaptor<SesionFilter> filtro = ArgumentCaptor.forClass(SesionFilter.class);
        verify(listSesion).list(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getDesde()).isNull();
        assertThat(filtro.getValue().getHasta()).isNull();
        assertThat(filtro.getValue().getRutinaId()).isNull();
    }

    @Test
    @DisplayName("GET lista: 400 con una fecha o un rutinaId ilegibles")
    void listaFiltroInvalido() throws Exception {
        mockMvc.perform(get("/api/atlas/sesion").param("desde", "ayer")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/atlas/sesion").param("rutinaId", "abc")).andExpect(status().isBadRequest());

        verify(listSesion, never()).list(any(), any());
    }

    @Test
    @DisplayName("GET detalle: 200 con la ficha y sus series planas (ejercicio, peso, rpe), sin usuarioId, siempre para el usuario actual")
    void detalle() throws Exception {
        when(getSesion.get(USUARIO, 10L)).thenReturn(sesion());

        mockMvc.perform(get("/api/atlas/sesion/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.rutinaId").value(4))
                .andExpect(jsonPath("$.rutinaNombre").value("Push"))
                .andExpect(jsonPath("$.fecha").value("2026-09-30"))
                .andExpect(jsonPath("$.duracionMin").value(65))
                .andExpect(jsonPath("$.notas").value("Buen dia"))
                .andExpect(jsonPath("$.usuarioId").doesNotExist())
                .andExpect(jsonPath("$.series.length()").value(2))
                .andExpect(jsonPath("$.series[0].id").value(20))
                .andExpect(jsonPath("$.series[0].ejercicioId").value(2))
                .andExpect(jsonPath("$.series[0].ejercicioNombre").value("Press banca"))
                .andExpect(jsonPath("$.series[0].ejercicioGrupoMuscular").value("Pecho"))
                .andExpect(jsonPath("$.series[0].numeroSerie").value(1))
                .andExpect(jsonPath("$.series[0].reps").value(8))
                .andExpect(jsonPath("$.series[0].pesoKg").value(82.5))
                .andExpect(jsonPath("$.series[0].rpe").value(8.5))
                .andExpect(jsonPath("$.series[0].usuarioId").doesNotExist())
                .andExpect(jsonPath("$.series[1].ejercicioNombre").value("Dominadas"))
                .andExpect(jsonPath("$.series[1].pesoKg").value(0.0))
                .andExpect(jsonPath("$.series[1].rpe").doesNotExist());
    }

    @Test
    @DisplayName("GET detalle: 404 si no existe o es de otro usuario")
    void detalleNoEncontrado() throws Exception {
        when(getSesion.get(USUARIO, 99L)).thenThrow(new SesionNotFoundException(99L));

        mockMvc.perform(get("/api/atlas/sesion/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST: 201 con la ficha, el usuario es el actual y un usuarioId o id del body se ignoran")
    void crea() throws Exception {
        when(createSesion.create(eq(USUARIO), any(Sesion.class))).thenReturn(sesion());

        mockMvc.perform(post("/api/atlas/sesion").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":99,\"usuarioId\":999,\"rutinaId\":4,\"fecha\":\"" + HOY + "\","
                                + "\"series\":[{\"id\":5,\"usuarioId\":999,\"ejercicioId\":2,\"numeroSerie\":1,"
                                + "\"reps\":8,\"pesoKg\":82.5,\"rpe\":8.5}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.series.length()").value(2));

        ArgumentCaptor<Sesion> enviada = ArgumentCaptor.forClass(Sesion.class);
        verify(createSesion).create(eq(USUARIO), enviada.capture());
        assertThat(enviada.getValue().getId()).isNull();
        assertThat(enviada.getValue().getUsuarioId()).isNull();
        assertThat(enviada.getValue().getRutinaId()).isEqualTo(4L);
        assertThat(enviada.getValue().getFecha()).isEqualTo(LocalDate.now());
        assertThat(enviada.getValue().getSeries()).hasSize(1);
        SerieRegistro serie = enviada.getValue().getSeries().get(0);
        assertThat(serie.getId()).isNull();
        assertThat(serie.getUsuarioId()).isNull();
        assertThat(serie.getEjercicioId()).isEqualTo(2L);
        assertThat(serie.getPesoKg()).isEqualTo(new BigDecimal("82.5"));
        assertThat(serie.getRpe()).isEqualTo(new BigDecimal("8.5"));
    }

    @Test
    @DisplayName("POST: la rutina, la duracion, las notas y el rpe son opcionales (entreno libre)")
    void creaLibreYMinima() throws Exception {
        when(createSesion.create(eq(USUARIO), any(Sesion.class))).thenReturn(sesion());

        mockMvc.perform(post("/api/atlas/sesion").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoConSerie("{\"ejercicioId\":2,\"numeroSerie\":1,\"reps\":8,\"pesoKg\":80}")))
                .andExpect(status().isCreated());

        ArgumentCaptor<Sesion> enviada = ArgumentCaptor.forClass(Sesion.class);
        verify(createSesion).create(eq(USUARIO), enviada.capture());
        assertThat(enviada.getValue().getRutinaId()).isNull();
        assertThat(enviada.getValue().getDuracionMin()).isNull();
        assertThat(enviada.getValue().getNotas()).isNull();
        assertThat(enviada.getValue().getSeries().get(0).getRpe()).isNull();
    }

    @Test
    @DisplayName("POST: 400 con el mensaje legible si el servicio rechaza un ejercicio o una rutina ajenos")
    void creaRechazadaPorElServicio() throws Exception {
        when(createSesion.create(eq(USUARIO), any(Sesion.class)))
                .thenThrow(new ValidationException("El ejercicio 2 no existe o no esta disponible para ti"));

        mockMvc.perform(post("/api/atlas/sesion").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El ejercicio 2 no existe o no esta disponible para ti"));
    }

    @Test
    @DisplayName("POST: 400 sin fecha, con fecha futura o ilegible, con duracion fuera de 1-1440, notas largas, sin series, con mas de 200 o con una serie null, y no llega al caso de uso")
    void creaInvalida() throws Exception {
        String futura = LocalDate.now().plusDays(1).toString();
        String muchas = String.join(",", Collections.nCopies(201, SERIE_VALIDA));
        String[] cuerpos = {
                cuerpo("", "[" + SERIE_VALIDA + "]"),
                cuerpo("\"fecha\":\"" + futura + "\",", "[" + SERIE_VALIDA + "]"),
                cuerpo("\"fecha\":\"ayer\",", "[" + SERIE_VALIDA + "]"),
                cuerpo("\"fecha\":\"" + HOY + "\",\"duracionMin\":0,", "[" + SERIE_VALIDA + "]"),
                cuerpo("\"fecha\":\"" + HOY + "\",\"duracionMin\":1441,", "[" + SERIE_VALIDA + "]"),
                cuerpo("\"fecha\":\"" + HOY + "\",\"notas\":\"" + "n".repeat(2001) + "\",", "[" + SERIE_VALIDA + "]"),
                "{\"fecha\":\"" + HOY + "\"}",
                cuerpo("\"fecha\":\"" + HOY + "\",", "[]"),
                cuerpo("\"fecha\":\"" + HOY + "\",", "[" + muchas + "]"),
                cuerpo("\"fecha\":\"" + HOY + "\",", "[null]")
        };

        for (String cuerpo : cuerpos) {
            mockMvc.perform(post("/api/atlas/sesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest());
        }

        verify(createSesion, never()).create(any(), any());
    }

    @Test
    @DisplayName("POST: 400 con una serie sin ejercicio, sin numero, con numero 0, reps 0, peso negativo, peso > 1000, mas de 2 decimales, sin peso o rpe fuera de 1-10")
    void creaSerieInvalida() throws Exception {
        String[] series = {
                "{\"numeroSerie\":1,\"reps\":8,\"pesoKg\":80}",
                "{\"ejercicioId\":2,\"reps\":8,\"pesoKg\":80}",
                "{\"ejercicioId\":2,\"numeroSerie\":0,\"reps\":8,\"pesoKg\":80}",
                "{\"ejercicioId\":2,\"numeroSerie\":1000,\"reps\":8,\"pesoKg\":80}",
                "{\"ejercicioId\":2,\"numeroSerie\":1,\"pesoKg\":80}",
                "{\"ejercicioId\":2,\"numeroSerie\":1,\"reps\":0,\"pesoKg\":80}",
                "{\"ejercicioId\":2,\"numeroSerie\":1,\"reps\":1000,\"pesoKg\":80}",
                "{\"ejercicioId\":2,\"numeroSerie\":1,\"reps\":8}",
                "{\"ejercicioId\":2,\"numeroSerie\":1,\"reps\":8,\"pesoKg\":-1}",
                "{\"ejercicioId\":2,\"numeroSerie\":1,\"reps\":8,\"pesoKg\":1000.01}",
                "{\"ejercicioId\":2,\"numeroSerie\":1,\"reps\":8,\"pesoKg\":80.005}",
                "{\"ejercicioId\":2,\"numeroSerie\":1,\"reps\":8,\"pesoKg\":80,\"rpe\":0.5}",
                "{\"ejercicioId\":2,\"numeroSerie\":1,\"reps\":8,\"pesoKg\":80,\"rpe\":10.5}",
                "{\"ejercicioId\":2,\"numeroSerie\":1,\"reps\":8,\"pesoKg\":80,\"rpe\":8.25}"
        };

        for (String serie : series) {
            mockMvc.perform(post("/api/atlas/sesion").contentType(MediaType.APPLICATION_JSON)
                            .content(cuerpoConSerie(serie)))
                    .andExpect(status().isBadRequest());
        }

        verify(createSesion, never()).create(any(), any());
    }

    @Test
    @DisplayName("PUT: 200 con la ficha actualizada, para el usuario actual")
    void actualiza() throws Exception {
        when(updateSesion.update(eq(USUARIO), eq(10L), any(Sesion.class))).thenReturn(sesion());

        mockMvc.perform(put("/api/atlas/sesion/10").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rutinaNombre").value("Push"))
                .andExpect(jsonPath("$.series.length()").value(2));

        ArgumentCaptor<Sesion> enviada = ArgumentCaptor.forClass(Sesion.class);
        verify(updateSesion).update(eq(USUARIO), eq(10L), enviada.capture());
        assertThat(enviada.getValue().getSeries()).hasSize(2);
    }

    @Test
    @DisplayName("PUT: 404 si es de otro usuario y 400 si el servicio la rechaza")
    void actualizaRechazado() throws Exception {
        when(updateSesion.update(eq(USUARIO), eq(12L), any(Sesion.class)))
                .thenThrow(new SesionNotFoundException(12L));
        when(updateSesion.update(eq(USUARIO), eq(14L), any(Sesion.class)))
                .thenThrow(new ValidationException("El numero de serie 1 esta repetido en el ejercicio 2"));

        mockMvc.perform(put("/api/atlas/sesion/12").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/atlas/sesion/14").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT: 400 con el cuerpo invalido, y no llega al caso de uso")
    void actualizaInvalido() throws Exception {
        mockMvc.perform(put("/api/atlas/sesion/10").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("\"fecha\":\"" + HOY + "\",", "[]")))
                .andExpect(status().isBadRequest());

        verify(updateSesion, never()).update(any(), any(), any());
    }

    @Test
    @DisplayName("DELETE: 204 y siempre para el usuario actual")
    void borra() throws Exception {
        mockMvc.perform(delete("/api/atlas/sesion/10"))
                .andExpect(status().isNoContent());

        verify(deleteSesion).delete(USUARIO, 10L);
    }

    @Test
    @DisplayName("DELETE: 404 si no existe o es de otro usuario")
    void borraNoEncontrada() throws Exception {
        doThrow(new SesionNotFoundException(12L)).when(deleteSesion).delete(USUARIO, 12L);

        mockMvc.perform(delete("/api/atlas/sesion/12")).andExpect(status().isNotFound());
    }
}
