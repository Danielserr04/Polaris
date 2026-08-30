package com.polaris.auth.application.in;

import com.polaris.auth.domain.model.Usuario;

public interface ActualizarPerfilInterface {
    Usuario actualizarPerfil(Long usuarioId, String nombre, String avatarUrl);
}
