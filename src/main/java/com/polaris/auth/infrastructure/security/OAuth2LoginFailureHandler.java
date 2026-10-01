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
import java.util.Set;

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

    /**
     * Codigos de error OAuth2 que provoca el cliente y no la configuracion del
     * servidor. Cualquier otro (invalid_client, redirect_uri_mismatch,
     * invalid_token_response...) es un fallo nuestro y se loguea como ERROR.
     */
    private static final Set<String> CODIGOS_DEL_CLIENTE = Set.of(
            "access_denied",
            "invalid_request",
            "authorization_request_not_found",
            "invalid_state_parameter");

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String codigo = oauth2Exception.getError().getErrorCode();
            if (CODIGOS_DEL_CLIENTE.contains(codigo)) {
                // El usuario cancela en Google, o el callback llega sin estado o
                // caducado: situacion del cliente, no un fallo nuestro. Sin stacktrace.
                log.warn("Login con Google no completado. Codigo: {}", codigo);
            } else {
                log.error("Fallo el login con Google. Codigo: {} · Descripcion: {}",
                        codigo,
                        oauth2Exception.getError().getDescription(),
                        exception);
            }
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
