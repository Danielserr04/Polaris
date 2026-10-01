package com.polaris.auth.infrastructure.security;

import com.polaris.auth.application.in.GetOrCreateUsuarioInterface;
import com.polaris.auth.domain.model.PerfilGoogle;
import com.polaris.auth.domain.model.Usuario;
import com.polaris.auth.infrastructure.persistence.dto.out.TokenDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Punto de union entre el login de Google y el JWT propio.
 *
 * <p>Google ya ha verificado quien eres; aqui se traduce su respuesta al dominio,
 * se da de alta el usuario si es la primera vez, y se emite el token de Polaris.
 *
 * <p>Termina redirigiendo al frontend (/auth/callback) con el token en el
 * <b>fragmento</b> de la URL, no en la query: el fragmento no viaja al servidor ni
 * queda en logs de acceso ni en el Referer. Ver ADR 031.
 */
@Slf4j
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final GetOrCreateUsuarioInterface getOrCreateUsuario;
    private final JwtService jwtService;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(GetOrCreateUsuarioInterface getOrCreateUsuario,
                                     JwtService jwtService,
                                     @Value("${polaris.frontend-url}") String frontendUrl) {
        this.getOrCreateUsuario = getOrCreateUsuario;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Usuario usuario = getOrCreateUsuario.getOrCreate(aPerfilGoogle(oauth2User));

        // Solo el id: el email es un dato personal y el id basta para seguir al usuario.
        log.info("Login correcto de usuario {}", usuario.getId());

        TokenDto token = TokenDto.bearer(
                jwtService.generar(usuario.getId()),
                jwtService.getExpiracionSegundos());

        // Una respuesta con el token no se guarda en ninguna cache.
        response.setHeader("Cache-Control", "no-store");
        response.sendRedirect(frontendUrl + "/auth/callback#token=" + token.token()
                + "&expiraEnSegundos=" + token.expiraEnSegundos());
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
