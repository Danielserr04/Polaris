package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.out.RecurrenteRepositoryPort;
import com.polaris.kuiper.domain.model.FrecuenciaRecurrente;
import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.domain.model.RecurrenteFilter;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.shared.testing.IntegracionBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El adaptador de Recurrente contra MySQL: las Specifications (activo, tipo,
 * categoria, aislamiento entre usuarios), el orden por proximaFecha e id,
 * findPendientes (el del job: activos con proxima_fecha <= hoy, de cualquier
 * usuario, por id) y que el DECIMAL(10,2), el BIT(1) y las cuotas nulas
 * vuelven bien.
 *
 * <p>findPendientes no filtra por usuario, asi que aqui se miran solo los ids
 * de este test: la base es compartida con las demas clases de integracion.
 */
class RecurrenteJpaAdapterIntegracionTest extends IntegracionBase {

    private static final Long A = 9401L;
    private static final Long B = 9402L;
    private static final LocalDate HOY = LocalDate.of(2026, 10, 2);

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private RecurrenteRepositoryPort recurrentes;

    private Long ocio;
    private Long nomina;
    private Long netflix;
    private Long gimnasio;
    private Long sueldo;
    private Long movil;
    private Long pausado;
    private Long deB;

    @BeforeEach
    void datos() {
        jdbc.update("delete from recurrente where usuario_id in (?, ?)", A, B);
        jdbc.update("delete from categoria where usuario_id in (?, ?)", A, B);

        ocio = categoria(A, "Ocio", "GASTO");
        nomina = categoria(A, "Nomina", "INGRESO");
        Long ocioDeB = categoria(B, "Ocio", "GASTO");

        netflix = recurrente(A, "Netflix", "12.99", "GASTO", ocio, "MENSUAL", "2026-10-05", null, 0, true);
        gimnasio = recurrente(A, "Gimnasio", "35.00", "GASTO", ocio, "MENSUAL", "2026-10-02", null, 0, true);
        sueldo = recurrente(A, "Sueldo", "99999999.99", "INGRESO", nomina, "MENSUAL", "2026-09-30", null, 0, true);
        movil = recurrente(A, "Movil a plazos", "20.00", "GASTO", ocio, "SEMANAL", "2026-10-09", 24, 10, true);
        pausado = recurrente(A, "Revista", "4.50", "GASTO", ocio, "ANUAL", "2026-01-01", null, 0, false);
        deB = recurrente(B, "Netflix", "12.99", "GASTO", ocioDeB, "MENSUAL", "2026-09-01", null, 0, true);
    }

    private List<Long> ids(Long usuario, RecurrenteFilter filtro) {
        return recurrentes.findAll(usuario, filtro).stream().map(Recurrente::getId).toList();
    }

    @Test
    @DisplayName("Sin filtros: solo los del usuario, del proximo cargo mas cercano al mas lejano")
    void soloLosDelUsuarioEnOrden() {
        assertThat(ids(A, RecurrenteFilter.builder().build()))
                .containsExactly(pausado, sueldo, gimnasio, netflix, movil);
        assertThat(ids(B, null)).containsExactly(deB);
        assertThat(ids(9999L, RecurrenteFilter.builder().build())).isEmpty();
    }

    @Test
    @DisplayName("activo=true son los proximos cargos y activo=false los pausados o terminados")
    void porActivo() {
        assertThat(ids(A, RecurrenteFilter.builder().activo(true).build()))
                .containsExactly(sueldo, gimnasio, netflix, movil);
        assertThat(ids(A, RecurrenteFilter.builder().activo(false).build())).containsExactly(pausado);
    }

    @Test
    @DisplayName("Tipo y categoria filtran y se combinan con activo")
    void tipoCategoriaYCombinados() {
        assertThat(ids(A, RecurrenteFilter.builder().tipo(TipoMovimiento.INGRESO).build())).containsExactly(sueldo);
        assertThat(ids(A, RecurrenteFilter.builder().categoriaId(nomina).build())).containsExactly(sueldo);
        assertThat(ids(A, RecurrenteFilter.builder().tipo(TipoMovimiento.GASTO).categoriaId(ocio).activo(true)
                .build())).containsExactly(gimnasio, netflix, movil);
    }

