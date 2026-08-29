package com.polaris.auth.application.in;

import com.polaris.auth.domain.model.Usuario;

public interface RegistrarUsuarioInterface {
    Usuario registrar(String username, String email, String password);
}
