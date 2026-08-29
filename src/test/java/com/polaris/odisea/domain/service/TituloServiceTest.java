package com.polaris.odisea.domain.service;

import com.polaris.odisea.application.out.TituloRepositoryPort;
import com.polaris.odisea.domain.model.TipoContenido;
import com.polaris.odisea.domain.model.Titulo;
import com.polaris.odisea.domain.model.TituloFilter;
import com.polaris.odisea.domain.model.TituloNotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Servicio de dominio con el puerto mockeado, prioridad 1 de docs/convenciones.md.
 */
@ExtendWith(MockitoExtension.class)
class TituloServiceTest {

    @Mock
    private TituloRepositoryPort repository;

    @InjectMocks
    private TituloService service;

    @Test
    @DisplayName("create delega en el repositorio y devuelve lo guardado")
    void createDelegaEnRepositorio() {
        Titulo aGuardar = Titulo.builder().tipo(TipoContenido.PELICULA).titulo("Sin titulo").build();
        Titulo guardado = Titulo.builder().id(1L).tipo(TipoContenido.PELICULA).titulo("Sin titulo").build();
        when(repository.save(aGuardar)).thenReturn(guardado);

        assertThat(service.create(aGuardar).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("get devuelve el titulo cuando existe")
    void getDevuelveTituloExistente() {
        Titulo titulo = Titulo.builder().id(1L).titulo("Interstellar").build();
        when(repository.findById(1L)).thenReturn(Optional.of(titulo));

        assertThat(service.get(1L).getTitulo()).isEqualTo("Interstellar");
    }

    @Test
    @DisplayName("get lanza TituloNotFoundException cuando no existe")
    void getLanzaCuandoNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L))
                .isInstanceOf(TituloNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    @DisplayName("list delega el filtro tal cual en el repositorio")
    void listDelegaFiltro() {
        TituloFilter filtro = TituloFilter.builder().tipo(TipoContenido.SERIE).build();
        List<Titulo> esperado = List.of(Titulo.builder().id(1L).build());
        when(repository.findAll(filtro)).thenReturn(esperado);

        assertThat(service.list(filtro)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("update conserva el id de la ruta, no el que traiga el body")
    void updateConservaIdDeLaRuta() {
        Titulo existente = Titulo.builder().id(5L).titulo("Version vieja").build();
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Titulo.class))).thenAnswer(inv -> inv.getArgument(0));

        Titulo cambios = Titulo.builder().id(999L).titulo("Version nueva").build();
        Titulo actualizado = service.update(5L, cambios);

        assertThat(actualizado.getId()).isEqualTo(5L);
        assertThat(actualizado.getTitulo()).isEqualTo("Version nueva");
    }

    @Test
    @DisplayName("update lanza TituloNotFoundException si el id de la ruta no existe")
    void updateLanzaSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(42L, Titulo.builder().build()))
                .isInstanceOf(TituloNotFoundException.class);

        verify(repository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    @DisplayName("delete comprueba que existe antes de borrar")
    void deleteComprueboExistenciaAntesDeBorrar() {
        when(repository.findById(1L)).thenReturn(Optional.of(Titulo.builder().id(1L).build()));

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("delete lanza TituloNotFoundException y no borra si no existe")
    void deleteLanzaSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(42L))
                .isInstanceOf(TituloNotFoundException.class);

        verify(repository, org.mockito.Mockito.never()).deleteById(any());
    }
}
