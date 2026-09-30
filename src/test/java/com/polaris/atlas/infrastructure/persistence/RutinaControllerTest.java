package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.CreateRutinaInterface;
import com.polaris.atlas.application.in.DeleteRutinaInterface;
import com.polaris.atlas.application.in.GetRutinaInterface;
import com.polaris.atlas.application.in.ListRutinaInterface;
import com.polaris.atlas.application.in.UpdateRutinaInterface;
import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.Rutina;
import com.polaris.atlas.domain.model.RutinaEjercicio;
import com.polaris.atlas.domain.model.RutinaFilter;
import com.polaris.atlas.domain.model.RutinaNotFoundException;
import com.polaris.atlas.infrastructure.persistence.mapper.RutinaFilterMapperImpl;
import com.polaris.atlas.infrastructure.persistence.mapper.RutinaEjercicioFormDtoMapperImpl;
import com.polaris.atlas.infrastructure.persistence.mapper.RutinaFormDtoMapperImpl;
import com.polaris.atlas.infrastructure.persistence.mapper.RutinaListDtoMapperImpl;
import com.polaris.atlas.infrastructure.persistence.mapper.RutinaRequestDtoMapperImpl;
import com.polaris.shared.error.DuplicateResourceException;
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
@WebMvcTest(controllers = RutinaController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({RutinaRequestDtoMapperImpl.class, RutinaFilterMapperImpl.class, RutinaFormDtoMapperImpl.class,
        RutinaEjercicioFormDtoMapperImpl.class, RutinaListDtoMapperImpl.class})
class RutinaControllerTest {

    private static final Long USUARIO = 7L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateRutinaInterface createRutina;

    @MockitoBean
    private GetRutinaInterface getRutina;

    @MockitoBean
    private ListRutinaInterface listRutina;

    @MockitoBean
    private UpdateRutinaInterface updateRutina;

    @MockitoBean
    private DeleteRutinaInterface deleteRutina;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @BeforeEach
    void usuarioAutenticado() {
        when(usuarioActual.id()).thenReturn(USUARIO);
    }

    private static Rutina rutina() {
        Ejercicio press = Ejercicio.builder().id(2L).nombre("Press banca").grupoMuscular("Pecho").build();
        Ejercicio dominadas = Ejercicio.builder().id(3L).usuarioId(USUARIO).nombre("Dominadas")
                .grupoMuscular("Espalda").build();
        return Rutina.builder().id(10L).usuarioId(USUARIO).nombre("Push").descripcion("Empuje").activa(true)
                .lineas(List.of(
                        RutinaEjercicio.builder().id(20L).usuarioId(USUARIO).ejercicioId(2L).ejercicio(press)
                                .orden(1).seriesObjetivo(4).repsObjetivo("8-12").build(),
                        RutinaEjercicio.builder().id(21L).usuarioId(USUARIO).ejercicioId(3L).ejercicio(dominadas)
                                .orden(2).seriesObjetivo(3).repsObjetivo("AMRAP").build()))
                .build();
    }

    private static final String CUERPO_VALIDO = "{\"nombre\":\"Push\",\"descripcion\":\"Empuje\",\"activa\":true,"
            + "\"lineas\":[{\"ejercicioId\":2,\"orden\":1,\"seriesObjetivo\":4,\"repsObjetivo\":\"8-12\"},"
            + "{\"ejercicioId\":3,\"orden\":2,\"seriesObjetivo\":3,\"repsObjetivo\":\"AMRAP\"}]}";

    private static String cuerpoConLinea(String linea) {
        return "{\"nombre\":\"Push\",\"activa\":true,\"lineas\":[" + linea + "]}";
    }

    private static final String LINEA_VALIDA =
            "{\"ejercicioId\":2,\"orden\":1,\"seriesObjetivo\":4,\"repsObjetivo\":\"8-12\"}";

    @Test
    @DisplayName("GET lista: 200, version ligera con numeroEjercicios y sin lineas ni usuarioId, y el filtro llega al caso de uso")
    void listaConFiltro() throws Exception {
        when(listRutina.list(eq(USUARIO), any(RutinaFilter.class))).thenReturn(List.of(rutina()));

        mockMvc.perform(get("/api/atlas/rutina").param("activa", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].nombre").value("Push"))
                .andExpect(jsonPath("$[0].activa").value(true))
                .andExpect(jsonPath("$[0].numeroEjercicios").value(2))
                .andExpect(jsonPath("$[0].lineas").doesNotExist())
                .andExpect(jsonPath("$[0].usuarioId").doesNotExist());

        ArgumentCaptor<RutinaFilter> filtro = ArgumentCaptor.forClass(RutinaFilter.class);
        verify(listRutina).list(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getActiva()).isTrue();
    }

    @Test
    @DisplayName("GET lista: sin ?activa= el filtro llega vacio")
    void listaSinFiltro() throws Exception {
        when(listRutina.list(eq(USUARIO), any(RutinaFilter.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/atlas/rutina"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        ArgumentCaptor<RutinaFilter> filtro = ArgumentCaptor.forClass(RutinaFilter.class);
        verify(listRutina).list(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getActiva()).isNull();
    }

    @Test
    @DisplayName("GET detalle: 200 con la ficha y sus lineas anidadas, sin usuarioId, siempre para el usuario actual")
    void detalle() throws Exception {
        when(getRutina.get(USUARIO, 10L)).thenReturn(rutina());

        mockMvc.perform(get("/api/atlas/rutina/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nombre").value("Push"))
                .andExpect(jsonPath("$.descripcion").value("Empuje"))
                .andExpect(jsonPath("$.activa").value(true))
                .andExpect(jsonPath("$.usuarioId").doesNotExist())
                .andExpect(jsonPath("$.lineas.length()").value(2))
                .andExpect(jsonPath("$.lineas[0].id").value(20))
                .andExpect(jsonPath("$.lineas[0].ejercicioId").value(2))
                .andExpect(jsonPath("$.lineas[0].ejercicioNombre").value("Press banca"))
                .andExpect(jsonPath("$.lineas[0].ejercicioGrupoMuscular").value("Pecho"))
                .andExpect(jsonPath("$.lineas[0].orden").value(1))
                .andExpect(jsonPath("$.lineas[0].seriesObjetivo").value(4))
                .andExpect(jsonPath("$.lineas[0].repsObjetivo").value("8-12"))
                .andExpect(jsonPath("$.lineas[0].usuarioId").doesNotExist())
                .andExpect(jsonPath("$.lineas[1].repsObjetivo").value("AMRAP"));
    }

    @Test
    @DisplayName("GET detalle: 404 si no existe o es de otro usuario")
    void detalleNoEncontrado() throws Exception {
        when(getRutina.get(USUARIO, 99L)).thenThrow(new RutinaNotFoundException(99L));

        mockMvc.perform(get("/api/atlas/rutina/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST: 201 con la ficha, el usuario es el actual y un usuarioId o id del body se ignoran")
    void crea() throws Exception {
        when(createRutina.create(eq(USUARIO), any(Rutina.class))).thenReturn(rutina());

        mockMvc.perform(post("/api/atlas/rutina").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":99,\"usuarioId\":999,\"nombre\":\"Push\",\"activa\":true,"
                                + "\"lineas\":[{\"id\":5,\"usuarioId\":999,\"ejercicioId\":2,\"orden\":1,"
                                + "\"seriesObjetivo\":4,\"repsObjetivo\":\"8-12\"}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.lineas.length()").value(2));

        ArgumentCaptor<Rutina> enviada = ArgumentCaptor.forClass(Rutina.class);
        verify(createRutina).create(eq(USUARIO), enviada.capture());
        assertThat(enviada.getValue().getId()).isNull();
        assertThat(enviada.getValue().getUsuarioId()).isNull();
        assertThat(enviada.getValue().getLineas()).hasSize(1);
        assertThat(enviada.getValue().getLineas().get(0).getId()).isNull();
        assertThat(enviada.getValue().getLineas().get(0).getUsuarioId()).isNull();
        assertThat(enviada.getValue().getLineas().get(0).getEjercicioId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("POST: la descripcion es opcional")
    void creaSinDescripcion() throws Exception {
        when(createRutina.create(eq(USUARIO), any(Rutina.class))).thenReturn(rutina());

        mockMvc.perform(post("/api/atlas/rutina").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoConLinea(LINEA_VALIDA)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST: 409 si el nombre ya existe")
    void creaDuplicada() throws Exception {
        when(createRutina.create(eq(USUARIO), any(Rutina.class)))
                .thenThrow(new DuplicateResourceException("Ya tienes una rutina con ese nombre"));

        mockMvc.perform(post("/api/atlas/rutina").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST: 400 con el mensaje legible si el servicio rechaza un ejercicio ajeno u orden repetido")
    void creaRechazadaPorElServicio() throws Exception {
        when(createRutina.create(eq(USUARIO), any(Rutina.class)))
                .thenThrow(new ValidationException("El ejercicio 2 no existe o no esta disponible para ti"));

        mockMvc.perform(post("/api/atlas/rutina").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El ejercicio 2 no existe o no esta disponible para ti"));
    }

    @Test
    @DisplayName("POST: 400 sin nombre, en blanco, demasiado largo, sin activa, sin lineas o con mas de 50, y no llega al caso de uso")
    void creaInvalida() throws Exception {
        String muchas = String.join(",", java.util.Collections.nCopies(51, LINEA_VALIDA));
        String[] cuerpos = {
                "{\"activa\":true,\"lineas\":[" + LINEA_VALIDA + "]}",
                "{\"nombre\":\" \",\"activa\":true,\"lineas\":[" + LINEA_VALIDA + "]}",
                "{\"nombre\":\"" + "x".repeat(101) + "\",\"activa\":true,\"lineas\":[" + LINEA_VALIDA + "]}",
                "{\"nombre\":\"Push\",\"descripcion\":\"" + "d".repeat(2001) + "\",\"activa\":true,\"lineas\":["
                        + LINEA_VALIDA + "]}",
                "{\"nombre\":\"Push\",\"lineas\":[" + LINEA_VALIDA + "]}",
                "{\"nombre\":\"Push\",\"activa\":true}",
                "{\"nombre\":\"Push\",\"activa\":true,\"lineas\":[]}",
                "{\"nombre\":\"Push\",\"activa\":true,\"lineas\":[" + muchas + "]}",
                "{\"nombre\":\"Push\",\"activa\":true,\"lineas\":[null]}"
        };

        for (String cuerpo : cuerpos) {
            mockMvc.perform(post("/api/atlas/rutina").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest());
        }

        verify(createRutina, never()).create(any(), any());
    }

    @Test
    @DisplayName("POST: 400 con una linea sin ejercicio, sin orden, con orden 0, con series fuera de 1-20 o con reps vacias o largas")
    void creaLineaInvalida() throws Exception {
        String[] lineas = {
                "{\"orden\":1,\"seriesObjetivo\":4,\"repsObjetivo\":\"8-12\"}",
                "{\"ejercicioId\":2,\"seriesObjetivo\":4,\"repsObjetivo\":\"8-12\"}",
                "{\"ejercicioId\":2,\"orden\":0,\"seriesObjetivo\":4,\"repsObjetivo\":\"8-12\"}",
                "{\"ejercicioId\":2,\"orden\":1,\"repsObjetivo\":\"8-12\"}",
                "{\"ejercicioId\":2,\"orden\":1,\"seriesObjetivo\":0,\"repsObjetivo\":\"8-12\"}",
                "{\"ejercicioId\":2,\"orden\":1,\"seriesObjetivo\":21,\"repsObjetivo\":\"8-12\"}",
                "{\"ejercicioId\":2,\"orden\":1,\"seriesObjetivo\":4}",
                "{\"ejercicioId\":2,\"orden\":1,\"seriesObjetivo\":4,\"repsObjetivo\":\" \"}",
                "{\"ejercicioId\":2,\"orden\":1,\"seriesObjetivo\":4,\"repsObjetivo\":\"" + "r".repeat(21) + "\"}"
        };

        for (String linea : lineas) {
            mockMvc.perform(post("/api/atlas/rutina").contentType(MediaType.APPLICATION_JSON)
                            .content(cuerpoConLinea(linea)))
                    .andExpect(status().isBadRequest());
        }

        verify(createRutina, never()).create(any(), any());
    }

    @Test
    @DisplayName("PUT: 200 con la ficha actualizada, para el usuario actual")
    void actualiza() throws Exception {
        when(updateRutina.update(eq(USUARIO), eq(10L), any(Rutina.class))).thenReturn(rutina());

        mockMvc.perform(put("/api/atlas/rutina/10").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Push"))
                .andExpect(jsonPath("$.lineas.length()").value(2));

        ArgumentCaptor<Rutina> enviada = ArgumentCaptor.forClass(Rutina.class);
        verify(updateRutina).update(eq(USUARIO), eq(10L), enviada.capture());
        assertThat(enviada.getValue().getLineas()).hasSize(2);
    }

    @Test
    @DisplayName("PUT: 404 si es de otro usuario, 409 si el nombre esta ocupado, 400 si el servicio la rechaza")
    void actualizaRechazado() throws Exception {
        when(updateRutina.update(eq(USUARIO), eq(12L), any(Rutina.class)))
                .thenThrow(new RutinaNotFoundException(12L));
        when(updateRutina.update(eq(USUARIO), eq(13L), any(Rutina.class)))
                .thenThrow(new DuplicateResourceException("Ya tienes una rutina con ese nombre"));
        when(updateRutina.update(eq(USUARIO), eq(14L), any(Rutina.class)))
                .thenThrow(new ValidationException("El orden 1 esta repetido en la rutina"));

        mockMvc.perform(put("/api/atlas/rutina/12").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/atlas/rutina/13").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/api/atlas/rutina/14").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT: 400 con el cuerpo invalido, y no llega al caso de uso")
    void actualizaInvalido() throws Exception {
        mockMvc.perform(put("/api/atlas/rutina/10").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"activa\":true,\"lineas\":[" + LINEA_VALIDA + "]}"))
                .andExpect(status().isBadRequest());

        verify(updateRutina, never()).update(any(), any(), any());
    }

    @Test
    @DisplayName("DELETE: 204 y siempre para el usuario actual")
    void borra() throws Exception {
        mockMvc.perform(delete("/api/atlas/rutina/10"))
                .andExpect(status().isNoContent());

        verify(deleteRutina).delete(USUARIO, 10L);
    }

    @Test
    @DisplayName("DELETE: 404 si no existe o es de otro usuario")
    void borraNoEncontrada() throws Exception {
        doThrow(new RutinaNotFoundException(12L)).when(deleteRutina).delete(USUARIO, 12L);

        mockMvc.perform(delete("/api/atlas/rutina/12")).andExpect(status().isNotFound());
    }
}
