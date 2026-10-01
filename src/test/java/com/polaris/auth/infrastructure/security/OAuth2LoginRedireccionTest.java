package com.polaris.auth.infrastructure.security;

import com.polaris.auth.application.in.GetOrCreateUsuarioInterface;
import com.polaris.auth.domain.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** A donde manda el login con Google al terminar (ADR 031). */
class OAuth2LoginRedireccionTest {

    private static final String FRONTEND = "http://localhost:5173";
    private static final String SECRETO = "secreto-de-tests-que-no-vale-para-nada-fuera-de-aqui-0123456789";

    @Test
    @DisplayName("exito: redirige a /auth/callback con el token en el fragmento, no en la query")
    void exitoRedirigeConElTokenEnElFragmento() throws Exception {
        GetOrCreateUsuarioInterface getOrCreate = mock(GetOrCreateUsuarioInterface.class);
        when(getOrCreate.getOrCreate(any())).thenReturn(Usuario.builder().id(7L).build());
        JwtService jwt = new JwtService(SECRETO, 3600, "polaris");
        Authentication autenticacion = mock(Authentication.class);
        when(autenticacion.getPrincipal()).thenReturn(mock(OAuth2User.class));
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        new OAuth2LoginSuccessHandler(getOrCreate, jwt, FRONTEND)
                .onAuthenticationSuccess(new MockHttpServletRequest(), respuesta, autenticacion);

        String destino = respuesta.getRedirectedUrl();
        assertThat(destino).startsWith(FRONTEND + "/auth/callback#token=eyJ").endsWith("&expiraEnSegundos=3600");
        assertThat(destino).doesNotContain("?");
        assertThat(respuesta.getHeader("Cache-Control")).isEqualTo("no-store");
    }

    @Test
    @DisplayName("el token del fragmento es un JWT de sesion valido para el usuario")
    void elTokenDelFragmentoEsValido() throws Exception {
        GetOrCreateUsuarioInterface getOrCreate = mock(GetOrCreateUsuarioInterface.class);
        when(getOrCreate.getOrCreate(any())).thenReturn(Usuario.builder().id(7L).build());
        JwtService jwt = new JwtService(SECRETO, 3600, "polaris");
        Authentication autenticacion = mock(Authentication.class);
        when(autenticacion.getPrincipal()).thenReturn(mock(OAuth2User.class));
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        new OAuth2LoginSuccessHandler(getOrCreate, jwt, FRONTEND)
                .onAuthenticationSuccess(new MockHttpServletRequest(), respuesta, autenticacion);

        String destino = respuesta.getRedirectedUrl();
        String token = destino.substring(destino.indexOf("#token=") + 7, destino.indexOf("&expira"));
        assertThat(jwt.validarYExtraerUsuarioId(token)).contains(7L);
    }

    @Test
    @DisplayName("cancelar en Google vuelve a /login?error=cancelado")
    void cancelarVuelveAlLoginConMotivoCancelado() throws Exception {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        new OAuth2LoginFailureHandler(FRONTEND).onAuthenticationFailure(
                new MockHttpServletRequest(), respuesta,
                new OAuth2AuthenticationException(new OAuth2Error("access_denied")));

        assertThat(respuesta.getRedirectedUrl()).isEqualTo(FRONTEND + "/login?error=cancelado");
    }

    @Test
    @DisplayName("cualquier otro fallo vuelve a /login?error=google, sin el codigo de Google")
    void otroFalloVuelveAlLoginConMotivoGoogle() throws Exception {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        new OAuth2LoginFailureHandler(FRONTEND).onAuthenticationFailure(
                new MockHttpServletRequest(), respuesta,
                new OAuth2AuthenticationException(new OAuth2Error("redirect_uri_mismatch")));

        assertThat(respuesta.getRedirectedUrl()).isEqualTo(FRONTEND + "/login?error=google");
    }

    @Test
    @DisplayName("una excepcion que no es de OAuth2 tambien vuelve a /login?error=google")
    void excepcionGenericaVuelveAlLogin() throws Exception {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        new OAuth2LoginFailureHandler(FRONTEND).onAuthenticationFailure(
                new MockHttpServletRequest(), respuesta, new BadCredentialsException("x"));

        assertThat(respuesta.getRedirectedUrl()).isEqualTo(FRONTEND + "/login?error=google");
    }
}
