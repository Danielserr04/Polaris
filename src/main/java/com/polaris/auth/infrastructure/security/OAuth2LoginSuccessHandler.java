package com.polaris.auth.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polaris.auth.application.in.GetOrCreateUsuarioInterface;
import com.polaris.auth.domain.model.PerfilGoogle;
import com.polaris.auth.domain.model.Usuario;
import com.polaris.auth.infrastructure.persistence.dto.out.TokenDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Punto de union entre el login de Google y el JWT propio.
 *
 * <p>Google ya ha verificado quien eres; aqui se traduce su respuesta al dominio,
 * se da de alta el usuario si es la primera vez, y se emite el token de Polaris.
 *
 * <p>Devuelve el JSON directamente en el navegador porque todavia no hay frontend
 * al que redirigir. Cuando exista React (despues de B3) esto pasa a ser un
 * redirect con el token. Ver docs/modulos/auth.md.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final GetOrCreateUsuarioInterface getOrCreateUsuario;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Usuario usuario = getOrCreateUsuario.getOrCreate(aPerfilGoogle(oauth2User));

        log.info("Login correcto de usuario {} ({})", usuario.getId(), usuario.getEmail());

        TokenDto body = TokenDto.bearer(
                jwtService.generar(usuario.getId()),
                jwtService.getExpiracionSegundos());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), body);
    }

    /**
     * "sub" es el identificador estable de la cuenta de Google. El email no lo es:
     * puede cambiar sin que cambie el sub.
     */
    private PerfilGoogle aPerfilGoogle(OAuth2User oauth2User) {
        return new PerfilGoogle(
                oauth2User.getAttribute("sub"),
                oauth2User.getAttribute("email"),
                oauth2User.getAttribute("name"),
                oauth2User.getAttribute("picture"));
    }
}
