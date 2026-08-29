package com.polaris.auth.application.in;

import com.polaris.auth.domain.model.Usuario;

public interface LoginInterface {
    Usuario login(String usernameOEmail, String password);
}
