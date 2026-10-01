package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.GetProgresionInterface;
import com.polaris.atlas.application.in.ListRecordsInterface;
import com.polaris.atlas.domain.model.EjercicioNotFoundException;
import com.polaris.atlas.domain.model.ProgresionFilter;
import com.polaris.atlas.domain.model.ProgresionSesion;
import com.polaris.atlas.domain.model.RecordEjercicio;
import com.polaris.auth.infrastructure.security.JwtService;
import com.polaris.shared.testing.IntegracionBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Las agregaciones de progresion y records (ADR 026) contra MySQL 8.4 real: la
 * tabla derivada del HQL (que pide MySQL >= 8.0.19), el SUM de reps por peso en
 * DECIMAL, el desempate y el aislamiento entre usuarios. Los unitarios solo
 * comprueban el dominio con las filas ya hechas; aqui se ejecutan las consultas.
 *
 * <p>Datos insertados por SQL y comprobados a mano. Todo lo que crea el test
 * lleva el prefijo "IT " en el nombre del ejercicio o un usuario propio (9101 y
 * 9102), y se borra antes de cada test.
 */
class ProgresionRecordsIntegracionTest extends IntegracionBase {

    private static final Long A = 9101L;
    private static final Long B = 9102L;

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private GetProgresionInterface progresion;
    @Autowired
    private ListRecordsInterface records;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private MockMvc mockMvc;

    private Long pressBanca;
    private Long sentadilla;
    private Long dominadas;
    private Long sinSeries;
    private Long propioDeB;

    /** Tambien despues: las series cuelgan de ejercicios del catalogo que otras clases limpian. */
    @AfterEach
    void limpiar() {
        jdbc.update("delete from serie_registro where usuario_id in (?, ?)", A, B);
        jdbc.update("delete from sesion where usuario_id in (?, ?)", A, B);
        jdbc.update("delete from ejercicio where nombre like 'IT %' or usuario_id in (?, ?)", A, B);
    }

    @BeforeEach
    void datos() {
        limpiar();

        // Catalogo compartido (usuario_id NULL).
        pressBanca = ejercicio(null, "IT Press banca", "Pecho");
        sentadilla = ejercicio(null, "IT Sentadilla", "Pierna");
        dominadas = ejercicio(null, "IT Dominadas", "Espalda");
        sinSeries = ejercicio(null, "IT Sin series", "Core");
        propioDeB = ejercicio(B, "IT Propio de B", "Core");

        // Usuario A. Los ids de sesion crecen en el orden de insercion: s4 se
        // inserta DESPUES de s3 pero cae en la fecha de s2.
        Long s1 = sesion(A, "2026-03-02");
        serie(A, s1, pressBanca, 1, 10, "60.00");
        serie(A, s1, pressBanca, 2, 8, "62.50");
        serie(A, s1, sentadilla, 1, 5, "100.00");

        Long s2 = sesion(A, "2026-03-09");
        serie(A, s2, pressBanca, 1, 5, "80.00");
        serie(A, s2, pressBanca, 2, 5, "80.00");
        serie(A, s2, dominadas, 1, 12, "0.00");
        serie(A, s2, dominadas, 2, 10, "0.00");

        Long s3 = sesion(A, "2026-03-16");
        serie(A, s3, pressBanca, 1, 3, "80.00");
        serie(A, s3, pressBanca, 2, 6, "80.00");

        Long s4 = sesion(A, "2026-03-09");
        serie(A, s4, pressBanca, 1, 10, "60.00");

        Long s5 = sesion(A, "2026-04-06");
        serie(A, s5, pressBanca, 1, 10, "60.00");
        serie(A, s5, pressBanca, 2, 8, "62.50");

        Long s6 = sesion(A, "2026-04-13");
        serie(A, s6, pressBanca, 1, 6, "80.00");
        serie(A, s6, dominadas, 1, 12, "0.00");

        // Usuario B, mismo ejercicio del catalogo y una marca enorme: no debe
        // aparecer nunca en lo de A.
        Long sb = sesion(B, "2026-03-02");
        serie(B, sb, pressBanca, 1, 1, "200.00");
        serie(B, sb, propioDeB, 1, 20, "15.00");
    }

