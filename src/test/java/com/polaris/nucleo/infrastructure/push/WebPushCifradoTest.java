package com.polaris.nucleo.infrastructure.push;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Se descifra como lo haria el navegador (RFC 8291) y se verifica la firma
 * VAPID como lo haria el servicio de push (RFC 8292). Si esto pasa, el
 * formato es el bueno; que el servicio real lo acepte solo se prueba con un
 * movil de verdad.
 */
class WebPushCifradoTest {

    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder D64 = Base64.getUrlDecoder();

    @Test
    @DisplayName("el mensaje cifrado se descifra con las claves del dispositivo")
    void cifradoDescifrable() throws Exception {
        KeyPair navegador = WebPushCifrado.generarPar();
        byte[] uaPublica = WebPushCifrado.publicaABytes((ECPublicKey) navegador.getPublic());
        byte[] auth = new byte[16];
        new SecureRandom().nextBytes(auth);
        byte[] mensaje = "{\"titulo\":\"Apunta tus comidas\"}".getBytes(StandardCharsets.UTF_8);

        byte[] cuerpo = WebPushCifrado.cifrar(mensaje, B64.encodeToString(uaPublica), B64.encodeToString(auth));

        ByteBuffer b = ByteBuffer.wrap(cuerpo);
        byte[] salt = new byte[16];
        b.get(salt);
        assertThat(b.getInt()).isEqualTo(4096);
        int idlen = b.get();
        assertThat(idlen).isEqualTo(65);
        byte[] asPublica = new byte[idlen];
        b.get(asPublica);
        byte[] cifrado = Arrays.copyOfRange(cuerpo, b.position(), cuerpo.length);

        KeyAgreement ecdh = KeyAgreement.getInstance("ECDH");
        ecdh.init(navegador.getPrivate());
        ecdh.doPhase(WebPushCifrado.publicaDeBytes(asPublica), true);
        byte[] info = WebPushCifrado.concatenar("WebPush: info\0".getBytes(StandardCharsets.US_ASCII), uaPublica, asPublica);
        byte[] ikm = WebPushCifrado.hkdf(auth, ecdh.generateSecret(), info, 32);
        byte[] cek = WebPushCifrado.hkdf(salt, ikm, "Content-Encoding: aes128gcm\0".getBytes(StandardCharsets.US_ASCII), 16);
        byte[] nonce = WebPushCifrado.hkdf(salt, ikm, "Content-Encoding: nonce\0".getBytes(StandardCharsets.US_ASCII), 12);

        Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
        aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(cek, "AES"), new GCMParameterSpec(128, nonce));
        byte[] claro = aes.doFinal(cifrado);

        assertThat(claro[claro.length - 1]).isEqualTo((byte) 2);
        assertThat(Arrays.copyOf(claro, claro.length - 1)).isEqualTo(mensaje);
    }

    @Test
    @DisplayName("la cabecera VAPID lleva un JWT ES256 valido con la audiencia y la clave publica")
    void autorizacionVapid() throws Exception {
        String[] claves = WebPushCifrado.generarClaves();
        assertThat(D64.decode(claves[0])).hasSize(65);
        assertThat(D64.decode(claves[1])).hasSize(32);

        String cabecera = WebPushCifrado.autorizacion("https://fcm.googleapis.com", "mailto:yo@example.com",
                1_900_000_000L, claves[0], claves[1]);

        assertThat(cabecera).startsWith("vapid t=").endsWith(", k=" + claves[0]);
        String jwt = cabecera.substring("vapid t=".length(), cabecera.indexOf(", k="));
        String[] partes = jwt.split("\\.");
        assertThat(partes).hasSize(3);
        String cuerpo = new String(D64.decode(partes[1]), StandardCharsets.UTF_8);
        assertThat(cuerpo).contains("\"aud\":\"https://fcm.googleapis.com\"", "\"exp\":1900000000",
                "\"sub\":\"mailto:yo@example.com\"");

        Signature verificar = Signature.getInstance("SHA256withECDSAinP1363Format");
        verificar.initVerify(WebPushCifrado.publicaDeBytes(D64.decode(claves[0])));
        verificar.update((partes[0] + "." + partes[1]).getBytes(StandardCharsets.US_ASCII));
        assertThat(verificar.verify(D64.decode(partes[2]))).isTrue();
    }
}
