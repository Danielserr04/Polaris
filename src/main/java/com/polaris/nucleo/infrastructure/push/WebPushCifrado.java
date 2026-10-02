package com.polaris.nucleo.infrastructure.push;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * Web Push con lo que trae el JDK, sin librerias: cifrado del mensaje
 * (RFC 8291, aes128gcm) y firma VAPID (RFC 8292, JWT ES256). Todo con claves
 * P-256: las publicas en formato sin comprimir (65 bytes, 0x04 || x || y) y
 * las privadas como el escalar d (32 bytes), siempre en base64url sin relleno.
 * Ver docs/decisiones/044-recordatorios.md.
 */
public final class WebPushCifrado {

    private static final SecureRandom AZAR = new SecureRandom();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder D64 = Base64.getUrlDecoder();
    /** Tamano de registro que se anuncia en la cabecera. Un aviso cabe de sobra en uno. */
    private static final int TAMANO_REGISTRO = 4096;
    private static final ECParameterSpec P256 = parametrosP256();

    private WebPushCifrado() {
    }

    /** Par de claves VAPID nuevo: [publica, privada] en base64url. */
    public static String[] generarClaves() {
        KeyPair par = generarPar();
        return new String[]{
                B64.encodeToString(publicaABytes((ECPublicKey) par.getPublic())),
                B64.encodeToString(privadaABytes((ECPrivateKey) par.getPrivate()))
        };
    }

    /**
     * Cifra {@code mensaje} para el dispositivo (p256dh y auth de su
     * suscripcion). Devuelve el cuerpo listo para el POST: cabecera aes128gcm
     * (salt, tamano de registro, clave publica efimera) y texto cifrado.
     */
    public static byte[] cifrar(byte[] mensaje, String p256dh, String auth) {
        try {
            byte[] uaPublica = D64.decode(p256dh);
            byte[] secretoAuth = D64.decode(auth);
            KeyPair efimero = generarPar();
            byte[] asPublica = publicaABytes((ECPublicKey) efimero.getPublic());

            KeyAgreement ecdh = KeyAgreement.getInstance("ECDH");
            ecdh.init(efimero.getPrivate());
            ecdh.doPhase(publicaDeBytes(uaPublica), true);
            byte[] secretoEcdh = ecdh.generateSecret();

            byte[] infoClave = concatenar("WebPush: info\0".getBytes(StandardCharsets.US_ASCII), uaPublica, asPublica);
            byte[] ikm = hkdf(secretoAuth, secretoEcdh, infoClave, 32);

            byte[] salt = new byte[16];
            AZAR.nextBytes(salt);
            byte[] cek = hkdf(salt, ikm, "Content-Encoding: aes128gcm\0".getBytes(StandardCharsets.US_ASCII), 16);
            byte[] nonce = hkdf(salt, ikm, "Content-Encoding: nonce\0".getBytes(StandardCharsets.US_ASCII), 12);

            // Un solo registro, que es el ultimo: el delimitador es 0x02 y no hay relleno.
            Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
            aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(cek, "AES"), new GCMParameterSpec(128, nonce));
            byte[] cifrado = aes.doFinal(concatenar(mensaje, new byte[]{2}));

            ByteBuffer cabecera = ByteBuffer.allocate(16 + 4 + 1 + asPublica.length);
            cabecera.put(salt).putInt(TAMANO_REGISTRO).put((byte) asPublica.length).put(asPublica);
            return concatenar(cabecera.array(), cifrado);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("No se ha podido cifrar el aviso: " + e.getMessage(), e);
        }
    }

    /**
     * Cabecera Authorization VAPID: {@code vapid t=<jwt>, k=<clave publica>}.
     * {@code audiencia} es el origen del endpoint (https://fcm.googleapis.com).
     */
    public static String autorizacion(String audiencia, String contacto, long expiraEnSegundos,
                                      String publica, String privada) {
        try {
            String cabecera = B64.encodeToString("{\"typ\":\"JWT\",\"alg\":\"ES256\"}".getBytes(StandardCharsets.UTF_8));
            String cuerpo = B64.encodeToString(("{\"aud\":\"" + audiencia + "\",\"exp\":" + expiraEnSegundos
                    + ",\"sub\":\"" + contacto + "\"}").getBytes(StandardCharsets.UTF_8));
            String firmable = cabecera + "." + cuerpo;
            // P1363: r || s de 32 bytes cada uno, que es lo que pide JWS (no DER).
            Signature firma = Signature.getInstance("SHA256withECDSAinP1363Format");
            firma.initSign(privadaDeBytes(D64.decode(privada)));
            firma.update(firmable.getBytes(StandardCharsets.US_ASCII));
            return "vapid t=" + firmable + "." + B64.encodeToString(firma.sign()) + ", k=" + publica;
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("No se ha podido firmar el aviso: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------ claves

    static KeyPair generarPar() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
            gen.initialize(new ECGenParameterSpec("secp256r1"), AZAR);
            return gen.generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("P-256 no disponible", e);
        }
    }

    static byte[] publicaABytes(ECPublicKey clave) {
        return concatenar(new byte[]{4}, a32(clave.getW().getAffineX()), a32(clave.getW().getAffineY()));
    }

    static byte[] privadaABytes(ECPrivateKey clave) {
        return a32(clave.getS());
    }

    static ECPublicKey publicaDeBytes(byte[] bytes) throws GeneralSecurityException {
        if (bytes.length != 65 || bytes[0] != 4) {
            throw new GeneralSecurityException("Clave publica P-256 sin comprimir no valida");
        }
        ECPoint punto = new ECPoint(new BigInteger(1, Arrays.copyOfRange(bytes, 1, 33)),
                new BigInteger(1, Arrays.copyOfRange(bytes, 33, 65)));
        return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(punto, P256));
    }

    static ECPrivateKey privadaDeBytes(byte[] bytes) throws GeneralSecurityException {
        return (ECPrivateKey) KeyFactory.getInstance("EC")
                .generatePrivate(new ECPrivateKeySpec(new BigInteger(1, bytes), P256));
    }

    // ------------------------------------------------------------ utilidades

    /** HKDF-SHA256 (extract + expand) para salidas de hasta 32 bytes. */
    static byte[] hkdf(byte[] salt, byte[] ikm, byte[] info, int longitud) throws GeneralSecurityException {
        byte[] prk = hmac(salt, ikm);
        return Arrays.copyOf(hmac(prk, concatenar(info, new byte[]{1})), longitud);
    }

    private static byte[] hmac(byte[] clave, byte[] datos) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(clave, "HmacSHA256"));
        return mac.doFinal(datos);
    }

    /** Entero positivo a exactamente 32 bytes big-endian (sin el 0 de signo, con ceros delante). */
    private static byte[] a32(BigInteger n) {
        byte[] b = n.toByteArray();
        if (b.length == 32) {
            return b;
        }
        byte[] r = new byte[32];
        int copiar = Math.min(b.length, 32);
        System.arraycopy(b, b.length - copiar, r, 32 - copiar, copiar);
        return r;
    }

    static byte[] concatenar(byte[]... partes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] p : partes) {
            out.writeBytes(p);
        }
        return out.toByteArray();
    }

    private static ECParameterSpec parametrosP256() {
        try {
            AlgorithmParameters p = AlgorithmParameters.getInstance("EC");
            p.init(new ECGenParameterSpec("secp256r1"));
            return p.getParameterSpec(ECParameterSpec.class);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("P-256 no disponible", e);
        }
    }
}
