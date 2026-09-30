package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.domain.model.TipoContenido;
import com.polaris.odisea.domain.model.TituloFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.polaris.shared.persistence.CriteriaFalsa.describir;
import static org.assertj.core.api.Assertions.assertThat;

/** Catalogo compartido: no hay filtro por usuario. */
class TituloSpecificationsTest {

    @Test
    @DisplayName("sin filtro, o con texto en blanco, no se filtra nada")
    void sinFiltro() {
        assertThat(describir(TituloSpecifications.from(null))).isNull();
        assertThat(describir(TituloSpecifications.from(TituloFilter.builder().build()))).isNull();
        assertThat(describir(TituloSpecifications.from(TituloFilter.builder().texto("   ").build()))).isNull();
    }

    @Test
    @DisplayName("el tipo se filtra por su campo")
    void porTipo() {
        assertThat(describir(TituloSpecifications.from(TituloFilter.builder().tipo(TipoContenido.LIBRO).build())))
                .isEqualTo("equal(tipo, LIBRO)");
    }

    @Test
    @DisplayName("el texto busca en minusculas en titulo y tituloOriginal, declarando el escape")
    void textoEnTituloYOriginal() {
        assertThat(describir(TituloSpecifications.from(TituloFilter.builder().texto("Dune").build())))
                .isEqualTo("or(like(lower(titulo), %dune%, \\), like(lower(tituloOriginal), %dune%, \\))");
    }

    @Test
    @DisplayName("% y _ en el texto se buscan literales")
    void textoConComodinesEsLiteral() {
        assertThat(describir(TituloSpecifications.from(TituloFilter.builder().texto("100%").build())))
                .isEqualTo("or(like(lower(titulo), %100\\%%, \\), like(lower(tituloOriginal), %100\\%%, \\))");
        assertThat(describir(TituloSpecifications.from(TituloFilter.builder().texto("a_b").build())))
                .isEqualTo("or(like(lower(titulo), %a\\_b%, \\), like(lower(tituloOriginal), %a\\_b%, \\))");
    }

    @Test
    @DisplayName("tipo y texto se combinan")
    void tipoYTexto() {
        TituloFilter filtro = TituloFilter.builder().tipo(TipoContenido.SERIE).texto("x").build();

        assertThat(describir(TituloSpecifications.from(filtro))).isEqualTo(
                "and(equal(tipo, SERIE), or(like(lower(titulo), %x%, \\), like(lower(tituloOriginal), %x%, \\)))");
    }
}
