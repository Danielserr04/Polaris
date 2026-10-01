package com.polaris.auth.infrastructure.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.polaris.auth.application.in.GetOrCreateUsuarioInterface;
import com.polaris.auth.domain.model.Usuario;
import com.polaris.shared.testing.CapturaLogs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Lo que auth escribe en el log (B8, ADR 029): nada de emails ni tokens salvo en
 * el adaptador de dev, y niveles coherentes en el login con Google.
 */
class LogsDeAuthTest {

    private static final String EMAIL = "persona.privada@example.com";

    @Test
    @DisplayName("login correcto: se loguea el id del usuario, no su email")
    void elLoginCorrectoNoLogueaElEmail() throws Exception {
        GetOrCreateUsuarioInterface getOrCreate = mock(GetOrCreateUsuarioInterface.class);
        when(getOrCreate.getOrCreate(any())).thenReturn(Usuario.builder().id(42L).email(EMAIL).build());
        JwtService jwt = new JwtService("secreto-de-tests-que-no-vale-para-nada-fuera-de-aqui-0123456789", 3600, "polaris");
        OAuth2LoginSuccessHandler manejador = new OAuth2LoginSuccessHandler(getOrCreate, jwt, "http://localhost:5173");
        OAuth2User principal = mock(OAuth2User.class);
        Authentication autenticacion = mock(Authentication.class);
        when(autenticacion.getPrincipal()).thenReturn(principal);

        try (CapturaLogs logs = CapturaLogs.de(OAuth2LoginSuccessHandler.class)) {
            manejador.onAuthenticationSuccess(new MockHttpServletRequest(), new MockHttpServletResponse(), autenticacion);

            assertThat(logs.eventos(Level.INFO)).hasSize(1);
            assertThat(logs.texto()).contains("42").doesNotContain(EMAIL).doesNotContain("eyJ");
        }
    }

    private ILoggingEvent fallarLogin(org.springframework.security.core.AuthenticationException error,
                                      CapturaLogs logs) throws Exception {
        new OAuth2LoginFailureHandler("http://localhost:5173").onAuthenticationFailure(
                new MockHttpServletRequest("GET", "/login/oauth2/code/google"), new MockHttpServletResponse(), error);
        assertThat(logs.eventos()).hasSize(1);
        return logs.eventos().get(0);
    }

    @Test
    @DisplayName("el usuario cancela en Google (access_denied): WARN sin stacktrace")
    void cancelarEnGoogleEsUnWarnSinStacktrace() throws Exception {
        try (CapturaLogs logs = CapturaLogs.de(OAuth2LoginFailureHandler.class)) {
            ILoggingEvent evento = fallarLogin(new OAuth2AuthenticationException(new OAuth2Error("access_denied")), logs);

            assertThat(evento.getLevel()).isEqualTo(Level.WARN);
            assertThat(evento.getThrowableProxy()).isNull();
        }
    }

    @Test
    @DisplayName("fallo de configuracion (invalid_client): ERROR con stacktrace")
    void fallarPorConfiguracionEsUnErrorConStacktrace() throws Exception {
        try (CapturaLogs logs = CapturaLogs.de(OAuth2LoginFailureHandler.class)) {
            ILoggingEvent evento = fallarLogin(new OAuth2AuthenticationException(new OAuth2Error("invalid_client")), logs);

            assertThat(evento.getLevel()).isEqualTo(Level.ERROR);
            assertThat(evento.getThrowableProxy()).isNotNull();
        }
    }

    @Test
    @DisplayName("una excepcion de autenticacion que no es OAuth2: ERROR con stacktrace")
    void otraExcepcionDeAutenticacionEsUnError() throws Exception {
        try (CapturaLogs logs = CapturaLogs.de(OAuth2LoginFailureHandler.class)) {
            ILoggingEvent evento = fallarLogin(new BadCredentialsException("x"), logs);

            assertThat(evento.getLevel()).isEqualTo(Level.ERROR);
            assertThat(evento.getThrowableProxy()).isNotNull();
        }
    }

    @Test
    @DisplayName("si SMTP falla, el log no lleva ni el email ni el token ni el mensaje de la excepcion")
    void elFalloDeSmtpNoFiltraElEmailNiElToken() {
        JavaMailSender correo = mock(JavaMailSender.class);
        // Los servidores SMTP devuelven la direccion en el rechazo: viaja en el mensaje de la excepcion.
        doThrow(new MailSendException("550 5.1.1 <" + EMAIL + ">: Recipient address rejected"))
                .when(correo).send(any(org.springframework.mail.SimpleMailMessage.class));
        SmtpEnviarVerificacionAdapter adaptador =
                new SmtpEnviarVerificacionAdapter(correo, "https://polaris.example", "no-reply@polaris.example");

        try (CapturaLogs logs = CapturaLogs.de(SmtpEnviarVerificacionAdapter.class)) {
            adaptador.enviar(EMAIL, "Ana", "TOKEN-SECRETO-DE-UN-SOLO-USO");

            assertThat(logs.eventos(Level.ERROR)).hasSize(1);
            assertThat(logs.texto())
                    .contains("Ana")
                    .contains("MailSendException")
                    .doesNotContain(EMAIL)
                    .doesNotContain("TOKEN-SECRETO-DE-UN-SOLO-USO");
        }
    }

    @Test
    @DisplayName("el adaptador que loguea el token y el email solo existe en el perfil dev")
    void elAdaptadorDeLogSoloEsDeDev() {
        Profile perfil = LogEnviarVerificacionAdapter.class.getAnnotation(Profile.class);

        assertThat(perfil).isNotNull();
        assertThat(perfil.value()).containsExactly("dev");
        assertThat(SmtpEnviarVerificacionAdapter.class.getAnnotation(Profile.class).value()).containsExactly("prod");
    }

    @Test
    @DisplayName("JwtService: un token invalido se rechaza sin volcar el token al log")
    void unJwtInvalidoNoSeVuelcaAlLog() {
        JwtService jwt = new JwtService("secreto-de-tests-que-no-vale-para-nada-fuera-de-aqui-0123456789", 3600, "polaris");
        String falso = "aaaa.bbbb.cccc-DATO-QUE-NO-DEBE-LOGUEARSE";

        try (CapturaLogs logs = CapturaLogs.de(JwtService.class)) {
            assertThat(jwt.validarYExtraerUsuarioId(falso)).isEmpty();

            assertThat(logs.texto()).doesNotContain("DATO-QUE-NO-DEBE-LOGUEARSE");
        }
    }
}
