package com.polaris.nucleo.domain.service;

import com.polaris.nucleo.application.out.PerfilRepositoryPort;
import com.polaris.nucleo.domain.model.NivelActividad;
import com.polaris.nucleo.domain.model.Perfil;
import com.polaris.nucleo.domain.model.PerfilNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Lo que importa: un perfil por usuario, siempre el del JWT, y que el PUT
 * actualice el existente en vez de crear otro. Ver
 * docs/decisiones/009-perfil-unico-por-usuario.md.
 */
@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    private static final Long USUARIO = 1L;

    @Mock
    private PerfilRepositoryPort repository;

    @InjectMocks
    private PerfilService service;

    @Test
    @DisplayName("get devuelve el perfil del usuario")
    void getDevuelvePerfilDelUsuario() {
        Perfil perfil = Perfil.builder().id(5L).usuarioId(USUARIO).alturaCm(180).build();
        when(repository.findByUsuarioId(USUARIO)).thenReturn(Optional.of(perfil));

        assertThat(service.get(USUARIO).getAlturaCm()).isEqualTo(180);
    }

    @Test
    @DisplayName("get lanza PerfilNotFoundException si el usuario aun no tiene perfil")
    void getLanzaNotFoundSiNoHayPerfil() {
        when(repository.findByUsuarioId(USUARIO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(USUARIO))
                .isInstanceOf(PerfilNotFoundException.class);
    }

    @Test
    @DisplayName("update crea el perfil si no existe: sin id y con el usuarioId del JWT")
    void updateCreaSiNoExiste() {
        when(repository.findByUsuarioId(USUARIO)).thenReturn(Optional.empty());
        when(repository.save(any(Perfil.class))).thenAnswer(inv -> inv.getArgument(0));

        Perfil cambios = Perfil.builder().id(999L).usuarioId(999L).alturaCm(175).build();
        Perfil guardado = service.update(USUARIO, cambios);

        assertThat(guardado.getId()).isNull();
        assertThat(guardado.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(guardado.getAlturaCm()).isEqualTo(175);
    }

    @Test
    @DisplayName("update conserva el id del perfil existente para no insertar otro")
    void updateConservaIdDelExistente() {
        Perfil existente = Perfil.builder().id(5L).usuarioId(USUARIO).alturaCm(180).build();
        when(repository.findByUsuarioId(USUARIO)).thenReturn(Optional.of(existente));
        when(repository.save(any(Perfil.class))).thenAnswer(inv -> inv.getArgument(0));

        Perfil cambios = Perfil.builder().id(999L).usuarioId(999L)
                .nivelActividad(NivelActividad.ALTO).build();
        Perfil guardado = service.update(USUARIO, cambios);

        assertThat(guardado.getId()).isEqualTo(5L);
        assertThat(guardado.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(guardado.getNivelActividad()).isEqualTo(NivelActividad.ALTO);
    }
}
