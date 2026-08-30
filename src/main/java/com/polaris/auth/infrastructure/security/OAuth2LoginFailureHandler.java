package com.polaris.auth.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polaris.shared.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Que hacer cuando el login con Google no sale.
 *
 * <p>Sin esto, Spring Security redirige a /login?error, una pagina que en una API
 * no existe: te quedas mirando un 404 sin saber que ha fallado. Aqui se responde
 * un 401 con el mismo formato de error que el resto de Polaris.
 *
 * <p>El codigo que manda Google (redirect_uri_mismatch, access_denied,
 * invalid_client) se registra en el log, que es donde hace falta para arreglarlo,
 * pero no se devuelve al cliente: describe la configuracion del servidor.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            log.error("Fallo el login con Google. Codigo: {} · Descripcion: {}",
                    oauth2Exception.getError().getErrorCode(),
                    oauth2Exception.getError().getDescription(),
                    exception);
        } else {
            log.error("Fallo el login con Google", exception);
        }

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "No se pudo completar el login con Google",
                request.getRequestURI());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
