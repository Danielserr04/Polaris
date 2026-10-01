package com.polaris.auth.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * Que hacer cuando el login con Google no sale.
 *
 * <p>Se vuelve a la pantalla de login del frontend con un motivo en la query
 * (/login?error=cancelado o /login?error=google): ahi el usuario ve un mensaje
 * legible en vez de un JSON suelto en el navegador.
 *
 * <p>El codigo que manda Google (redirect_uri_mismatch, access_denied,
 * invalid_client) se registra en el log, que es donde hace falta para arreglarlo,
 * pero no se devuelve al cliente: describe la configuracion del servidor.
 */
@Slf4j
@Component
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

    private final String frontendUrl;

    public OAuth2LoginFailureHandler(@Value("${polaris.frontend-url}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String motivo = "google";
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String codigo = oauth2Exception.getError().getErrorCode();
            if (CODIGOS_DEL_CLIENTE.contains(codigo)) {
                // El usuario cancela en Google, o el callback llega sin estado o
                // caducado: situacion del cliente, no un fallo nuestro. Sin stacktrace.
                log.warn("Login con Google no completado. Codigo: {}", codigo);
                if ("access_denied".equals(codigo)) {
                    motivo = "cancelado";
                }
            } else {
                log.error("Fallo el login con Google. Codigo: {} · Descripcion: {}",
                        codigo,
                        oauth2Exception.getError().getDescription(),
                        exception);
            }
        } else {
            log.error("Fallo el login con Google", exception);
        }

        response.sendRedirect(frontendUrl + "/login?error=" + motivo);
    }
}
