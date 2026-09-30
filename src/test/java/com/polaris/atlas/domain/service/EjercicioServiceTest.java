package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.out.EjercicioRepositoryPort;
import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.EjercicioCatalogoNoModificableException;
import com.polaris.atlas.domain.model.EjercicioFilter;
import com.polaris.atlas.domain.model.EjercicioNotFoundException;
import com.polaris.shared.error.DuplicateResourceException;
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
 * Lo que mas importa: el catalogo (usuarioId nulo) se ve pero no se toca, lo
 * propio solo lo toca su dueno, y lo de otro usuario no existe. Ver
 * docs/decisiones/023-ejercicio-catalogo-y-propios.md.
 */
@ExtendWith(MockitoExtension.class)
class EjercicioServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;

    @Mock
    private EjercicioRepositoryPort repository;

    @InjectMocks
    private EjercicioService service;

    private static Ejercicio ejercicio(Long id, Long usuarioId, String nombre) {
        return Ejercicio.builder().id(id).usuarioId(usuarioId).nombre(nombre).grupoMuscular("Pecho").build();
    }

    // ---- create ----

    @Test
    @DisplayName("create fija el usuarioId del JWT y anula el id y el usuarioId que traiga (nunca crea catalogo)")
    void createFijaUsuarioYAnulaId() {
        when(repository.findVisiblesByNombre(USUARIO, "Press banca")).thenReturn(List.of());
        when(repository.save(any(Ejercicio.class))).thenAnswer(inv -> inv.getArgument(0));

        Ejercicio creado = service.create(USUARIO, ejercicio(999L, null, "Press banca"));

        assertThat(creado.getId()).isNull();
        assertThat(creado.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(creado.isPropio()).isTrue();
    }

    @Test
    @DisplayName("create lanza DuplicateResourceException y no guarda si ya tiene uno propio con ese nombre")
    void createLanzaSiYaTieneUnoPropio() {
        when(repository.findVisiblesByNombre(USUARIO, "Press banca"))
                .thenReturn(List.of(ejercicio(3L, USUARIO, "Press banca")));

        assertThatThrownBy(() -> service.create(USUARIO, ejercicio(null, null, "Press banca")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create lanza DuplicateResourceException y no guarda si el nombre ya esta en el catalogo")
    void createLanzaSiElNombreEstaEnElCatalogo() {
        when(repository.findVisiblesByNombre(USUARIO, "Sentadilla"))
                .thenReturn(List.of(ejercicio(3L, null, "Sentadilla")));

        assertThatThrownBy(() -> service.create(USUARIO, ejercicio(null, null, "Sentadilla")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    // ---- get ----

    @Test
    @DisplayName("get devuelve un ejercicio del catalogo")
    void getDevuelveCatalogo() {
        when(repository.findById(5L)).thenReturn(Optional.of(ejercicio(5L, null, "Sentadilla")));

        assertThat(service.get(USUARIO, 5L).getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("get devuelve un ejercicio propio")
    void getDevuelvePropio() {
        when(repository.findById(5L)).thenReturn(Optional.of(ejercicio(5L, USUARIO, "Mi ejercicio")));

        assertThat(service.get(USUARIO, 5L).getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("get lanza EjercicioNotFoundException, no 403, si es propio de otro usuario")
    void getLanzaNotFoundSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(ejercicio(5L, OTRO_USUARIO, "Mi ejercicio")));

        assertThatThrownBy(() -> service.get(USUARIO, 5L))
                .isInstanceOf(EjercicioNotFoundException.class);
    }

    @Test
    @DisplayName("get lanza EjercicioNotFoundException si el id no existe")
    void getLanzaNotFoundSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(USUARIO, 42L))
                .isInstanceOf(EjercicioNotFoundException.class);
    }

    // ---- list ----

    @Test
    @DisplayName("list delega usuarioId y filtro tal cual en el repositorio, que resuelve catalogo mas propios")
    void listDelegaUsuarioYFiltro() {
        EjercicioFilter filtro = EjercicioFilter.builder().grupoMuscular("Pecho").build();
        List<Ejercicio> esperado = List.of(ejercicio(1L, null, "Press banca"), ejercicio(2L, USUARIO, "Flexiones"));
        when(repository.findAll(USUARIO, filtro)).thenReturn(esperado);

        assertThat(service.list(USUARIO, filtro)).isEqualTo(esperado);
    }

    // ---- update ----

    @Test
    @DisplayName("update conserva id y usuarioId del existente, y permite guardar con su propio nombre")
    void updateConservaIdYUsuarioDelExistente() {
        Ejercicio existente = ejercicio(5L, USUARIO, "Flexiones");
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(repository.findVisiblesByNombre(USUARIO, "Flexiones")).thenReturn(List.of(existente));
        when(repository.save(any(Ejercicio.class))).thenAnswer(inv -> inv.getArgument(0));

        Ejercicio cambios = ejercicio(999L, OTRO_USUARIO, "Flexiones");
        cambios.setEquipamiento("Peso corporal");
        Ejercicio actualizado = service.update(USUARIO, 5L, cambios);

        assertThat(actualizado.getId()).isEqualTo(5L);
        assertThat(actualizado.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(actualizado.getEquipamiento()).isEqualTo("Peso corporal");
    }

    @Test
    @DisplayName("update lanza DuplicateResourceException y no guarda si el nombre lo usa otro ejercicio visible")
    void updateLanzaSiElNombreLoUsaOtro() {
        when(repository.findById(5L)).thenReturn(Optional.of(ejercicio(5L, USUARIO, "Flexiones")));
        when(repository.findVisiblesByNombre(USUARIO, "Sentadilla"))
                .thenReturn(List.of(ejercicio(6L, null, "Sentadilla")));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, ejercicio(null, null, "Sentadilla")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza EjercicioCatalogoNoModificableException (403) y no guarda si es del catalogo")
    void updateLanzaSiEsDelCatalogo() {
        when(repository.findById(5L)).thenReturn(Optional.of(ejercicio(5L, null, "Sentadilla")));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, ejercicio(null, null, "Sentadilla")))
                .isInstanceOf(EjercicioCatalogoNoModificableException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza EjercicioNotFoundException y no guarda si es propio de otro usuario")
    void updateLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(ejercicio(5L, OTRO_USUARIO, "Flexiones")));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, ejercicio(null, null, "Flexiones")))
                .isInstanceOf(EjercicioNotFoundException.class);

        verify(repository, never()).save(any());
    }

    // ---- delete ----

    @Test
    @DisplayName("delete borra un ejercicio propio")
    void deleteBorraPropio() {
        when(repository.findById(5L)).thenReturn(Optional.of(ejercicio(5L, USUARIO, "Flexiones")));

        service.delete(USUARIO, 5L);

        verify(repository).deleteById(5L);
    }

    @Test
    @DisplayName("delete lanza EjercicioCatalogoNoModificableException (403) y no borra si es del catalogo")
    void deleteNoBorraCatalogo() {
        when(repository.findById(5L)).thenReturn(Optional.of(ejercicio(5L, null, "Sentadilla")));

        assertThatThrownBy(() -> service.delete(USUARIO, 5L))
                .isInstanceOf(EjercicioCatalogoNoModificableException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete lanza EjercicioNotFoundException y no borra si es propio de otro usuario")
    void deleteNoBorraDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(ejercicio(5L, OTRO_USUARIO, "Flexiones")));

        assertThatThrownBy(() -> service.delete(USUARIO, 5L))
                .isInstanceOf(EjercicioNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete lanza EjercicioNotFoundException si el id no existe")
    void deleteLanzaSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(USUARIO, 42L))
                .isInstanceOf(EjercicioNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
