package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.CreateEjercicioInterface;
import com.polaris.atlas.application.in.DeleteEjercicioInterface;
import com.polaris.atlas.application.in.GetEjercicioInterface;
import com.polaris.atlas.application.in.ListEjercicioInterface;
import com.polaris.atlas.application.in.UpdateEjercicioInterface;
import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.EjercicioCatalogoNoModificableException;
import com.polaris.atlas.domain.model.EjercicioFilter;
import com.polaris.atlas.domain.model.EjercicioNotFoundException;
import com.polaris.atlas.infrastructure.persistence.mapper.EjercicioFilterMapperImpl;
import com.polaris.atlas.infrastructure.persistence.mapper.EjercicioFormDtoMapperImpl;
import com.polaris.atlas.infrastructure.persistence.mapper.EjercicioListDtoMapperImpl;
import com.polaris.atlas.infrastructure.persistence.mapper.EjercicioRequestDtoMapperImpl;
import com.polaris.shared.error.DuplicateResourceException;
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
@WebMvcTest(controllers = EjercicioController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({EjercicioRequestDtoMapperImpl.class, EjercicioFilterMapperImpl.class,
        EjercicioFormDtoMapperImpl.class, EjercicioListDtoMapperImpl.class})
class EjercicioControllerTest {

    private static final Long USUARIO = 7L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateEjercicioInterface createEjercicio;

    @MockitoBean
    private GetEjercicioInterface getEjercicio;

    @MockitoBean
    private ListEjercicioInterface listEjercicio;

    @MockitoBean
    private UpdateEjercicioInterface updateEjercicio;

    @MockitoBean
    private DeleteEjercicioInterface deleteEjercicio;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @BeforeEach
    void usuarioAutenticado() {
        when(usuarioActual.id()).thenReturn(USUARIO);
    }

    private static Ejercicio propio() {
        return Ejercicio.builder().id(10L).usuarioId(USUARIO).nombre("Flexiones").grupoMuscular("Pecho")
                .equipamiento("Peso corporal").build();
    }

    private static Ejercicio catalogo() {
        return Ejercicio.builder().id(11L).nombre("Sentadilla").grupoMuscular("Pierna").build();
    }

    private static final String CUERPO_VALIDO =
            "{\"nombre\":\"Flexiones\",\"grupoMuscular\":\"Pecho\",\"equipamiento\":\"Peso corporal\"}";

    @Test
    @DisplayName("GET lista: 200, catalogo y propios con esPropio, sin usuarioId, y el filtro llega al caso de uso")
    void listaConFiltros() throws Exception {
        when(listEjercicio.list(eq(USUARIO), any(EjercicioFilter.class))).thenReturn(List.of(catalogo(), propio()));

        mockMvc.perform(get("/api/atlas/ejercicio").param("grupoMuscular", "Pecho").param("q", "flex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].esPropio").value(false))
                .andExpect(jsonPath("$[0].equipamiento").doesNotExist())
                .andExpect(jsonPath("$[1].nombre").value("Flexiones"))
                .andExpect(jsonPath("$[1].esPropio").value(true))
                .andExpect(jsonPath("$[1].usuarioId").doesNotExist());

        ArgumentCaptor<EjercicioFilter> filtro = ArgumentCaptor.forClass(EjercicioFilter.class);
        verify(listEjercicio).list(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getGrupoMuscular()).isEqualTo("Pecho");
        assertThat(filtro.getValue().getTexto()).isEqualTo("flex");
    }

    @Test
    @DisplayName("GET detalle: 200 con la ficha, y siempre para el usuario actual")
    void detalle() throws Exception {
        when(getEjercicio.get(USUARIO, 10L)).thenReturn(propio());

        mockMvc.perform(get("/api/atlas/ejercicio/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nombre").value("Flexiones"))
                .andExpect(jsonPath("$.grupoMuscular").value("Pecho"))
                .andExpect(jsonPath("$.equipamiento").value("Peso corporal"))
                .andExpect(jsonPath("$.esPropio").value(true))
                .andExpect(jsonPath("$.usuarioId").doesNotExist());
    }

    @Test
    @DisplayName("GET detalle: 404 si no existe o es de otro usuario")
    void detalleNoEncontrado() throws Exception {
        when(getEjercicio.get(USUARIO, 99L)).thenThrow(new EjercicioNotFoundException(99L));

        mockMvc.perform(get("/api/atlas/ejercicio/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST: 201, el usuario es el actual y un usuarioId o esPropio del body se ignoran")
    void crea() throws Exception {
        when(createEjercicio.create(eq(USUARIO), any(Ejercicio.class))).thenReturn(propio());

        mockMvc.perform(post("/api/atlas/ejercicio").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Flexiones\",\"grupoMuscular\":\"Pecho\",\"equipamiento\":\"Peso corporal\","
                                + "\"usuarioId\":999,\"esPropio\":false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.esPropio").value(true));

        ArgumentCaptor<Ejercicio> enviado = ArgumentCaptor.forClass(Ejercicio.class);
        verify(createEjercicio).create(eq(USUARIO), enviado.capture());
        assertThat(enviado.getValue().getId()).isNull();
        assertThat(enviado.getValue().getUsuarioId()).isNull();
        assertThat(enviado.getValue().getNombre()).isEqualTo("Flexiones");
    }

    @Test
    @DisplayName("POST: 409 si el nombre ya existe")
    void creaDuplicado() throws Exception {
        when(createEjercicio.create(eq(USUARIO), any(Ejercicio.class)))
                .thenThrow(new DuplicateResourceException("Ya existe un ejercicio con ese nombre"));

        mockMvc.perform(post("/api/atlas/ejercicio").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST: 400 sin nombre, con nombre en blanco, sin grupo muscular o con campos mas largos que la columna")
    void creaInvalido() throws Exception {
        String largo = "x".repeat(151);
        String[] cuerpos = {
                "{\"grupoMuscular\":\"Pecho\"}",
                "{\"nombre\":\" \",\"grupoMuscular\":\"Pecho\"}",
                "{\"nombre\":\"Flexiones\"}",
                "{\"nombre\":\"" + largo + "\",\"grupoMuscular\":\"Pecho\"}",
                "{\"nombre\":\"Flexiones\",\"grupoMuscular\":\"" + "y".repeat(51) + "\"}",
                "{\"nombre\":\"Flexiones\",\"grupoMuscular\":\"Pecho\",\"equipamiento\":\"" + "z".repeat(101) + "\"}"
        };

        for (String cuerpo : cuerpos) {
            mockMvc.perform(post("/api/atlas/ejercicio").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest());
        }

        verify(createEjercicio, never()).create(any(), any());
    }

    @Test
    @DisplayName("POST: el equipamiento es opcional")
    void creaSinEquipamiento() throws Exception {
        when(createEjercicio.create(eq(USUARIO), any(Ejercicio.class))).thenReturn(catalogo());

        mockMvc.perform(post("/api/atlas/ejercicio").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Sentadilla\",\"grupoMuscular\":\"Pierna\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT: 200 con la ficha actualizada, para el usuario actual")
    void actualiza() throws Exception {
        when(updateEjercicio.update(eq(USUARIO), eq(10L), any(Ejercicio.class))).thenReturn(propio());

        mockMvc.perform(put("/api/atlas/ejercicio/10").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Flexiones"));
    }

    @Test
    @DisplayName("PUT: 403 si es del catalogo, 404 si es de otro usuario, 409 si el nombre esta ocupado")
    void actualizaRechazado() throws Exception {
        when(updateEjercicio.update(eq(USUARIO), eq(11L), any(Ejercicio.class)))
                .thenThrow(new EjercicioCatalogoNoModificableException(11L));
        when(updateEjercicio.update(eq(USUARIO), eq(12L), any(Ejercicio.class)))
                .thenThrow(new EjercicioNotFoundException(12L));
        when(updateEjercicio.update(eq(USUARIO), eq(13L), any(Ejercicio.class)))
                .thenThrow(new DuplicateResourceException("Ya existe un ejercicio con ese nombre"));

        mockMvc.perform(put("/api/atlas/ejercicio/11").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/atlas/ejercicio/12").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/atlas/ejercicio/13").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT: 400 con el cuerpo invalido, y no llega al caso de uso")
    void actualizaInvalido() throws Exception {
        mockMvc.perform(put("/api/atlas/ejercicio/10").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"grupoMuscular\":\"Pecho\"}"))
                .andExpect(status().isBadRequest());

        verify(updateEjercicio, never()).update(any(), any(), any());
    }

    @Test
    @DisplayName("DELETE: 204 y siempre para el usuario actual")
    void borra() throws Exception {
        mockMvc.perform(delete("/api/atlas/ejercicio/10"))
                .andExpect(status().isNoContent());

        verify(deleteEjercicio).delete(USUARIO, 10L);
    }

    @Test
    @DisplayName("DELETE: 403 si es del catalogo y 404 si es de otro usuario")
    void borraRechazado() throws Exception {
        doThrow(new EjercicioCatalogoNoModificableException(11L)).when(deleteEjercicio).delete(USUARIO, 11L);
        doThrow(new EjercicioNotFoundException(12L)).when(deleteEjercicio).delete(USUARIO, 12L);

        mockMvc.perform(delete("/api/atlas/ejercicio/11")).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/atlas/ejercicio/12")).andExpect(status().isNotFound());
    }
}
