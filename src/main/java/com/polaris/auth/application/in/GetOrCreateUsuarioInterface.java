package com.polaris.auth.application.in;

import com.polaris.auth.domain.model.PerfilGoogle;
import com.polaris.auth.domain.model.Usuario;

/**
 * Alta implicita en el primer login: si el googleId ya existe se devuelve ese
 * usuario, y si no se crea. No hay registro manual en Polaris.
 */
public interface GetOrCreateUsuarioInterface {
    Usuario getOrCreate(PerfilGoogle perfil);
}
