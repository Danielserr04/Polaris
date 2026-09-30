package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.domain.model.AlimentoFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.polaris.shared.persistence.CriteriaFalsa.describir;
import static org.assertj.core.api.Assertions.assertThat;

/** Catalogo compartido: no hay filtro por usuario. */
class AlimentoSpecificationsTest {

    @Test
    @DisplayName("sin filtro, o con texto en blanco, no se filtra nada")
    void sinFiltro() {
        assertThat(describir(AlimentoSpecifications.from(null))).isNull();
        assertThat(describir(AlimentoSpecifications.from(AlimentoFilter.builder().build()))).isNull();
        assertThat(describir(AlimentoSpecifications.from(AlimentoFilter.builder().texto(" ").build()))).isNull();
    }

    @Test
    @DisplayName("el texto busca en minusculas en nombre y marca, declarando el escape")
    void textoEnNombreYMarca() {
        assertThat(describir(AlimentoSpecifications.from(AlimentoFilter.builder().texto("Arroz").build())))
                .isEqualTo("or(like(lower(nombre), %arroz%, \\), like(lower(marca), %arroz%, \\))");
    }

    @Test
    @DisplayName("% y _ en el texto se buscan literales")
    void textoConComodinesEsLiteral() {
        assertThat(describir(AlimentoSpecifications.from(AlimentoFilter.builder().texto("50%").build())))
                .isEqualTo("or(like(lower(nombre), %50\\%%, \\), like(lower(marca), %50\\%%, \\))");
        assertThat(describir(AlimentoSpecifications.from(AlimentoFilter.builder().texto("a_b").build())))
                .isEqualTo("or(like(lower(nombre), %a\\_b%, \\), like(lower(marca), %a\\_b%, \\))");
    }
}
