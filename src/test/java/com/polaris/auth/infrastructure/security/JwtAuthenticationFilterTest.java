package com.polaris.auth.infrastructure.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * El filtro es lo que convierte un token en un usuarioId dentro del contexto.
 * Si se equivoca, o no entras con token bueno, o entras sin token.
 */
class JwtAuthenticationFilterTest {

    private static final String SECRETO = "un-secreto-de-mas-de-32-caracteres-para-hs256";

    private final JwtService jwtService = new JwtService(SECRETO, 3600, "polaris");
    private final JwtAuthenticationFilter filtro = new JwtAuthenticationFilter(jwtService);

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("con un Bearer valido deja el usuarioId como principal")
    void tokenValidoAutentica() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwtService.generar(7L));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filtro.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(7L);
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("sin cabecera no autentica pero deja pasar la peticion")
    void sinCabeceraDejaPasar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        FilterChain chain = mock(FilterChain.class);

        filtro.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("un token invalido no autentica: la cadena decidira si eso es 401")
    void tokenInvalidoNoAutentica() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer esto-no-es-un-jwt");
        FilterChain chain = mock(FilterChain.class);

        filtro.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
