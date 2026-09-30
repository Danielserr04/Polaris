package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.domain.model.EjercicioFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.polaris.shared.persistence.CriteriaFalsa.describir;
import static org.assertj.core.api.Assertions.assertThat;

/** La visibilidad (catalogo mas propios) va siempre: es el aislamiento entre usuarios. */
class EjercicioSpecificationsTest {

    private static final String VISIBLE = "or(isNull(usuarioId), equal(usuarioId, 1))";

    @Test
    @DisplayName("sin filtro, o con filtros en blanco, solo se filtra por visibilidad: catalogo o del usuario")
    void soloVisibilidad() {
        assertThat(describir(EjercicioSpecifications.from(1L, null))).isEqualTo(VISIBLE);
        assertThat(describir(EjercicioSpecifications.from(1L, EjercicioFilter.builder().build()))).isEqualTo(VISIBLE);
        assertThat(describir(EjercicioSpecifications.from(1L,
                EjercicioFilter.builder().grupoMuscular(" ").texto(" ").build()))).isEqualTo(VISIBLE);
    }

    @Test
    @DisplayName("con grupo muscular se anade una igualdad, sin espacios sobrantes")
    void conGrupoMuscular() {
        EjercicioFilter filtro = EjercicioFilter.builder().grupoMuscular(" Pecho ").build();

        assertThat(describir(EjercicioSpecifications.from(1L, filtro)))
                .isEqualTo("and(" + VISIBLE + ", equal(grupoMuscular, Pecho))");
    }

    @Test
    @DisplayName("el texto busca en minusculas en el nombre, declarando el escape")
    void conTexto() {
        EjercicioFilter filtro = EjercicioFilter.builder().texto("Press").build();

        assertThat(describir(EjercicioSpecifications.from(1L, filtro)))
                .isEqualTo("and(" + VISIBLE + ", like(lower(nombre), %press%, \\))");
    }

    @Test
    @DisplayName("% y _ en el texto se buscan literales")
    void textoConComodinesEsLiteral() {
        assertThat(describir(EjercicioSpecifications.from(1L, EjercicioFilter.builder().texto("50%").build())))
                .isEqualTo("and(" + VISIBLE + ", like(lower(nombre), %50\\%%, \\))");
        assertThat(describir(EjercicioSpecifications.from(1L, EjercicioFilter.builder().texto("a_b").build())))
                .isEqualTo("and(" + VISIBLE + ", like(lower(nombre), %a\\_b%, \\))");
    }

    @Test
    @DisplayName("grupo y texto a la vez se combinan con and, empezando por la visibilidad")
    void grupoYTexto() {
        EjercicioFilter filtro = EjercicioFilter.builder().grupoMuscular("Pecho").texto("press").build();

        assertThat(describir(EjercicioSpecifications.from(1L, filtro)))
                .isEqualTo("and(and(" + VISIBLE + ", equal(grupoMuscular, Pecho)), like(lower(nombre), %press%, \\))");
    }
}
