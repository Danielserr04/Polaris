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
 * Firma y valida el JWT propio de Polaris con Nimbus, que ya viene con
 * spring-boot-starter-oauth2-client. Ver docs/modulos/auth.md.
 *
 * <p>El token de Google se usa una sola vez, en el login. A partir de ahi manda
 * este.
 */
@Slf4j
@Component
public class JwtService {

    /** HS256 necesita al menos 256 bits de secreto. */
    private static final int LONGITUD_MINIMA_SECRETO = 32;

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
        Instant ahora = Instant.now();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(usuarioId))
                .issuer(emisor)
                .issueTime(Date.from(ahora))
                .expirationTime(Date.from(ahora.plusSeconds(expiracionSegundos)))
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);

        try {
            jwt.sign(new MACSigner(secreto));
        } catch (JOSEException ex) {
            throw new IllegalStateException("No se pudo firmar el JWT", ex);
        }

        return jwt.serialize();
    }

    /**
     * @return el usuarioId si el token esta firmado por nosotros y no ha caducado,
     *         vacio en cualquier otro caso
     */
    public Optional<Long> validarYExtraerUsuarioId(String token) {
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

            return Optional.of(Long.valueOf(claims.getSubject()));

        } catch (ParseException | JOSEException | NumberFormatException ex) {
            log.debug("JWT rechazado: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