    @Test
    @DisplayName("Aislamiento: la categoria de otro usuario no devuelve nada aunque se pida su id")
    void categoriaDeOtroUsuario() {
        Long categoriaDeB = jdbc.queryForObject("select id from categoria where usuario_id = ?", Long.class, B);

        assertThat(ids(A, RecurrenteFilter.builder().categoriaId(categoriaDeB).build())).isEmpty();
        assertThat(ids(B, RecurrenteFilter.builder().categoriaId(categoriaDeB).build())).containsExactly(deB);
    }

    @Test
    @DisplayName("findPendientes: activos con cargo hoy o antes, de cualquier usuario, por id; ni pausados ni futuros")
    void pendientes() {
        List<Long> pendientes = recurrentes.findPendientes(HOY).stream().map(Recurrente::getId)
                .filter(id -> List.of(netflix, gimnasio, sueldo, movil, pausado, deB).contains(id)).toList();

        assertThat(pendientes).containsExactly(gimnasio, sueldo, deB);
    }

    @Test
    @DisplayName("Los campos vuelven bien: DECIMAL, BIT, cuotas nulas o con valor y la categoria leida")
    void camposDeVuelta() {
        Recurrente leidoSueldo = recurrentes.findById(sueldo).orElseThrow();
        Recurrente leidoMovil = recurrentes.findById(movil).orElseThrow();

        assertThat(leidoSueldo.getImporte()).isEqualByComparingTo(new BigDecimal("99999999.99"));
        assertThat(leidoSueldo.getCuotasTotal()).isNull();
        assertThat(leidoSueldo.isActivo()).isTrue();
        assertThat(leidoSueldo.getCategoriaId()).isEqualTo(nomina);
        assertThat(leidoSueldo.getCategoria().getNombre()).isEqualTo("Nomina");
        assertThat(leidoMovil.getFrecuencia()).isEqualTo(FrecuenciaRecurrente.SEMANAL);
        assertThat(leidoMovil.getCuotasTotal()).isEqualTo(24);
        assertThat(leidoMovil.getCuotasPagadas()).isEqualTo(10);
        assertThat(recurrentes.findById(pausado).orElseThrow().isActivo()).isFalse();
    }

    @Test
    @DisplayName("save: crea y actualiza, y avanzar proximaFecha lo saca de los pendientes")
    void guardaYAvanza() {
        Recurrente nuevo = recurrentes.save(Recurrente.builder().usuarioId(A).concepto("Spotify")
                .importe(new BigDecimal("10.99")).tipo(TipoMovimiento.GASTO).categoriaId(ocio)
                .frecuencia(FrecuenciaRecurrente.MENSUAL).fechaInicio(HOY).proximaFecha(HOY).activo(true).build());

        assertThat(nuevo.getId()).isNotNull();
        assertThat(recurrentes.findPendientes(HOY)).extracting(Recurrente::getId).contains(nuevo.getId());

        nuevo.setProximaFecha(nuevo.siguienteDespuesDe(HOY));
        nuevo.setCuotasPagadas(1);
        recurrentes.save(nuevo);

        assertThat(recurrentes.findPendientes(HOY)).extracting(Recurrente::getId).doesNotContain(nuevo.getId());
        assertThat(recurrentes.findById(nuevo.getId()).orElseThrow().getProximaFecha())
                .isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(recurrentes.existsByCategoriaId(ocio)).isTrue();
    }

    private Long categoria(Long usuarioId, String nombre, String tipo) {
        return new SimpleJdbcInsert(jdbc).withTableName("categoria").usingGeneratedKeyColumns("id")
                .executeAndReturnKey(Map.of("usuario_id", usuarioId, "nombre", nombre, "tipo", tipo)).longValue();
    }

    private Long recurrente(Long usuarioId, String concepto, String importe, String tipo, Long categoriaId,
                            String frecuencia, String proxima, Integer cuotasTotal, int cuotasPagadas,
                            boolean activo) {
        Map<String, Object> fila = new HashMap<>();
        fila.put("usuario_id", usuarioId);
        fila.put("concepto", concepto);
        fila.put("importe", new BigDecimal(importe));
        fila.put("tipo", tipo);
        fila.put("categoria_id", categoriaId);
        fila.put("frecuencia", frecuencia);
        fila.put("fecha_inicio", LocalDate.parse(proxima));
        fila.put("proxima_fecha", LocalDate.parse(proxima));
        fila.put("cuotas_total", cuotasTotal);
        fila.put("cuotas_pagadas", cuotasPagadas);
        fila.put("activo", activo);
        return new SimpleJdbcInsert(jdbc).withTableName("recurrente").usingGeneratedKeyColumns("id")
                .executeAndReturnKey(fila).longValue();
    }
}
