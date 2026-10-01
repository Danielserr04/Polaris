package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.out.AlimentoRepositoryPort;
import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.AlimentoFilter;
import com.polaris.shared.testing.IntegracionBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Busqueda de texto de Alimento (PatronLike) contra MySQL real: "50%" y "a_b"
 * son literales y no comodines, el backslash tambien, y la comparacion ignora
 * mayusculas y tildes por la collation utf8mb4_unicode_ci. Lo que el unitario
 * de PatronLike y de la Specification solo ven como una cadena.
 *
 * <p>Los alimentos son catalogo compartido (sin usuario_id): los del test llevan
 * el prefijo "IT " y se borran antes de cada test.
 */
class AlimentoSpecificationsIntegracionTest extends IntegracionBase {

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private AlimentoRepositoryPort alimentos;

    @BeforeEach
    void datos() {
        jdbc.update("delete from alimento where nombre like 'IT %'");
        alimento("IT Yogur 50% proteina", "Hacendado");
        alimento("IT Yogur 500 g", "Hacendado");
        alimento("IT Barrita a_b", null);
        alimento("IT Barrita axb", null);
        alimento("IT Ruta c:\\datos", null);
        alimento("IT Ruta c:datos", null);
        alimento("IT Jamón serrano", "Campofrío");
        alimento("IT Pan", "Bimbo 100%");
    }

    private List<String> nombres(String texto) {
        return alimentos.findAll(AlimentoFilter.builder().texto(texto).build()).stream()
                .map(Alimento::getNombre).filter(n -> n.startsWith("IT ")).toList();
    }

    @Test
    @DisplayName("\"50%\" busca el texto literal: no devuelve \"500 g\", que casaria si el % fuera un comodin")
    void porcentajeEsLiteral() {
        assertThat(nombres("50%")).containsExactly("IT Yogur 50% proteina");
        // Sin el %, "50" si casa con los dos.
        assertThat(nombres("yogur 50")).containsExactlyInAnyOrder("IT Yogur 50% proteina", "IT Yogur 500 g");
    }

    @Test
    @DisplayName("\"a_b\" busca el texto literal: no devuelve \"axb\", que casaria si el _ fuera un comodin")
    void guionBajoEsLiteral() {
        assertThat(nombres("a_b")).containsExactly("IT Barrita a_b");
    }

    @Test
    @DisplayName("El propio caracter de escape (backslash) tambien es literal")
    void backslashEsLiteral() {
        assertThat(nombres("c:\\d")).containsExactly("IT Ruta c:\\datos");
    }

    @Test
    @DisplayName("Los comodines tambien se escapan en la marca")
    void porcentajeEnLaMarca() {
        assertThat(nombres("100%")).containsExactly("IT Pan");
    }

    @Test
    @DisplayName("Ignora mayusculas y tildes en nombre y marca (collation utf8mb4_unicode_ci)")
    void ignoraMayusculasYTildes() {
        assertThat(nombres("JAMON")).containsExactly("IT Jamón serrano");
        assertThat(nombres("jamón")).containsExactly("IT Jamón serrano");
        assertThat(nombres("campofrio")).containsExactly("IT Jamón serrano");
        assertThat(nombres("HACENDADO")).containsExactlyInAnyOrder("IT Yogur 50% proteina", "IT Yogur 500 g");
    }

    @Test
    @DisplayName("Sin texto o en blanco no filtra; un texto que no esta da lista vacia")
    void sinTextoYSinResultados() {
        assertThat(nombres(null)).hasSize(8);
        assertThat(nombres("   ")).hasSize(8);
        assertThat(nombres("zzz-no-existe")).isEmpty();
    }

    private void alimento(String nombre, String marca) {
        jdbc.update("insert into alimento (nombre, marca, kcal_100g, proteinas_100g, carbohidratos_100g, "
                + "grasas_100g, fuente_externa) values (?, ?, 100, 10, 10, 1, 'MANUAL')", nombre, marca);
    }
}
