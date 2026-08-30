package com.polaris.auth.application.in;

import com.polaris.auth.domain.model.Usuario;

public interface CambiarEmailInterface {
    Usuario cambiarEmail(Long usuarioId, String emailNuevo, String password);
}