    @Test
    @DisplayName("Progresion: una fila por sesion con volumen, series, peso maximo y reps comprobados a mano, por fecha y luego por id de sesion")
    void progresionPorSesion() {
        List<ProgresionSesion> puntos = progresion.get(A, ProgresionFilter.builder().ejercicioId(pressBanca).build());

        assertThat(puntos).extracting(p -> p.getFecha().toString())
                .containsExactly("2026-03-02", "2026-03-09", "2026-03-09", "2026-03-16", "2026-04-06", "2026-04-13");
        // Los dos del 9 de marzo, por id de sesion: s2 (la primera) y luego s4.
        assertThat(puntos.get(1).getSesionId()).isLessThan(puntos.get(2).getSesionId());

        // 10x60 + 8x62.50 = 600 + 500
        assertPunto(puntos.get(0), "1100.00", 2, "62.50", 18);
        // 5x80 + 5x80
        assertPunto(puntos.get(1), "800.00", 2, "80.00", 10);
        // 10x60
        assertPunto(puntos.get(2), "600.00", 1, "60.00", 10);
        // 3x80 + 6x80 = 240 + 480
        assertPunto(puntos.get(3), "720.00", 2, "80.00", 9);
        assertPunto(puntos.get(4), "1100.00", 2, "62.50", 18);
        // 6x80
        assertPunto(puntos.get(5), "480.00", 1, "80.00", 6);
    }

    @Test
    @DisplayName("Progresion: el rango desde/hasta es inclusivo por los dos extremos, y fuera de rango da lista vacia")
    void progresionConRango() {
        List<ProgresionSesion> puntos = progresion.get(A, ProgresionFilter.builder().ejercicioId(pressBanca)
                .desde(LocalDate.of(2026, 3, 9)).hasta(LocalDate.of(2026, 3, 16)).build());
        List<ProgresionSesion> soloDesde = progresion.get(A, ProgresionFilter.builder().ejercicioId(pressBanca)
                .desde(LocalDate.of(2026, 4, 6)).build());
        List<ProgresionSesion> soloHasta = progresion.get(A, ProgresionFilter.builder().ejercicioId(pressBanca)
                .hasta(LocalDate.of(2026, 3, 2)).build());
        List<ProgresionSesion> fuera = progresion.get(A, ProgresionFilter.builder().ejercicioId(pressBanca)
                .desde(LocalDate.of(2027, 1, 1)).build());

        assertThat(puntos).extracting(p -> p.getVolumen().toPlainString())
                .containsExactly("800.00", "600.00", "720.00");
        assertThat(soloDesde).extracting(p -> p.getVolumen().toPlainString()).containsExactly("1100.00", "480.00");
        assertThat(soloHasta).extracting(p -> p.getVolumen().toPlainString()).containsExactly("1100.00");
        assertThat(fuera).isEmpty();
    }

    @Test
    @DisplayName("Progresion: un ejercicio solo con peso corporal da volumen 0.00 en cada sesion, pero cuenta series y reps")
    void progresionConPesoCero() {
        List<ProgresionSesion> puntos = progresion.get(A, ProgresionFilter.builder().ejercicioId(dominadas).build());

        assertThat(puntos).hasSize(2);
        assertPunto(puntos.get(0), "0.00", 2, "0.00", 22);
        assertPunto(puntos.get(1), "0.00", 1, "0.00", 12);
    }

    @Test
    @DisplayName("Progresion: aislamiento entre usuarios. B solo ve lo suyo del mismo ejercicio del catalogo, y un ejercicio sin series da vacio")
    void progresionAislaUsuarios() {
        List<ProgresionSesion> deB = progresion.get(B, ProgresionFilter.builder().ejercicioId(pressBanca).build());

        assertThat(deB).hasSize(1);
        assertPunto(deB.get(0), "200.00", 1, "200.00", 1);
        assertThat(progresion.get(A, ProgresionFilter.builder().ejercicioId(sinSeries).build())).isEmpty();
        assertThat(progresion.get(9999L, ProgresionFilter.builder().ejercicioId(pressBanca).build())).isEmpty();
    }

