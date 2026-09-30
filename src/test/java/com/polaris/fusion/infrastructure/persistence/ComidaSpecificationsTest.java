package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.domain.model.ComidaFilter;
import com.polaris.fusion.domain.model.MomentoComida;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.polaris.shared.persistence.CriteriaFalsa.describir;
import static org.assertj.core.api.Assertions.assertThat;

/** El filtro por usuario va siempre; fecha es un dia exacto y desde/hasta un rango inclusivo. */
class ComidaSpecificationsTest {

    @Test
    @DisplayName("sin filtro solo se filtra por usuario")
    void soloUsuario() {
        assertThat(describir(ComidaSpecifications.from(1L, null))).isEqualTo("equal(usuarioId, 1)");
        assertThat(describir(ComidaSpecifications.from(1L, ComidaFilter.builder().build())))
                .isEqualTo("equal(usuarioId, 1)");
    }

    @Test
    @DisplayName("fecha es un dia exacto y momento se filtra por su campo")
    void fechaExactaYMomento() {
        ComidaFilter filtro = ComidaFilter.builder().fecha(LocalDate.of(2026, 9, 30)).momento(MomentoComida.CENA).build();

        assertThat(describir(ComidaSpecifications.from(1L, filtro))).isEqualTo(
                "and(and(equal(usuarioId, 1), equal(fecha, 2026-09-30)), equal(momento, CENA))");
    }

    @Test
    @DisplayName("desde y hasta son un rango inclusivo")
    void rangoInclusivo() {
        ComidaFilter filtro = ComidaFilter.builder()
                .desde(LocalDate.of(2026, 9, 1)).hasta(LocalDate.of(2026, 9, 30)).build();

        assertThat(describir(ComidaSpecifications.from(1L, filtro))).isEqualTo(
                "and(and(equal(usuarioId, 1), greaterThanOrEqualTo(fecha, 2026-09-01)), "
                        + "lessThanOrEqualTo(fecha, 2026-09-30))");
    }
}
