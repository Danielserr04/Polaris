package com.polaris.auth.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * El JWT es lo que separa un endpoint protegido de uno abierto, asi que los
 * casos de rechazo importan mas que el camino feliz.
 */
class JwtServiceTest {

    private static final String SECRETO = "un-secreto-de-mas-de-32-caracteres-para-hs256";
    private static final String OTRO_SECRETO = "otro-secreto-distinto-de-mas-de-32-caracteres";

    private final JwtService jwtService = new JwtService(SECRETO, 3600, "polaris");

    @Test
    @DisplayName("un token recien emitido se valida y devuelve el usuarioId")
    void tokenValidoDevuelveUsuarioId() {
        String token = jwtService.generar(7L);

        assertThat(jwtService.validarYExtraerUsuarioId(token)).contains(7L);
    }

    @Test
    @DisplayName("un token firmado con otro secreto se rechaza")
    void tokenDeOtroEmisorSeRechaza() {
        String ajeno = new JwtService(OTRO_SECRETO, 3600, "polaris").generar(7L);

        assertThat(jwtService.validarYExtraerUsuarioId(ajeno)).isEmpty();
    }

    @Test
    @DisplayName("un token caducado se rechaza")
    void tokenCaducadoSeRechaza() {
        String caducado = new JwtService(SECRETO, -1, "polaris").generar(7L);

        assertThat(jwtService.validarYExtraerUsuarioId(caducado)).isEmpty();
    }

    @Test
    @DisplayName("basura en la cabecera no revienta, solo se rechaza")
    void tokenMalformadoSeRechaza() {
        assertThat(jwtService.validarYExtraerUsuarioId("esto-no-es-un-jwt")).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("un secreto corto revienta al arrancar, no en la primera peticion")
    void secretoCortoFallaAlConstruir() {
        assertThatThrownBy(() -> new JwtService("corto", 3600, "polaris"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }
}
