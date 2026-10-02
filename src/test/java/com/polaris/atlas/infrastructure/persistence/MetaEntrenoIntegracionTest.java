package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.in.CreateMetaEntrenoInterface;
import com.polaris.atlas.application.in.ListMetaEntrenoInterface;
import com.polaris.atlas.domain.model.MetaEntreno;
import com.polaris.atlas.domain.model.MetaEntrenoFilter;
import com.polaris.atlas.domain.model.TipoMetaEntreno;
import com.polaris.shared.testing.IntegracionBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Metas contra MySQL 8.4 real: el ENUM del tipo, el punto de partida con el
 * record calculado en la base, el filtro por tipo y el ON DELETE CASCADE del
 * ejercicio. Usuario propio 9201, limpiado antes y despues.
 */
class MetaEntrenoIntegracionTest extends IntegracionBase {

    private static final Long U = 9201L;

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private CreateMetaEntrenoInterface crear;
    @Autowired
    private ListMetaEntrenoInterface listar;

    private Long press;

    @AfterEach
    void limpiar() {
        jdbc.update("delete from meta_entreno where usuario_id = ?", U);
        jdbc.update("delete from serie_registro where usuario_id = ?", U);
        jdbc.update("delete from sesion where usuario_id = ?", U);
        jdbc.update("delete from ejercicio where usuario_id = ?", U);
    }

    @BeforeEach
    void datos() {
        limpiar();
        press = insertar("ejercicio", Map.of("usuario_id", U, "nombre", "IT Press propio", "grupo_muscular", "Pecho"));
        Long sesion = insertar("sesion", Map.of("usuario_id", U, "fecha", LocalDate.now()));
        insertar("serie_registro", Map.of("usuario_id", U, "sesion_id", sesion, "ejercicio_id", press,
                "numero_serie", 1, "reps", 5, "peso_kg", new BigDecimal("82.50")));
    }

    @Test
    @DisplayName("Una marca parte del record actual, se lee con progreso y sesiones de la semana, y se filtra por tipo")
    void crearYListar() {
        crear.create(U, MetaEntreno.builder().tipo(TipoMetaEntreno.MARCA_EJERCICIO).ejercicioId(press)
                .valorObjetivo(new BigDecimal("100")).build());
        crear.create(U, MetaEntreno.builder().tipo(TipoMetaEntreno.SESIONES_SEMANA)
                .valorObjetivo(new BigDecimal("2")).build());

        List<MetaEntreno> marcas = listar.list(U, MetaEntrenoFilter.builder().tipo(TipoMetaEntreno.MARCA_EJERCICIO).build());
        assertThat(marcas).hasSize(1);
        assertThat(marcas.get(0).getValorInicial()).isEqualByComparingTo("82.50");
        assertThat(marcas.get(0).getEjercicioNombre()).isEqualTo("IT Press propio");

        MetaEntreno semana = listar.list(U, MetaEntrenoFilter.builder().tipo(TipoMetaEntreno.SESIONES_SEMANA).build()).get(0);
        assertThat(semana.getValorActual()).isEqualByComparingTo("1");
        assertThat(semana.getProgresoPct()).isEqualTo(50);
    }

    @Test
    @DisplayName("Borrar el ejercicio propio borra sus metas (ON DELETE CASCADE)")
    void cascada() {
        crear.create(U, MetaEntreno.builder().tipo(TipoMetaEntreno.MARCA_EJERCICIO).ejercicioId(press)
                .valorObjetivo(new BigDecimal("100")).build());
        jdbc.update("delete from serie_registro where usuario_id = ?", U);

        jdbc.update("delete from ejercicio where id = ?", press);

        assertThat(listar.list(U, new MetaEntrenoFilter())).isEmpty();
    }

    private Long insertar(String tabla, Map<String, Object> fila) {
        return new SimpleJdbcInsert(jdbc).withTableName(tabla).usingGeneratedKeyColumns("id")
                .executeAndReturnKey(fila).longValue();
    }
}