    @Test
    @DisplayName("Progresion: el ejercicio propio de otro usuario es 404, igual que uno inexistente")
    void progresionDeEjercicioAjeno() {
        assertThatThrownBy(() -> progresion.get(A, ProgresionFilter.builder().ejercicioId(propioDeB).build()))
                .isInstanceOf(EjercicioNotFoundException.class);
        // El dueno si lo ve.
        assertThat(progresion.get(B, ProgresionFilter.builder().ejercicioId(propioDeB).build())).hasSize(1);
    }

    @Test
    @DisplayName("Records: peso maximo con desempate por mas reps y primera fecha, y volumen maximo en la primera sesion que lo logro")
    void records() {
        List<RecordEjercicio> marcas = records.list(A);

        // Orden por nombre de ejercicio. El de B no aparece: A no tiene series en el suyo.
        assertThat(marcas).extracting(RecordEjercicio::getEjercicioNombre)
                .containsExactly("IT Dominadas", "IT Press banca", "IT Sentadilla");

        RecordEjercicio press = marcas.get(1);
        assertThat(press.getEjercicioId()).isEqualTo(pressBanca);
        assertThat(press.getEjercicioGrupoMuscular()).isEqualTo("Pecho");
        // 80 kg lo hizo con 5 reps (el 9/3), con 3 y con 6 (el 16/3 y otra vez el 13/4): gana el de mas reps,
        // y de esos la primera fecha. El 200 de B no cuenta.
        assertThat(press.getPesoMaximo().toPlainString()).isEqualTo("80.00");
        assertThat(press.getRepsPesoMaximo()).isEqualTo(6);
        assertThat(press.getFechaPesoMaximo()).isEqualTo(LocalDate.of(2026, 3, 16));
        // 1100 el 2/3 y otra vez el 6/4: gana la primera.
        assertThat(press.getVolumenMaximoSesion().toPlainString()).isEqualTo("1100.00");
        assertThat(press.getFechaVolumenMaximo()).isEqualTo(LocalDate.of(2026, 3, 2));

        RecordEjercicio sentadillaRecord = marcas.get(2);
        assertThat(sentadillaRecord.getPesoMaximo().toPlainString()).isEqualTo("100.00");
        assertThat(sentadillaRecord.getRepsPesoMaximo()).isEqualTo(5);
        assertThat(sentadillaRecord.getFechaPesoMaximo()).isEqualTo(LocalDate.of(2026, 3, 2));
        assertThat(sentadillaRecord.getVolumenMaximoSesion().toPlainString()).isEqualTo("500.00");
        assertThat(sentadillaRecord.getFechaVolumenMaximo()).isEqualTo(LocalDate.of(2026, 3, 2));
    }

    @Test
    @DisplayName("Records: el peso corporal tiene su marca de peso (0.00, mas reps, primera fecha) y ninguna de volumen")
    void recordsConPesoCero() {
        RecordEjercicio dom = records.list(A).get(0);

        assertThat(dom.getEjercicioNombre()).isEqualTo("IT Dominadas");
        assertThat(dom.getPesoMaximo().toPlainString()).isEqualTo("0.00");
        // 12 reps el 9/3 y otra vez el 13/4.
        assertThat(dom.getRepsPesoMaximo()).isEqualTo(12);
        assertThat(dom.getFechaPesoMaximo()).isEqualTo(LocalDate.of(2026, 3, 9));
        assertThat(dom.getVolumenMaximoSesion()).isNull();
        assertThat(dom.getFechaVolumenMaximo()).isNull();
    }

