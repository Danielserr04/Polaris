package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.domain.model.RegistroPesoFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.polaris.shared.persistence.CriteriaFalsa.describir;
import static org.assertj.core.api.Assertions.assertThat;

/** El filtro por usuario va siempre y el rango de fechas es inclusivo. */
class RegistroPesoSpecificationsTest {

    @Test
    @DisplayName("sin filtro solo se filtra por usuario")
    void soloUsuario() {
        assertThat(describir(RegistroPesoSpecifications.from(1L, null))).isEqualTo("equal(usuarioId, 1)");
        assertThat(describir(RegistroPesoSpecifications.from(1L, RegistroPesoFilter.builder().build())))
                .isEqualTo("equal(usuarioId, 1)");
    }

    @Test
    @DisplayName("desde y hasta son inclusivos y pueden ir por separado")
    void rangoInclusivo() {
        RegistroPesoFilter completo = RegistroPesoFilter.builder()
                .desde(LocalDate.of(2026, 9, 1)).hasta(LocalDate.of(2026, 9, 30)).build();
        RegistroPesoFilter soloDesde = RegistroPesoFilter.builder().desde(LocalDate.of(2026, 9, 1)).build();

        assertThat(describir(RegistroPesoSpecifications.from(1L, completo))).isEqualTo(
                "and(and(equal(usuarioId, 1), greaterThanOrEqualTo(fecha, 2026-09-01)), "
                        + "lessThanOrEqualTo(fecha, 2026-09-30))");
        assertThat(describir(RegistroPesoSpecifications.from(1L, soloDesde)))
                .isEqualTo("and(equal(usuarioId, 1), greaterThanOrEqualTo(fecha, 2026-09-01))");
    }
}
