package com.polaris.nucleo.domain.service;

import com.polaris.nucleo.application.out.RegistroPesoRepositoryPort;
import com.polaris.nucleo.domain.model.RegistroPeso;
import com.polaris.nucleo.domain.model.RegistroPesoFilter;
import com.polaris.nucleo.domain.model.RegistroPesoNotFoundException;
import com.polaris.shared.error.DuplicateResourceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo que mas importa: un peso por dia y que un usuario nunca vea ni toque el
 * registro de otro. Ver docs/decisiones/010-registro-peso-un-peso-por-dia.md.
 */
@ExtendWith(MockitoExtension.class)
class RegistroPesoServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;
    private static final LocalDate HOY = LocalDate.of(2026, 9, 30);

    @Mock
    private RegistroPesoRepositoryPort repository;

    @InjectMocks
    private RegistroPesoService service;

    private static RegistroPeso registro(Long id, Long usuarioId, LocalDate fecha, String peso) {
        return RegistroPeso.builder().id(id).usuarioId(usuarioId).fecha(fecha).pesoKg(new BigDecimal(peso)).build();
    }

    @Test
    @DisplayName("create fija el usuarioId del JWT y crea sin id si ese dia esta libre")
    void createNuevoFijaUsuarioYSinId() {
        when(repository.findByUsuarioIdAndFecha(USUARIO, HOY)).thenReturn(Optional.empty());
        when(repository.save(any(RegistroPeso.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistroPeso creado = service.create(USUARIO, registro(999L, 999L, HOY, "78.50"));

        assertThat(creado.getId()).isNull();
        assertThat(creado.getUsuarioId()).isEqualTo(USUARIO);
    }

    @Test
    @DisplayName("create sobre un dia ya registrado actualiza ese registro en vez de crear otro")
    void createMismoDiaActualiza() {
        when(repository.findByUsuarioIdAndFecha(USUARIO, HOY))
                .thenReturn(Optional.of(registro(5L, USUARIO, HOY, "78.00")));
        when(repository.save(any(RegistroPeso.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistroPeso guardado = service.create(USUARIO, registro(null, null, HOY, "77.40"));

        assertThat(guardado.getId()).isEqualTo(5L);
        assertThat(guardado.getPesoKg()).isEqualByComparingTo("77.40");
    }

    @Test
    @DisplayName("get devuelve el registro cuando es del usuario")
    void getDevuelveRegistroPropio() {
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, USUARIO, HOY, "78.00")));

        assertThat(service.get(USUARIO, 5L).getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("get lanza RegistroPesoNotFoundException, no 403, si es de otro usuario")
    void getLanzaNotFoundSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, OTRO_USUARIO, HOY, "78.00")));

        assertThatThrownBy(() -> service.get(USUARIO, 5L))
                .isInstanceOf(RegistroPesoNotFoundException.class);
    }

    @Test
    @DisplayName("get lanza RegistroPesoNotFoundException si el id no existe")
    void getLanzaNotFoundSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(USUARIO, 42L))
                .isInstanceOf(RegistroPesoNotFoundException.class);
    }

    @Test
    @DisplayName("list delega usuarioId y filtro tal cual en el repositorio")
    void listDelegaUsuarioYFiltro() {
        RegistroPesoFilter filtro = RegistroPesoFilter.builder().desde(HOY).build();
        List<RegistroPeso> esperado = List.of(registro(1L, USUARIO, HOY, "78.00"));
        when(repository.findAll(USUARIO, filtro)).thenReturn(esperado);

        assertThat(service.list(USUARIO, filtro)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("update conserva id y usuarioId del registro existente")
    void updateConservaIdYUsuarioDelExistente() {
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, USUARIO, HOY, "78.00")));
        when(repository.findByUsuarioIdAndFecha(USUARIO, HOY))
                .thenReturn(Optional.of(registro(5L, USUARIO, HOY, "78.00")));
        when(repository.save(any(RegistroPeso.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistroPeso actualizado = service.update(USUARIO, 5L, registro(999L, 999L, HOY, "77.00"));

        assertThat(actualizado.getId()).isEqualTo(5L);
        assertThat(actualizado.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(actualizado.getPesoKg()).isEqualByComparingTo("77.00");
    }

    @Test
    @DisplayName("update permite mover el registro a una fecha libre")
    void updateMueveAFechaLibre() {
        LocalDate ayer = HOY.minusDays(1);
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, USUARIO, HOY, "78.00")));
        when(repository.findByUsuarioIdAndFecha(USUARIO, ayer)).thenReturn(Optional.empty());
        when(repository.save(any(RegistroPeso.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.update(USUARIO, 5L, registro(null, null, ayer, "78.00")).getFecha()).isEqualTo(ayer);
    }

    @Test
    @DisplayName("update lanza DuplicateResourceException y no guarda si la fecha ya tiene otro registro")
    void updateLanzaSiLaFechaEstaOcupadaPorOtro() {
        LocalDate ayer = HOY.minusDays(1);
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, USUARIO, HOY, "78.00")));
        when(repository.findByUsuarioIdAndFecha(USUARIO, ayer))
                .thenReturn(Optional.of(registro(6L, USUARIO, ayer, "79.00")));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, registro(null, null, ayer, "78.00")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza RegistroPesoNotFoundException y no guarda si es de otro usuario")
    void updateLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, OTRO_USUARIO, HOY, "78.00")));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, registro(null, null, HOY, "77.00")))
                .isInstanceOf(RegistroPesoNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("delete comprueba propiedad antes de borrar")
    void deleteComprobarPropiedadAntesDeBorrar() {
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, USUARIO, HOY, "78.00")));

        service.delete(USUARIO, 5L);

        verify(repository).deleteById(5L);
    }

    @Test
    @DisplayName("delete no borra si es de otro usuario")
    void deleteNoBorraSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, OTRO_USUARIO, HOY, "78.00")));

        assertThatThrownBy(() -> service.delete(USUARIO, 5L))
                .isInstanceOf(RegistroPesoNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
