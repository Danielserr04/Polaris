package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.domain.model.PerfilFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.polaris.shared.persistence.CriteriaFalsa.describir;
import static org.assertj.core.api.Assertions.assertThat;

class PerfilSpecificationsTest {

    @Test
    @DisplayName("solo filtra por usuario: el filtro esta vacio a proposito")
    void soloUsuario() {
        assertThat(describir(PerfilSpecifications.from(1L, PerfilFilter.builder().build())))
                .isEqualTo("equal(usuarioId, 1)");
        assertThat(describir(PerfilSpecifications.from(1L, null))).isEqualTo("equal(usuarioId, 1)");
    }
}
