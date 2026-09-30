package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import com.polaris.kuiper.domain.model.PresupuestoFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.polaris.shared.persistence.CriteriaFalsa.describir;
import static org.assertj.core.api.Assertions.assertThat;

/** El filtro por usuario va siempre: es el aislamiento entre usuarios. */
class PresupuestoSpecificationsTest {

    @Test
    @DisplayName("sin filtro solo se filtra por usuario")
    void soloUsuario() {
        assertThat(describir(PresupuestoSpecifications.from(1L, null))).isEqualTo("equal(usuarioId, 1)");
        assertThat(describir(PresupuestoSpecifications.from(1L, PresupuestoFilter.builder().build())))
                .isEqualTo("equal(usuarioId, 1)");
    }

    @Test
    @DisplayName("periodo y categoria se filtran por su campo y por el id de la categoria")
    void periodoYCategoria() {
        PresupuestoFilter filtro = PresupuestoFilter.builder()
                .periodo(PeriodoPresupuesto.MENSUAL).categoriaId(10L).build();

        assertThat(describir(PresupuestoSpecifications.from(1L, filtro))).isEqualTo(
                "and(and(equal(usuarioId, 1), equal(periodo, MENSUAL)), equal(categoria.id, 10))");
    }
}
