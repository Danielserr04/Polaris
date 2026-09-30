package com.polaris.shared.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** La zona por defecto es estado global de la JVM: se restaura tras cada test. */
class ZonaHorariaTest {

    private TimeZone original;

    @BeforeEach
    void guardarZona() {
        original = TimeZone.getDefault();
    }

    @AfterEach
    void restaurarZona() {
        TimeZone.setDefault(original);
    }

    @Test
    void aplicaMadridPorDefecto() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        ZonaHoraria.aplicar(ZonaHoraria.POR_DEFECTO);

        assertThat(TimeZone.getDefault().getID()).isEqualTo("Europe/Madrid");
    }

    @Test
    void aplicaLaZonaIndicada() {
        ZonaHoraria.aplicar("UTC");

        assertThat(TimeZone.getDefault().getID()).isEqualTo("UTC");
    }

    @Test
    void idInvalidoLanzaExcepcionYNoCambiaLaZona() {
        assertThatThrownBy(() -> ZonaHoraria.aplicar("Marte/Olympus"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Marte/Olympus");

        assertThat(TimeZone.getDefault()).isEqualTo(original);
    }
}
