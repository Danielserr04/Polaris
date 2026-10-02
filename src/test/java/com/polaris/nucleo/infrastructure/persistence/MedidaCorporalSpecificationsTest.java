package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.domain.model.MedidaCorporalFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.polaris.shared.persistence.CriteriaFalsa.describir;
import static org.assertj.core.api.Assertions.assertThat;

/** El filtro por usuario va siempre y el rango de fechas es inclusivo. */
class MedidaCorporalSpecificationsTest {

    @Test
    @DisplayName("sin filtro solo se filtra por usuario")
    void soloUsuario() {
        assertThat(describir(MedidaCorporalSpecifications.from(1L, null))).isEqualTo("equal(usuarioId, 1)");
        assertThat(describir(MedidaCorporalSpecifications.from(1L, MedidaCorporalFilter.builder().build())))
                .isEqualTo("equal(usuarioId, 1)");
    }

    @Test
    @DisplayName("desde y hasta son inclusivos y pueden ir por separado")
    void rangoInclusivo() {
        MedidaCorporalFilter completo = MedidaCorporalFilter.builder()
                .desde(LocalDate.of(2026, 9, 1)).hasta(LocalDate.of(2026, 9, 30)).build();
        MedidaCorporalFilter soloDesde = MedidaCorporalFilter.builder().desde(LocalDate.of(2026, 9, 1)).build();

        assertThat(describir(MedidaCorporalSpecifications.from(1L, completo))).isEqualTo(
                "and(and(equal(usuarioId, 1), greaterThanOrEqualTo(fecha, 2026-09-01)), "
                        + "lessThanOrEqualTo(fecha, 2026-09-30))");
        assertThat(describir(MedidaCorporalSpecifications.from(1L, soloDesde)))
                .isEqualTo("and(equal(usuarioId, 1), greaterThanOrEqualTo(fecha, 2026-09-01))");
    }
}
