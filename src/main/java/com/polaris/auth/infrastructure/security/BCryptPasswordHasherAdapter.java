package com.polaris.auth.infrastructure.security;

import com.polaris.auth.application.out.PasswordHasherPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCryptPasswordEncoder es sin estado y seguro para hilos, asi que basta una
 * instancia propia sin exponerla como bean de Spring.
 */
@Component
public class BCryptPasswordHasherAdapter implements PasswordHasherPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String contrasenaEnClaro) {
        return encoder.encode(contrasenaEnClaro);
    }

    @Override
    public boolean coincide(String contrasenaEnClaro, String hash) {
        return encoder.matches(contrasenaEnClaro, hash);
    }
}
