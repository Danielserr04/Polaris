package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.domain.model.EntradaFilter;
import com.polaris.odisea.domain.model.EstadoEntrada;
import com.polaris.odisea.domain.model.TipoContenido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.polaris.shared.persistence.CriteriaFalsa.describir;
import static org.assertj.core.api.Assertions.assertThat;

/** El filtro por usuario va siempre; el tipo se filtra por el titulo referenciado. */
class EntradaSpecificationsTest {

    @Test
    @DisplayName("sin filtro solo se filtra por usuario")
    void soloUsuario() {
        assertThat(describir(EntradaSpecifications.from(1L, null))).isEqualTo("equal(usuarioId, 1)");
        assertThat(describir(EntradaSpecifications.from(1L, EntradaFilter.builder().build())))
                .isEqualTo("equal(usuarioId, 1)");
    }

    @Test
    @DisplayName("el tipo sale del titulo (titulo.tipo) y el estado es propio de la entrada")
    void tipoDelTituloYEstado() {
        EntradaFilter filtro = EntradaFilter.builder().tipo(TipoContenido.JUEGO).estado(EstadoEntrada.EN_CURSO).build();

        assertThat(describir(EntradaSpecifications.from(1L, filtro))).isEqualTo(
                "and(and(equal(usuarioId, 1), equal(titulo.tipo, JUEGO)), equal(estado, EN_CURSO))");
    }
}