    @Test
    @DisplayName("Records: aislamiento entre usuarios y sin series da lista vacia")
    void recordsAislaUsuarios() {
        List<RecordEjercicio> deB = records.list(B);

        assertThat(deB).extracting(RecordEjercicio::getEjercicioNombre).containsExactly("IT Press banca", "IT Propio de B");
        assertThat(deB.get(0).getPesoMaximo().toPlainString()).isEqualTo("200.00");
        assertThat(deB.get(0).getVolumenMaximoSesion().toPlainString()).isEqualTo("200.00");
        assertThat(deB.get(1).getPesoMaximo().toPlainString()).isEqualTo("15.00");
        assertThat(deB.get(1).getVolumenMaximoSesion().toPlainString()).isEqualTo("300.00");
        assertThat(records.list(9999L)).isEmpty();
    }

    @Test
    @DisplayName("HTTP con el contexto completo: JWT de A, JSON con dos decimales y los campos de volumen omitidos con peso corporal")
    void recordsPorHttp() throws Exception {
        mockMvc.perform(get("/api/atlas/records").header("Authorization", bearer(A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].ejercicioNombre").value("IT Dominadas"))
                .andExpect(jsonPath("$[0].volumenMaximoSesion").doesNotExist())
                .andExpect(jsonPath("$[0].fechaVolumenMaximo").doesNotExist())
                .andExpect(jsonPath("$[1].pesoMaximo").value(80.0))
                .andExpect(jsonPath("$[1].repsPesoMaximo").value(6))
                .andExpect(jsonPath("$[1].fechaPesoMaximo").value("2026-03-16"))
                .andExpect(jsonPath("$[1].volumenMaximoSesion").value(1100.0))
                .andExpect(jsonPath("$[1].fechaVolumenMaximo").value("2026-03-02"));
    }

    @Test
    @DisplayName("HTTP: la progresion respeta el JWT (B solo ve lo suyo), el rango invertido es 400 y el ejercicio ajeno 404")
    void progresionPorHttp() throws Exception {
        mockMvc.perform(get("/api/atlas/progresion").param("ejercicioId", pressBanca.toString())
                        .header("Authorization", bearer(B)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].volumen").value(200.0))
                .andExpect(jsonPath("$[0].fecha").value("2026-03-02"));

        mockMvc.perform(get("/api/atlas/progresion").param("ejercicioId", pressBanca.toString())
                        .param("desde", "2026-04-01").param("hasta", "2026-03-01")
                        .header("Authorization", bearer(A)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/atlas/progresion").param("ejercicioId", propioDeB.toString())
                        .header("Authorization", bearer(A)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/atlas/records")).andExpect(status().isUnauthorized());
    }

    private String bearer(Long usuarioId) {
        return "Bearer " + jwtService.generar(usuarioId);
    }

    private static void assertPunto(ProgresionSesion punto, String volumen, int series, String pesoMaximo, long reps) {
        assertThat(punto.getVolumen().toPlainString()).isEqualTo(volumen);
        assertThat(punto.getNumeroSeries()).isEqualTo(series);
        assertThat(punto.getPesoMaximo().toPlainString()).isEqualTo(pesoMaximo);
        assertThat(punto.getRepsTotales()).isEqualTo(reps);
    }

    private Long ejercicio(Long usuarioId, String nombre, String grupo) {
        Map<String, Object> fila = new java.util.HashMap<>();
        fila.put("usuario_id", usuarioId);
        fila.put("nombre", nombre);
        fila.put("grupo_muscular", grupo);
        return insertar("ejercicio", fila);
    }

    private Long sesion(Long usuarioId, String fecha) {
        return insertar("sesion", Map.of("usuario_id", usuarioId, "fecha", LocalDate.parse(fecha)));
    }

    private Long serie(Long usuarioId, Long sesionId, Long ejercicioId, int numero, int reps, String pesoKg) {
        return insertar("serie_registro", Map.of("usuario_id", usuarioId, "sesion_id", sesionId,
                "ejercicio_id", ejercicioId, "numero_serie", numero, "reps", reps, "peso_kg", new BigDecimal(pesoKg)));
    }

    private Long insertar(String tabla, Map<String, Object> columnas) {
        return new SimpleJdbcInsert(jdbc).withTableName(tabla).usingGeneratedKeyColumns("id")
                .executeAndReturnKey(columnas).longValue();
    }
}
