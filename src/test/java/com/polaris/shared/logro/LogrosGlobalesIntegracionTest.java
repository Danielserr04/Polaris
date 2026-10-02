package com.polaris.shared.logro;

import com.polaris.auth.infrastructure.security.JwtService;
import com.polaris.shared.testing.IntegracionBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Las consultas de los logros de Nucleo, Fusion, Kuiper y Odisea contra MySQL
 * real: filtran por usuario, dejan fuera la papelera y la fecha sale de los
 * datos. Los de Atlas estan en ProgresionRecordsIntegracionTest. Ver
 * docs/decisiones/044-logros-globales-con-fecha-calculada.md.
 *
 * <p>Usuarios propios (9301 y 9302), borrados antes y despues de cada test.
 */
class LogrosGlobalesIntegracionTest extends IntegracionBase {

    private static final Long A = 9301L;
    private static final Long B = 9302L;

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void limpiar() {
        for (String tabla : new String[]{"registro_peso", "comida", "movimiento", "categoria", "entrada"}) {
            jdbc.update("delete from " + tabla + " where usuario_id in (?, ?)", A, B);
        }
        jdbc.update("delete from titulo where titulo like 'IT logros %'");
    }

    @BeforeEach
    void datos() {
        limpiar();
        // Nucleo: dos pesajes de A y uno de B.
        peso(A, "2026-03-04");
        peso(A, "2026-03-01");
        peso(B, "2026-01-01");
        // Fusion: dos comidas de A el mismo dia.
        jdbc.update("insert into comida (usuario_id, fecha, momento) values (?, '2026-02-10', 'COMIDA'), "
                + "(?, '2026-02-10', 'CENA')", A, A);
        // Kuiper: en agosto entra 500 y sale 200; un gasto de 1000 en la papelera no cuenta.
        jdbc.update("insert into categoria (usuario_id, nombre, tipo) values (?, 'IT', 'GASTO')", A);
        Long cat = jdbc.queryForObject("select id from categoria where usuario_id = ?", Long.class, A);
        movimiento(cat, "2026-08-02", "500.00", "INGRESO", null);
        movimiento(cat, "2026-08-20", "200.00", "GASTO", null);
        movimiento(cat, "2026-08-21", "1000.00", "GASTO", "2026-09-01 10:00:00");
        // Odisea: una pelicula terminada el 5 de mayo, con nota.
        jdbc.update("insert into titulo (tipo, titulo, fuente_externa) values ('PELICULA', 'IT logros peli', 'MANUAL')");
        Long titulo = jdbc.queryForObject("select id from titulo where titulo = 'IT logros peli'", Long.class);
        jdbc.update("insert into entrada (usuario_id, titulo_id, estado, valoracion, fecha_fin, favorito) "
                + "values (?, ?, 'TERMINADO', 8, '2026-05-05', b'0')", A, titulo);
    }

    @Test
    @DisplayName("Nucleo: los pesajes son de A y el primero se consiguio el dia del pesaje mas antiguo")
    void nucleo() throws Exception {
        mockMvc.perform(get("/api/nucleo/logros").header("Authorization", bearer(A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == 'PRIMER_PESAJE')].fechaConseguido").value("2026-03-01"))
                .andExpect(jsonPath("$[?(@.codigo == 'PESAJES_30')].progreso").value(2));
    }

    @Test
    @DisplayName("Fusion: dos comidas cuentan dos, pero un solo dia")
    void fusion() throws Exception {
        mockMvc.perform(get("/api/fusion/logros").header("Authorization", bearer(A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == 'PRIMERA_COMIDA')].fechaConseguido").value("2026-02-10"))
                .andExpect(jsonPath("$[?(@.codigo == 'COMIDAS_100')].progreso").value(2))
                .andExpect(jsonPath("$[?(@.codigo == 'DIAS_30')].progreso").value(1));
    }

    @Test
    @DisplayName("Kuiper: agosto cierra en verde sin contar la papelera, fechado el 31")
    void kuiper() throws Exception {
        mockMvc.perform(get("/api/kuiper/logros").header("Authorization", bearer(A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == 'MOVIMIENTOS_100')].progreso").value(2))
                .andExpect(jsonPath("$[?(@.codigo == 'MES_EN_VERDE')].fechaConseguido").value("2026-08-31"));
    }

    @Test
    @DisplayName("Odisea: la pelicula terminada y valorada cuenta con su fecha de fin; B no ve nada de A")
    void odisea() throws Exception {
        mockMvc.perform(get("/api/odisea/logros").header("Authorization", bearer(A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == 'PRIMER_TERMINADO')].fechaConseguido").value("2026-05-05"))
                .andExpect(jsonPath("$[?(@.codigo == 'PELICULAS_10')].progreso").value(1))
                .andExpect(jsonPath("$[?(@.codigo == 'VALORADAS_10')].progreso").value(1));
        mockMvc.perform(get("/api/odisea/logros").header("Authorization", bearer(B)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == 'PRIMER_TERMINADO')].progreso").value(0));
        mockMvc.perform(get("/api/odisea/logros")).andExpect(status().isUnauthorized());
    }

    private void peso(Long usuario, String fecha) {
        jdbc.update("insert into registro_peso (usuario_id, fecha, peso_kg) values (?, ?, 80.00)", usuario, fecha);
    }

    private void movimiento(Long categoria, String fecha, String importe, String tipo, String borradoEn) {
        jdbc.update("insert into movimiento (usuario_id, fecha, importe, tipo, categoria_id, recurrente, borrado_en) "
                + "values (?, ?, ?, ?, ?, b'0', ?)", A, fecha, importe, tipo, categoria, borradoEn);
    }

    private String bearer(Long usuarioId) {
        return "Bearer " + jwtService.generar(usuarioId);
    }
}
