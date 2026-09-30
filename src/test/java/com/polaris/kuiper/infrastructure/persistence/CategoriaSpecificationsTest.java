package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.CategoriaFilter;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.polaris.shared.persistence.CriteriaFalsa.describir;
import static org.assertj.core.api.Assertions.assertThat;

/** El filtro por usuario va siempre: es el aislamiento entre usuarios. */
class CategoriaSpecificationsTest {

    @Test
    @DisplayName("sin filtro solo se filtra por usuario")
    void soloUsuario() {
        assertThat(describir(CategoriaSpecifications.from(1L, null))).isEqualTo("equal(usuarioId, 1)");
        assertThat(describir(CategoriaSpecifications.from(1L, CategoriaFilter.builder().build())))
                .isEqualTo("equal(usuarioId, 1)");
    }

    @Test
    @DisplayName("con tipo se anade el filtro por tipo")
    void conTipo() {
        CategoriaFilter filtro = CategoriaFilter.builder().tipo(TipoMovimiento.GASTO).build();

        assertThat(describir(CategoriaSpecifications.from(1L, filtro)))
                .isEqualTo("and(equal(usuarioId, 1), equal(tipo, GASTO))");
    }
}
