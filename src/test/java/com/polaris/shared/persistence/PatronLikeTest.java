package com.polaris.shared.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PatronLikeTest {

    @Test
    @DisplayName("el escape es la barra invertida")
    void elEscapeEsLaBarraInvertida() {
        assertThat(PatronLike.ESCAPE).isEqualTo('\\');
    }

    @Test
    @DisplayName("un texto normal se envuelve en % y se pasa a minusculas")
    void textoNormalEnMinusculas() {
        assertThat(PatronLike.contieneMinusculas("Arroz")).isEqualTo("%arroz%");
    }

    @Test
    @DisplayName("% y _ del texto se escapan para que sean literales")
    void escapaComodines() {
        assertThat(PatronLike.contieneMinusculas("50%")).isEqualTo("%50\\%%");
        assertThat(PatronLike.contieneMinusculas("a_b")).isEqualTo("%a\\_b%");
    }

    @Test
    @DisplayName("el propio caracter de escape tambien se escapa")
    void escapaElEscape() {
        assertThat(PatronLike.contieneMinusculas("a\\b")).isEqualTo("%a\\\\b%");
    }

    @Test
    @DisplayName("el escape y un comodin seguidos se escapan los dos")
    void escapeYComodinJuntos() {
        assertThat(PatronLike.contieneMinusculas("\\%")).isEqualTo("%\\\\\\%%");
    }

    @Test
    @DisplayName("un texto solo de comodines se convierte en literales")
    void soloComodines() {
        assertThat(PatronLike.contieneMinusculas("%_")).isEqualTo("%\\%\\_%");
    }
}
