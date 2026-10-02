package com.polaris.nucleo.domain.service;

import com.polaris.nucleo.application.out.MedidaCorporalRepositoryPort;
import com.polaris.nucleo.domain.model.MedidaCorporal;
import com.polaris.nucleo.domain.model.MedidaCorporalFilter;
import com.polaris.nucleo.domain.model.MedidaCorporalNotFoundException;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
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
 * Lo que mas importa: una medicion por dia, al menos una medida, y que un usuario nunca vea ni toque el
 * registro de otro. Ver docs/decisiones/041-medida-corporal-una-por-dia.md.
 */
@ExtendWith(MockitoExtension.class)
class MedidaCorporalServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;
    private static final LocalDate HOY = LocalDate.of(2026, 9, 30);

    @Mock
    private MedidaCorporalRepositoryPort repository;

    @InjectMocks
    private MedidaCorporalService service;

    private static MedidaCorporal registro(Long id, Long usuarioId, LocalDate fecha, String cintura) {
        return MedidaCorporal.builder().id(id).usuarioId(usuarioId).fecha(fecha).cinturaCm(new BigDecimal(cintura)).build();
    }

    @Test
    @DisplayName("create fija el usuarioId del JWT y crea sin id si ese dia esta libre")
    void createNuevoFijaUsuarioYSinId() {
        when(repository.findByUsuarioIdAndFecha(USUARIO, HOY)).thenReturn(Optional.empty());
        when(repository.save(any(MedidaCorporal.class))).thenAnswer(inv -> inv.getArgument(0));

        MedidaCorporal creado = service.create(USUARIO, registro(999L, 999L, HOY, "82.5"));

        assertThat(creado.getId()).isNull();
        assertThat(creado.getUsuarioId()).isEqualTo(USUARIO);
    }

    @Test
    @DisplayName("create sobre un dia ya registrado actualiza ese registro en vez de crear otro")
    void createMismoDiaActualiza() {
        when(repository.findByUsuarioIdAndFecha(USUARIO, HOY))
                .thenReturn(Optional.of(registro(5L, USUARIO, HOY, "83.0")));
        when(repository.save(any(MedidaCorporal.class))).thenAnswer(inv -> inv.getArgument(0));

        MedidaCorporal guardado = service.create(USUARIO, registro(null, null, HOY, "81.4"));

        assertThat(guardado.getId()).isEqualTo(5L);
        assertThat(guardado.getCinturaCm()).isEqualByComparingTo("81.4");
    }

    @Test
    @DisplayName("create sin ninguna medida lanza ValidationException y no guarda")
    void createSinMedidasLanza() {
        MedidaCorporal vacio = MedidaCorporal.builder().fecha(HOY).notas("solo notas").build();

        assertThatThrownBy(() -> service.create(USUARIO, vacio))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Indica al menos una medida");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update sin ninguna medida lanza ValidationException y no guarda")
    void updateSinMedidasLanza() {
        MedidaCorporal vacio = MedidaCorporal.builder().fecha(HOY).build();

        assertThatThrownBy(() -> service.update(USUARIO, 5L, vacio))
                .isInstanceOf(ValidationException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("get devuelve el registro cuando es del usuario")
    void getDevuelveRegistroPropio() {
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, USUARIO, HOY, "83.0")));

        assertThat(service.get(USUARIO, 5L).getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("get lanza MedidaCorporalNotFoundException, no 403, si es de otro usuario")
    void getLanzaNotFoundSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, OTRO_USUARIO, HOY, "83.0")));

        assertThatThrownBy(() -> service.get(USUARIO, 5L))
                .isInstanceOf(MedidaCorporalNotFoundException.class);
    }

    @Test
    @DisplayName("get lanza MedidaCorporalNotFoundException si el id no existe")
    void getLanzaNotFoundSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(USUARIO, 42L))
                .isInstanceOf(MedidaCorporalNotFoundException.class);
    }

    @Test
    @DisplayName("list delega usuarioId y filtro tal cual en el repositorio")
    void listDelegaUsuarioYFiltro() {
        MedidaCorporalFilter filtro = MedidaCorporalFilter.builder().desde(HOY).build();
        List<MedidaCorporal> esperado = List.of(registro(1L, USUARIO, HOY, "83.0"));
        when(repository.findAll(USUARIO, filtro)).thenReturn(esperado);

        assertThat(service.list(USUARIO, filtro)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("update conserva id y usuarioId del registro existente")
    void updateConservaIdYUsuarioDelExistente() {
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, USUARIO, HOY, "83.0")));
        when(repository.findByUsuarioIdAndFecha(USUARIO, HOY))
                .thenReturn(Optional.of(registro(5L, USUARIO, HOY, "83.0")));
        when(repository.save(any(MedidaCorporal.class))).thenAnswer(inv -> inv.getArgument(0));

        MedidaCorporal actualizado = service.update(USUARIO, 5L, registro(999L, 999L, HOY, "81.0"));

        assertThat(actualizado.getId()).isEqualTo(5L);
        assertThat(actualizado.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(actualizado.getCinturaCm()).isEqualByComparingTo("81.0");
    }

    @Test
    @DisplayName("update permite mover el registro a una fecha libre")
    void updateMueveAFechaLibre() {
        LocalDate ayer = HOY.minusDays(1);
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, USUARIO, HOY, "83.0")));
        when(repository.findByUsuarioIdAndFecha(USUARIO, ayer)).thenReturn(Optional.empty());
        when(repository.save(any(MedidaCorporal.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.update(USUARIO, 5L, registro(null, null, ayer, "83.0")).getFecha()).isEqualTo(ayer);
    }

    @Test
    @DisplayName("update lanza DuplicateResourceException y no guarda si la fecha ya tiene otro registro")
    void updateLanzaSiLaFechaEstaOcupadaPorOtro() {
        LocalDate ayer = HOY.minusDays(1);
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, USUARIO, HOY, "83.0")));
        when(repository.findByUsuarioIdAndFecha(USUARIO, ayer))
                .thenReturn(Optional.of(registro(6L, USUARIO, ayer, "79.00")));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, registro(null, null, ayer, "83.0")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza MedidaCorporalNotFoundException y no guarda si es de otro usuario")
    void updateLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, OTRO_USUARIO, HOY, "83.0")));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, registro(null, null, HOY, "81.0")))
                .isInstanceOf(MedidaCorporalNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("delete comprueba propiedad antes de borrar")
    void deleteComprobarPropiedadAntesDeBorrar() {
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, USUARIO, HOY, "83.0")));

        service.delete(USUARIO, 5L);

        verify(repository).deleteById(5L);
    }

    @Test
    @DisplayName("delete no borra si es de otro usuario")
    void deleteNoBorraSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(registro(5L, OTRO_USUARIO, HOY, "83.0")));

        assertThatThrownBy(() -> service.delete(USUARIO, 5L))
                .isInstanceOf(MedidaCorporalNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
