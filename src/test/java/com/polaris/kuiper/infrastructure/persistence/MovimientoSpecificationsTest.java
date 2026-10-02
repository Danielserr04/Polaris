package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.polaris.shared.persistence.CriteriaFalsa.describir;
import static org.assertj.core.api.Assertions.assertThat;

/** El filtro por usuario y el de fuera de la papelera van siempre; el rango de fechas es inclusivo. */
class MovimientoSpecificationsTest {

    @Test
    @DisplayName("sin filtro solo se filtra por usuario y fuera de la papelera")
    void soloUsuario() {
        assertThat(describir(MovimientoSpecifications.from(1L, null)))
                .isEqualTo("and(equal(usuarioId, 1), isNull(borradoEn))");
        assertThat(describir(MovimientoSpecifications.from(1L, MovimientoFilter.builder().build())))
                .isEqualTo("and(equal(usuarioId, 1), isNull(borradoEn))");
    }

    @Test
    @DisplayName("el rango de fechas es inclusivo por ambos extremos")
    void rangoInclusivo() {
        MovimientoFilter filtro = MovimientoFilter.builder()
                .desde(LocalDate.of(2026, 9, 1)).hasta(LocalDate.of(2026, 9, 30)).build();

        assertThat(describir(MovimientoSpecifications.from(1L, filtro))).isEqualTo(
                "and(and(and(equal(usuarioId, 1), isNull(borradoEn)), greaterThanOrEqualTo(fecha, 2026-09-01)), "
                        + "lessThanOrEqualTo(fecha, 2026-09-30))");
    }

    @Test
    @DisplayName("la categoria se filtra por su id y el tipo por su campo")
    void categoriaYTipo() {
        MovimientoFilter filtro = MovimientoFilter.builder().categoriaId(10L).tipo(TipoMovimiento.INGRESO).build();

        assertThat(describir(MovimientoSpecifications.from(1L, filtro))).isEqualTo(
                "and(and(and(equal(usuarioId, 1), isNull(borradoEn)), equal(categoria.id, 10)), equal(tipo, INGRESO))");
    }
}
