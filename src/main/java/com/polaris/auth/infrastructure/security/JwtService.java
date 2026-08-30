package com.polaris.auth.infrastructure.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Firma y valida los JWT propios de Polaris con Nimbus, que ya viene con
 * spring-boot-starter-oauth2-client. Ver docs/modulos/auth.md.
 *
 * <p>Dos propositos distintos comparten la misma firma: token de sesion (el
 * que autentica cada peticion) y token de verificacion de email (el del
 * enlace que se manda al registrarse). El claim "proposito" los separa, para
 * que un enlace de verificacion filtrado no sirva para autenticar una sesion.
 */
@Slf4j
@Component
public class JwtService {

    /** HS256 necesita al menos 256 bits de secreto. */
    private static final int LONGITUD_MINIMA_SECRETO = 32;

    private static final String CLAIM_PROPOSITO = "proposito";
    private static final String PROPOSITO_SESION = "sesion";
    private static final String PROPOSITO_VERIFICACION = "verificacion";

    /** El enlace de verificacion vive 24 horas, sin importar la expiracion de la sesion. */
    private static final long EXPIRACION_VERIFICACION_SEGUNDOS = 24 * 60 * 60;

    private final byte[] secreto;
    private final long expiracionSegundos;
    private final String emisor;

    public JwtService(@Value("${polaris.jwt.secreto}") String secreto,
                      @Value("${polaris.jwt.expiracion-segundos}") long expiracionSegundos,
                      @Value("${polaris.jwt.emisor}") String emisor) {

        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < LONGITUD_MINIMA_SECRETO) {
            throw new IllegalStateException(
                    "POLARIS_JWT_SECRETO debe tener al menos " + LONGITUD_MINIMA_SECRETO + " caracteres");
        }

        this.secreto = bytes;
        this.expiracionSegundos = expiracionSegundos;
        this.emisor = emisor;
    }

    public long getExpiracionSegundos() {
        return expiracionSegundos;
    }

    public String generar(Long usuarioId) {
        return generarToken(usuarioId, PROPOSITO_SESION, expiracionSegundos);
    }

    public String generarVerificacion(Long usuarioId) {
        return generarToken(usuarioId, PROPOSITO_VERIFICACION, EXPIRACION_VERIFICACION_SEGUNDOS);
    }

    /**
     * @return el usuarioId si el token es de sesion, esta firmado por nosotros
     *         y no ha caducado; vacio en cualquier otro caso, incluido un
     *         token de verificacion
     */
    public Optional<Long> validarYExtraerUsuarioId(String token) {
        return validar(token, PROPOSITO_SESION);
    }

    /**
     * @return el usuarioId si el token es de verificacion, esta firmado por
     *         nosotros y no ha caducado; vacio en cualquier otro caso
     */
    public Optional<Long> validarTokenVerificacion(String token) {
        return validar(token, PROPOSITO_VERIFICACION);
    }

    private String generarToken(Long usuarioId, String proposito, long expiracionSegundosDelToken) {
        Instant ahora = Instant.now();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(usuarioId))
                .issuer(emisor)
                .claim(CLAIM_PROPOSITO, proposito)
                .issueTime(Date.from(ahora))
                .expirationTime(Date.from(ahora.plusSeconds(expiracionSegundosDelToken)))
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);

        try {
            jwt.sign(new MACSigner(secreto));
        } catch (JOSEException ex) {
            throw new IllegalStateException("No se pudo firmar el JWT", ex);
        }

        return jwt.serialize();
    }

    private Optional<Long> validar(String token, String propositoEsperado) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!jwt.verify(new MACVerifier(secreto))) {
                return Optional.empty();
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            if (claims.getExpirationTime() == null || claims.getExpirationTime().toInstant().isBefore(Instant.now())) {
                return Optional.empty();
            }

            if (!emisor.equals(claims.getIssuer())) {
                return Optional.empty();
            }

            if (!propositoEsperado.equals(claims.getStringClaim(CLAIM_PROPOSITO))) {
                return Optional.empty();
            }

            return Optional.of(Long.valueOf(claims.getSubject()));

        } catch (ParseException | JOSEException | NumberFormatException ex) {
            log.debug("JWT rechazado: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
