package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.application.out.EjercicioRepositoryPort;
import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.EjercicioFilter;
import com.polaris.shared.testing.IntegracionBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Specifications de Ejercicio contra MySQL: visibilidad (catalogo mas propios,
 * nunca los de otro usuario), el unique que no protege el catalogo (NULL) y la
 * busqueda de texto literal con PatronLike.
 */
class EjercicioSpecificationsIntegracionTest extends IntegracionBase {

    private static final Long A = 9401L;
    private static final Long B = 9402L;

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private EjercicioRepositoryPort ejercicios;

    @BeforeEach
    void datos() {
        jdbc.update("delete from serie_registro where ejercicio_id in "
                + "(select id from ejercicio where nombre like 'IT %' or usuario_id in (?, ?))", A, B);
        jdbc.update("delete from ejercicio where nombre like 'IT %' or usuario_id in (?, ?)", A, B);
        ejercicio(null, "IT Press banca", "Pecho");
        ejercicio(null, "IT Curl 100% concentracion", "Biceps");
        ejercicio(null, "IT Curl 1000 reps", "Biceps");
        ejercicio(A, "IT Mi remo", "Espalda");
        ejercicio(B, "IT Remo de B", "Espalda");
    }

    private List<String> nombres(Long usuario, EjercicioFilter filtro) {
        return ejercicios.findAll(usuario, filtro).stream().map(Ejercicio::getNombre)
                .filter(n -> n.startsWith("IT ")).toList();
    }

    @Test
    @DisplayName("Cada usuario ve el catalogo y lo suyo, nunca los ejercicios propios de otro")
    void visibilidad() {
        EjercicioFilter sinFiltro = EjercicioFilter.builder().build();

        assertThat(nombres(A, sinFiltro)).containsExactlyInAnyOrder(
                "IT Press banca", "IT Curl 100% concentracion", "IT Curl 1000 reps", "IT Mi remo");
        assertThat(nombres(B, sinFiltro)).containsExactlyInAnyOrder(
                "IT Press banca", "IT Curl 100% concentracion", "IT Curl 1000 reps", "IT Remo de B");
        assertThat(nombres(9999L, sinFiltro)).hasSize(3);
    }

    @Test
    @DisplayName("Texto: \"100%\" es literal y no casa con \"1000 reps\"")
    void porcentajeEsLiteral() {
        assertThat(nombres(A, EjercicioFilter.builder().texto("100%").build()))
                .containsExactly("IT Curl 100% concentracion");
        assertThat(nombres(A, EjercicioFilter.builder().texto("curl 100").build()))
                .containsExactlyInAnyOrder("IT Curl 100% concentracion", "IT Curl 1000 reps");
    }

    @Test
    @DisplayName("Grupo muscular: igualdad que ignora mayusculas y tildes; combinado con texto y con la visibilidad")
    void grupoMuscular() {
        assertThat(nombres(A, EjercicioFilter.builder().grupoMuscular("BICEPS").build()))
                .containsExactlyInAnyOrder("IT Curl 100% concentracion", "IT Curl 1000 reps");
        assertThat(nombres(A, EjercicioFilter.builder().grupoMuscular("bíceps").build())).hasSize(2);
        assertThat(nombres(A, EjercicioFilter.builder().grupoMuscular("espalda").texto("remo").build()))
                .containsExactly("IT Mi remo");
    }

    private void ejercicio(Long usuarioId, String nombre, String grupo) {
        jdbc.update("insert into ejercicio (usuario_id, nombre, grupo_muscular) values (?, ?, ?)",
                usuarioId, nombre, grupo);
    }
}
