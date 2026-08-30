package com.polaris.auth.infrastructure.security;

import com.polaris.auth.application.out.EnviarVerificacionPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Adaptador de prod: manda el enlace de verificacion por SMTP.
 *
 * <p>Constructor explicito y no @RequiredArgsConstructor por los @Value, igual
 * que LogEnviarVerificacionAdapter.
 */
@Slf4j
@Component
@Profile("prod")
public class SmtpEnviarVerificacionAdapter implements EnviarVerificacionPort {

    private final JavaMailSender mailSender;
    private final String urlBase;
    private final String remitente;

    public SmtpEnviarVerificacionAdapter(
            JavaMailSender mailSender,
            @Value("${polaris.url-base}") String urlBase,
            @Value("${polaris.correo.remitente}") String remitente) {
        this.mailSender = mailSender;
        this.urlBase = urlBase;
        this.remitente = remitente;
    }

    @Override
    public void enviar(String email, String nombre, String tokenVerificacion) {
        String enlace = "%s/api/auth/verificacion?token=%s".formatted(urlBase, tokenVerificacion);

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject("Verifica tu cuenta de Polaris");
        mensaje.setText("""
                Hola %s:

                Para activar tu cuenta de Polaris, abre este enlace:

                %s

                Caduca en 24 horas. Si no has sido tu, ignora este correo.
                """.formatted(nombre, enlace));

        try {
            mailSender.send(mensaje);
        } catch (MailException e) {
            // El usuario ya esta creado: reventar aqui le dejaria una cuenta a
            // medias sin forma de reintentar. Se registra y se sigue; el token
            // se puede reenviar. El correo NO se escribe en el log.
            log.error("No se pudo enviar la verificacion al usuario {}", nombre, e);
        }
    }
}
