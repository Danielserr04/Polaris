package com.polaris.kuiper.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polaris.auth.infrastructure.security.JwtService;
import com.polaris.shared.testing.IntegracionBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La papelera y el duplicado de punta a punta: controller, servicio, adaptador
 * y MySQL. Lo que importa es que un movimiento en la papelera desaparece del
 * listado, del detalle y del resumen, y que vuelve igual al restaurarlo. Ver
 * docs/decisiones/038-movimiento-papelera-y-duplicar.md.
 */
class MovimientoPapeleraIntegracionTest extends IntegracionBase {

    private static final Long USUARIO = 9311L;
    private static final Long OTRO = 9312L;
    private static final String BASE = "/api/kuiper/movimiento";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ObjectMapper json;

    private Long categoria;
    private final String hoy = LocalDate.now().toString();
    private final String periodo = hoy.substring(0, 7);

    @BeforeEach
    void datos() throws Exception {
        jdbc.update("delete from movimiento where usuario_id in (?, ?)", USUARIO, OTRO);
        jdbc.update("delete from categoria where usuario_id in (?, ?)", USUARIO, OTRO);
        categoria = idDe(mockMvc.perform(post("/api/kuiper/categoria").header("Authorization", bearer(USUARIO))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Comida\",\"tipo\":\"GASTO\"}"))
                .andExpect(status().isCreated()));
    }

    private String bearer(Long usuario) {
        return "Bearer " + jwtService.generar(usuario);
    }

    private Long idDe(ResultActions r) throws Exception {
        return json.readTree(r.andReturn().getResponse().getContentAsString()).get("id").asLong();
    }

    private Long crear(String importe) throws Exception {
        return idDe(mockMvc.perform(post(BASE).header("Authorization", bearer(USUARIO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"" + hoy + "\",\"importe\":" + importe + ",\"tipo\":\"GASTO\","
                                + "\"categoriaId\":" + categoria + ",\"concepto\":\"Cafe\",\"recurrente\":true}"))
                .andExpect(status().isCreated()));
    }

    private ResultActions como(Long usuario, MockHttpServletRequestBuilder r)
            throws Exception {
        return mockMvc.perform(r.header("Authorization", bearer(usuario)));
    }

    @Test
    @DisplayName("Borrar manda a la papelera: sale del listado, del detalle y del resumen; restaurar lo devuelve")
    void borrarYRestaurar() throws Exception {
        Long id = crear("10.00");
        crear("5.00");

        como(USUARIO, delete(BASE + "/" + id)).andExpect(status().isNoContent());

        como(USUARIO, get(BASE)).andExpect(jsonPath("$", hasSize(1)));
        como(USUARIO, get(BASE + "/" + id)).andExpect(status().isNotFound());
        como(USUARIO, delete(BASE + "/" + id)).andExpect(status().isNotFound());
        como(USUARIO, get("/api/kuiper/resumen").param("periodo", periodo))
                .andExpect(jsonPath("$.gastos").value(5.00));
        como(USUARIO, get(BASE + "/papelera"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].categoriaNombre").value("Comida"))
                .andExpect(jsonPath("$[0].borradoEn").exists());
        como(OTRO, get(BASE + "/papelera")).andExpect(jsonPath("$", hasSize(0)));
        como(OTRO, post(BASE + "/" + id + "/restaurar")).andExpect(status().isNotFound());

        como(USUARIO, post(BASE + "/" + id + "/restaurar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.recurrente").value(true));

        como(USUARIO, get(BASE)).andExpect(jsonPath("$", hasSize(2)));
        como(USUARIO, get(BASE + "/papelera")).andExpect(jsonPath("$", hasSize(0)));
        como(USUARIO, post(BASE + "/" + id + "/restaurar")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Borrar definitivo solo vale desde la papelera; vaciar borra toda la del usuario")
    void definitivoYVaciar() throws Exception {
        Long a = crear("1.00");
        Long b = crear("2.00");
        Long c = crear("3.00");

        como(USUARIO, delete(BASE + "/" + a + "/definitivo")).andExpect(status().isNotFound());
        como(USUARIO, delete(BASE + "/" + a)).andExpect(status().isNoContent());
        como(OTRO, delete(BASE + "/" + a + "/definitivo")).andExpect(status().isNotFound());
        como(USUARIO, delete(BASE + "/" + a + "/definitivo")).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("select count(*) from movimiento where id = ?", Integer.class, a)).isZero();

        como(USUARIO, delete(BASE + "/" + b)).andExpect(status().isNoContent());
        como(USUARIO, delete(BASE + "/" + c)).andExpect(status().isNoContent());
        como(USUARIO, delete(BASE + "/papelera")).andExpect(status().isNoContent());

        assertThat(jdbc.queryForObject("select count(*) from movimiento where usuario_id = ?", Integer.class, USUARIO))
                .isZero();
    }

    @Test
    @DisplayName("Borrar varios: todos o ninguno; un id ajeno da 404 y no mueve nada")
    void borrarVarios() throws Exception {
        Long a = crear("1.00");
        Long b = crear("2.00");
        Long ajeno = jdbc.queryForObject("select max(id) from movimiento where usuario_id = ?", Long.class, USUARIO);
        jdbc.update("update movimiento set usuario_id = ? where id = ?", OTRO, ajeno);

        como(USUARIO, post(BASE + "/borrar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\":[" + a + "," + b + "]}")).andExpect(status().isNotFound());
        como(USUARIO, get(BASE)).andExpect(jsonPath("$", hasSize(1)));

        jdbc.update("update movimiento set usuario_id = ? where id = ?", USUARIO, ajeno);
        como(USUARIO, post(BASE + "/borrar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\":[]}")).andExpect(status().isBadRequest());
        como(USUARIO, post(BASE + "/borrar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\":[" + a + "," + b + "]}")).andExpect(status().isNoContent());

        como(USUARIO, get(BASE)).andExpect(jsonPath("$", hasSize(0)));
        como(USUARIO, get(BASE + "/papelera")).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Duplicar: 201 con la copia de hoy (o de la fecha pedida), sin marca recurrente; 400 si es futura")
    void duplicar() throws Exception {
        Long id = crear("12.50");

        como(USUARIO, post(BASE + "/" + id + "/duplicar"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fecha").value(hoy))
                .andExpect(jsonPath("$.importe").value(12.50))
                .andExpect(jsonPath("$.concepto").value("Cafe"))
                .andExpect(jsonPath("$.recurrente").value(false));
        como(USUARIO, post(BASE + "/" + id + "/duplicar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2026-01-15\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fecha").value("2026-01-15"));
        como(USUARIO, post(BASE + "/" + id + "/duplicar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"" + LocalDate.now().plusDays(2) + "\"}"))
                .andExpect(status().isBadRequest());
        como(OTRO, post(BASE + "/" + id + "/duplicar")).andExpect(status().isNotFound());

        como(USUARIO, get(BASE)).andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("Una categoria cuyos movimientos estan todos en la papelera se puede borrar, y se los lleva")
    void categoriaConSoloPapelera() throws Exception {
        Long id = crear("7.00");

        como(USUARIO, delete("/api/kuiper/categoria/" + categoria)).andExpect(status().isBadRequest());
        como(USUARIO, delete(BASE + "/" + id)).andExpect(status().isNoContent());
        como(USUARIO, delete("/api/kuiper/categoria/" + categoria)).andExpect(status().isNoContent());

        assertThat(jdbc.queryForObject("select count(*) from movimiento where usuario_id = ?", Integer.class, USUARIO))
                .isZero();
    }
}
