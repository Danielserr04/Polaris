package com.polaris.auth.infrastructure.security;

import com.polaris.auth.application.out.EnviarVerificacionPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Adaptador de dev: sin SMTP, sin cuenta de correo, sin dependencia nueva.
 * Escribe el enlace de verificacion en el log, para poder probar el registro
 * de punta a punta hoy mismo.
 *
 * <p>ES LA UNICA CLASE QUE ESCRIBE UN TOKEN Y UN EMAIL EN EL LOG, y por eso esta
 * limitada a {@code @Profile("dev")} (en prod la sustituye
 * SmtpEnviarVerificacionAdapter, que no los loguea). No quitar el perfil ni
 * copiar este log a otra clase: el token de verificacion es una credencial de
 * un solo uso. Hay un test que fija el perfil. Ver
 * docs/decisiones/029-logs-y-requestid.md y docs/modulos/auth.md.
 *
 * <p>Constructor explicito y no @RequiredArgsConstructor: @Value necesita
 * anotar el parametro del constructor, y Lombok no copia anotaciones propias
 * a los que genera. Mismo patron que JwtService.
 */
@Slf4j
@Component
@Profile("dev")
public class LogEnviarVerificacionAdapter implements EnviarVerificacionPort {

    private final String urlBase;

    public LogEnviarVerificacionAdapter(@Value("${polaris.url-base}") String urlBase) {
        this.urlBase = urlBase;
    }

    @Override
    public void enviar(String email, String nombre, String tokenVerificacion) {
        log.info("Verificacion de email para {} ({}): {}/api/auth/verificacion?token={}",
                nombre, email, urlBase, tokenVerificacion);
    }
}
