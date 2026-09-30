package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.domain.model.RutinaFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.polaris.shared.persistence.CriteriaFalsa.describir;
import static org.assertj.core.api.Assertions.assertThat;

/** El filtro por usuario va siempre: es el aislamiento entre usuarios. */
class RutinaSpecificationsTest {

    @Test
    @DisplayName("sin filtro, o con activa nula, solo se filtra por usuario")
    void soloUsuario() {
        assertThat(describir(RutinaSpecifications.from(1L, null))).isEqualTo("equal(usuarioId, 1)");
        assertThat(describir(RutinaSpecifications.from(1L, RutinaFilter.builder().build())))
                .isEqualTo("equal(usuarioId, 1)");
    }

    @Test
    @DisplayName("activa=true y activa=false se filtran por su campo")
    void porActiva() {
        assertThat(describir(RutinaSpecifications.from(1L, RutinaFilter.builder().activa(true).build())))
                .isEqualTo("and(equal(usuarioId, 1), equal(activa, true))");
        assertThat(describir(RutinaSpecifications.from(2L, RutinaFilter.builder().activa(false).build())))
                .isEqualTo("and(equal(usuarioId, 2), equal(activa, false))");
    }
}
