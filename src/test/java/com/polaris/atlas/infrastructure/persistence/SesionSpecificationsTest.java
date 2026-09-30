package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.domain.model.SesionFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.polaris.shared.persistence.CriteriaFalsa.describir;
import static org.assertj.core.api.Assertions.assertThat;

/** El filtro por usuario va siempre: es el aislamiento entre usuarios. desde/hasta son un rango inclusivo. */
class SesionSpecificationsTest {

    @Test
    @DisplayName("sin filtro solo se filtra por usuario")
    void soloUsuario() {
        assertThat(describir(SesionSpecifications.from(1L, null))).isEqualTo("equal(usuarioId, 1)");
        assertThat(describir(SesionSpecifications.from(1L, SesionFilter.builder().build())))
                .isEqualTo("equal(usuarioId, 1)");
    }

    @Test
    @DisplayName("desde y hasta son un rango inclusivo")
    void rangoInclusivo() {
        SesionFilter filtro = SesionFilter.builder()
                .desde(LocalDate.of(2026, 9, 1)).hasta(LocalDate.of(2026, 9, 30)).build();

        assertThat(describir(SesionSpecifications.from(1L, filtro))).isEqualTo(
                "and(and(equal(usuarioId, 1), greaterThanOrEqualTo(fecha, 2026-09-01)), "
                        + "lessThanOrEqualTo(fecha, 2026-09-30))");
    }

    @Test
    @DisplayName("desde solo o hasta solo tambien filtran")
    void extremosSueltos() {
        assertThat(describir(SesionSpecifications.from(1L,
                SesionFilter.builder().desde(LocalDate.of(2026, 9, 1)).build())))
                .isEqualTo("and(equal(usuarioId, 1), greaterThanOrEqualTo(fecha, 2026-09-01))");
        assertThat(describir(SesionSpecifications.from(1L,
                SesionFilter.builder().hasta(LocalDate.of(2026, 9, 30)).build())))
                .isEqualTo("and(equal(usuarioId, 1), lessThanOrEqualTo(fecha, 2026-09-30))");
    }

    @Test
    @DisplayName("rutinaId se filtra por su campo y se combina con el rango")
    void porRutina() {
        assertThat(describir(SesionSpecifications.from(2L, SesionFilter.builder().rutinaId(7L).build())))
                .isEqualTo("and(equal(usuarioId, 2), equal(rutinaId, 7))");

        SesionFilter todo = SesionFilter.builder().desde(LocalDate.of(2026, 9, 1))
                .hasta(LocalDate.of(2026, 9, 30)).rutinaId(7L).build();
        assertThat(describir(SesionSpecifications.from(2L, todo))).isEqualTo(
                "and(and(and(equal(usuarioId, 2), greaterThanOrEqualTo(fecha, 2026-09-01)), "
                        + "lessThanOrEqualTo(fecha, 2026-09-30)), equal(rutinaId, 7))");
    }
}
