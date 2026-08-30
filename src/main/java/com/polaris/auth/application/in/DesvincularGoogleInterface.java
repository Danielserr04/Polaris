package com.polaris.auth.application.in;

import com.polaris.auth.domain.model.Usuario;

public interface DesvincularGoogleInterface {
    Usuario desvincularGoogle(Long usuarioId);
}
