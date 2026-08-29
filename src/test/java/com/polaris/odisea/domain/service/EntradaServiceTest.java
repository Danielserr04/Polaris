package com.polaris.odisea.domain.service;

import com.polaris.odisea.application.out.EntradaRepositoryPort;
import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.domain.model.EntradaFilter;
import com.polaris.odisea.domain.model.EntradaNotFoundException;
import com.polaris.odisea.domain.model.EstadoEntrada;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo que mas importa aqui no es el CRUD, es que un usuario nunca vea ni toque
 * la entrada de otro. Ver docs/modulos/odisea.md.
 */
@ExtendWith(MockitoExtension.class)
class EntradaServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;

    @Mock
    private EntradaRepositoryPort repository;

    @InjectMocks
    private EntradaService service;

    @Test
    @DisplayName("create fija el usuarioId del JWT, no el que traiga la entrada")
    void createFijaUsuarioIdDelContexto() {
        Entrada entrada = Entrada.builder().tituloId(10L).estado(EstadoEntrada.PENDIENTE).build();
        when(repository.save(any(Entrada.class))).thenAnswer(inv -> inv.getArgument(0));

        Entrada creada = service.create(USUARIO, entrada);

        assertThat(creada.getUsuarioId()).isEqualTo(USUARIO);
    }

    @Test
    @DisplayName("get devuelve la entrada cuando es del usuario")
    void getDevuelveEntradaPropia() {
        Entrada entrada = Entrada.builder().id(5L).usuarioId(USUARIO).build();
        when(repository.findById(5L)).thenReturn(Optional.of(entrada));

        assertThat(service.get(USUARIO, 5L).getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("get lanza EntradaNotFoundException, no 403, si la entrada es de otro usuario")
    void getLanzaNotFoundSiEsDeOtroUsuario() {
        Entrada entrada = Entrada.builder().id(5L).usuarioId(OTRO_USUARIO).build();
        when(repository.findById(5L)).thenReturn(Optional.of(entrada));

        assertThatThrownBy(() -> service.get(USUARIO, 5L))
                .isInstanceOf(EntradaNotFoundException.class);
    }

    @Test
    @DisplayName("get lanza EntradaNotFoundException si el id no existe")
    void getLanzaNotFoundSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(USUARIO, 42L))
                .isInstanceOf(EntradaNotFoundException.class);
    }

    @Test
    @DisplayName("list delega usuarioId y filtro tal cual en el repositorio")
    void listDelegaUsuarioYFiltro() {
        EntradaFilter filtro = EntradaFilter.builder().estado(EstadoEntrada.EN_CURSO).build();
        List<Entrada> esperado = List.of(Entrada.builder().id(1L).build());
        when(repository.findAll(USUARIO, filtro)).thenReturn(esperado);

        assertThat(service.list(USUARIO, filtro)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("update conserva id y usuarioId de la entrada existente")
    void updateConservaIdYUsuarioDeLaExistente() {
        Entrada existente = Entrada.builder().id(5L).usuarioId(USUARIO).estado(EstadoEntrada.PENDIENTE).build();
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Entrada.class))).thenAnswer(inv -> inv.getArgument(0));

        Entrada cambios = Entrada.builder().id(999L).usuarioId(999L).estado(EstadoEntrada.TERMINADO).build();
        Entrada actualizada = service.update(USUARIO, 5L, cambios);

        assertThat(actualizada.getId()).isEqualTo(5L);
        assertThat(actualizada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(actualizada.getEstado()).isEqualTo(EstadoEntrada.TERMINADO);
    }

    @Test
    @DisplayName("update lanza EntradaNotFoundException y no guarda si la entrada es de otro usuario")
    void updateLanzaSiEsDeOtroUsuario() {
        Entrada existente = Entrada.builder().id(5L).usuarioId(OTRO_USUARIO).build();
        when(repository.findById(5L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, Entrada.builder().build()))
                .isInstanceOf(EntradaNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("delete comprueba propiedad antes de borrar")
    void deleteComprueboPropiedadAntesDeBorrar() {
        when(repository.findById(5L)).thenReturn(Optional.of(Entrada.builder().id(5L).usuarioId(USUARIO).build()));

        service.delete(USUARIO, 5L);

        verify(repository).deleteById(5L);
    }

    @Test
    @DisplayName("delete lanza EntradaNotFoundException y no borra si la entrada es de otro usuario")
    void deleteLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(Entrada.builder().id(5L).usuarioId(OTRO_USUARIO).build()));

        assertThatThrownBy(() -> service.delete(USUARIO, 5L))
                .isInstanceOf(EntradaNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
