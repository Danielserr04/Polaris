package com.polaris.auth.application.in;

import com.polaris.auth.domain.model.Usuario;

public interface GetUsuarioInterface {
    Usuario get(Long id);
}
