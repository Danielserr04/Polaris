package com.polaris.shared.error;

import com.polaris.auth.infrastructure.security.JwtService;
import com.polaris.kuiper.application.out.CategoriaRepositoryPort;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.nucleo.application.out.RegistroPesoRepositoryPort;
import com.polaris.shared.testing.IntegracionBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ADR 020: la violacion REAL de un UNIQUE de MySQL (error 1062) llega al
 * GlobalExceptionHandler como 409, y una violacion de otra clase (FK, error
 * 1451) sigue siendo 500. Los unitarios del handler fabrican la excepcion a mano;
 * aqui la lanza la base.
 *
 * <p>Una carrera entre dos peticiones no se puede provocar de forma estable, asi
 * que se simula su efecto: el puerto espiado contesta "no hay duplicado" a la
 * comprobacion previa del servicio (que es lo que veria la peticion perdedora
 * en la carrera) y el resto, controller, servicio, adaptador, Hibernate y MySQL,
 * es real. Los espias se reinician solos tras cada test.
 */
class ViolacionUnicidadIntegracionTest extends IntegracionBase {

    private static final Long USUARIO = 9201L;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private JwtService jwtService;

    @MockitoSpyBean
    private CategoriaRepositoryPort categorias;
    @MockitoSpyBean
    private RegistroPesoRepositoryPort registrosPeso;
    @MockitoSpyBean
    private MovimientoRepositoryPort movimientos;
    @MockitoSpyBean
    private PresupuestoRepositoryPort presupuestos;

    @BeforeEach
    void limpiar() {
        jdbc.update("delete from movimiento where usuario_id = ?", USUARIO);
        jdbc.update("delete from categoria where usuario_id = ?", USUARIO);
        jdbc.update("delete from registro_peso where usuario_id = ?", USUARIO);
    }

    private String bearer() {
        return "Bearer " + jwtService.generar(USUARIO);
    }

    private ResultActions postCategoria(String nombre, String tipo) throws Exception {
        return mockMvc.perform(post("/api/kuiper/categoria").header("Authorization", bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + nombre + "\",\"tipo\":\"" + tipo + "\"}"));
    }

    private ResultActions postPeso(String fecha) throws Exception {
        return mockMvc.perform(post("/api/nucleo/registro-peso").header("Authorization", bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fecha\":\"" + fecha + "\",\"pesoKg\":80.5}"));
    }

    private int contar(String tabla) {
        return jdbc.queryForObject("select count(*) from " + tabla + " where usuario_id = ?", Integer.class, USUARIO);
    }

    @Test
    @DisplayName("Categoria duplicada: la comprobacion previa da 409 con su mensaje concreto")
    void categoriaDuplicadaPorComprobacionPrevia() throws Exception {
        postCategoria("Comida", "GASTO").andExpect(status().isCreated());

        postCategoria("Comida", "GASTO")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ya tienes una categoria con ese nombre y tipo"));
    }

    @Test
    @DisplayName("Categoria duplicada que se cuela por la carrera: el UNIQUE de MySQL da 409 generico, no 500")
    void categoriaDuplicadaPorUniqueDeLaBase() throws Exception {
        postCategoria("Comida", "GASTO").andExpect(status().isCreated());
        doReturn(Optional.empty()).when(categorias).findByUsuarioIdAndNombreAndTipo(any(), any(), any());

        // Con la collation utf8mb4_unicode_ci "COMIDA" y "Comida" son la misma clave.
        postCategoria("COMIDA", "GASTO")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("El recurso ya existe"))
                .andExpect(jsonPath("$.error").value(not(containsString("uk_categoria"))))
                .andExpect(jsonPath("$.path").value("/api/kuiper/categoria"));

        assertThat(contar("categoria")).isEqualTo(1);
    }

    @Test
    @DisplayName("El mismo nombre con otro tipo no choca: el unique es (usuario, nombre, tipo)")
    void mismoNombreOtroTipoNoChoca() throws Exception {
        postCategoria("Otros", "GASTO").andExpect(status().isCreated());
        doReturn(Optional.empty()).when(categorias).findByUsuarioIdAndNombreAndTipo(any(), any(), any());

        postCategoria("Otros", "INGRESO").andExpect(status().isCreated());

        assertThat(contar("categoria")).isEqualTo(2);
    }

    @Test
    @DisplayName("RegistroPeso: un POST que se cuela por la carrera para el mismo dia choca con el UNIQUE y da 409")
    void registroPesoDuplicadoPorUniqueDeLaBase() throws Exception {
        postPeso("2026-03-02").andExpect(status().isCreated());
        // Sin la carrera, el servicio actualiza el registro del dia en vez de insertar otro.
        doReturn(Optional.empty()).when(registrosPeso).findByUsuarioIdAndFecha(anyLong(), any());

        postPeso("2026-03-02")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El recurso ya existe"));

        assertThat(contar("registro_peso")).isEqualTo(1);
    }

    @Test
    @DisplayName("RegistroPeso: un PUT que mueve el registro a un dia ocupado y se cuela por la carrera da 409")
    void registroPesoMovidoADiaOcupado() throws Exception {
        postPeso("2026-03-02").andExpect(status().isCreated());
        postPeso("2026-03-03").andExpect(status().isCreated());
        Long idDelTres = jdbc.queryForObject(
                "select id from registro_peso where usuario_id = ? and fecha = '2026-03-03'", Long.class, USUARIO);
        doReturn(Optional.empty()).when(registrosPeso).findByUsuarioIdAndFecha(anyLong(), any());

        mockMvc.perform(put("/api/nucleo/registro-peso/" + idDelTres).header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2026-03-02\",\"pesoKg\":79.0}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El recurso ya existe"));
    }

    @Test
    @DisplayName("Una violacion de FK (error 1451) NO es un 409: sigue siendo 500 y no filtra nombres del esquema")
    void violacionDeClaveForaneaSigueSiendo500() throws Exception {
        postCategoria("Comida", "GASTO").andExpect(status().isCreated());
        Long categoriaId = jdbc.queryForObject("select id from categoria where usuario_id = ?", Long.class, USUARIO);
        jdbc.update("insert into movimiento (usuario_id, fecha, importe, tipo, categoria_id, recurrente) "
                + "values (?, '2026-03-02', 12.34, 'GASTO', ?, 0)", USUARIO, categoriaId);
        // El servicio la protege con un 400; se apaga la comprobacion para que llegue la FK.
        doReturn(false).when(movimientos).existsByCategoriaId(anyLong());
        doReturn(false).when(presupuestos).existsByCategoriaId(anyLong());

        mockMvc.perform(delete("/api/kuiper/categoria/" + categoriaId).header("Authorization", bearer()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno del servidor"));

        assertThat(contar("categoria")).isEqualTo(1);
    }
}
