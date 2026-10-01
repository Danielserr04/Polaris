package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.application.out.EntradaRepositoryPort;
import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.domain.model.EntradaFilter;
import com.polaris.shared.testing.IntegracionBase;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El listado de entradas ejecutado contra MySQL: una sola sentencia sea cual sea el numero de
 * entradas (antes, el titulo EAGER de cada entrada costaba una sentencia mas: 14.001 sentencias
 * y 7,3 s con 14.000 entradas), y el aislamiento entre usuarios.
 */
class EntradaListadoIntegracionTest extends IntegracionBase {

    private static final Long A = 9501L;
    private static final Long B = 9502L;
    private static final int ENTRADAS = 25;

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private EntradaRepositoryPort entradas;
    @Autowired
    private EntityManagerFactory emf;

    @BeforeEach
    void datos() {
        jdbc.update("delete from entrada where usuario_id in (?, ?)", A, B);
        jdbc.update("delete from titulo where titulo like 'N+1 test %'");
        for (int i = 0; i < ENTRADAS; i++) {
            Long titulo = titulo("N+1 test " + i);
            jdbc.update("insert into entrada (usuario_id, titulo_id, estado, favorito) values (?, ?, 'PENDIENTE', b'0')", A, titulo);
        }
        jdbc.update("insert into entrada (usuario_id, titulo_id, estado, favorito) values (?, ?, 'PENDIENTE', b'0')", B, titulo("N+1 test ajeno"));
    }

    private Long titulo(String nombre) {
        jdbc.update("insert into titulo (tipo, titulo, fuente_externa) values ('PELICULA', ?, 'MANUAL')", nombre);
        return jdbc.queryForObject("select id from titulo where titulo = ?", Long.class, nombre);
    }

    @Test
    @DisplayName("Listar N entradas con su titulo es una sola sentencia, no N+1")
    void unaSolaSentencia() {
        Statistics estadisticas = emf.unwrap(SessionFactory.class).getStatistics();
        estadisticas.setStatisticsEnabled(true);
        estadisticas.clear();

        List<Entrada> lista = entradas.findAll(A, EntradaFilter.builder().build());

        assertThat(lista).hasSize(ENTRADAS);
        assertThat(lista).allSatisfy(e -> assertThat(e.getTitulo().getTitulo()).startsWith("N+1 test "));
        assertThat(estadisticas.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Solo salen las entradas del usuario")
    void soloLasDelUsuario() {
        assertThat(entradas.findAll(B, EntradaFilter.builder().build())).hasSize(1);
    }
}
