package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.out.CategoriaRepositoryPort;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.application.out.RecurrenteRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaFilter;
import com.polaris.kuiper.domain.model.CategoriaNotFoundException;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
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
 * Lo que mas importa: nombre unico por usuario y tipo, y que un usuario nunca
 * vea ni toque la categoria de otro. Ver docs/decisiones/011-categoria-nombre-unico-por-tipo.md.
 */
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;

    @Mock
    private CategoriaRepositoryPort repository;

    @Mock
    private MovimientoRepositoryPort movimientoRepository;

    @Mock
    private PresupuestoRepositoryPort presupuestoRepository;

    @Mock
    private RecurrenteRepositoryPort recurrenteRepository;

    @InjectMocks
    private CategoriaService service;

    private static Categoria categoria(Long id, Long usuarioId, String nombre, TipoMovimiento tipo) {
        return Categoria.builder().id(id).usuarioId(usuarioId).nombre(nombre).tipo(tipo).build();
    }

    @Test
    @DisplayName("create fija el usuarioId del JWT y anula el id que traiga")
    void createFijaUsuarioYAnulaId() {
        when(repository.findByUsuarioIdAndNombreAndTipo(USUARIO, "Comida", TipoMovimiento.GASTO))
                .thenReturn(Optional.empty());
        when(repository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        Categoria creada = service.create(USUARIO, categoria(999L, 999L, "Comida", TipoMovimiento.GASTO));

        assertThat(creada.getId()).isNull();
        assertThat(creada.getUsuarioId()).isEqualTo(USUARIO);
    }

    @Test
    @DisplayName("create lanza DuplicateResourceException y no guarda si ya hay una con ese nombre y tipo")
    void createLanzaSiElNombreYaExiste() {
        when(repository.findByUsuarioIdAndNombreAndTipo(USUARIO, "Comida", TipoMovimiento.GASTO))
                .thenReturn(Optional.of(categoria(3L, USUARIO, "Comida", TipoMovimiento.GASTO)));

        assertThatThrownBy(() -> service.create(USUARIO, categoria(null, null, "Comida", TipoMovimiento.GASTO)))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create permite el mismo nombre con otro tipo")
    void createPermiteMismoNombreConOtroTipo() {
        when(repository.findByUsuarioIdAndNombreAndTipo(USUARIO, "Otros", TipoMovimiento.INGRESO))
                .thenReturn(Optional.empty());
        when(repository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.create(USUARIO, categoria(null, null, "Otros", TipoMovimiento.INGRESO)).getTipo())
                .isEqualTo(TipoMovimiento.INGRESO);
    }

    @Test
    @DisplayName("get devuelve la categoria cuando es del usuario")
    void getDevuelveCategoriaPropia() {
        when(repository.findById(5L)).thenReturn(Optional.of(categoria(5L, USUARIO, "Comida", TipoMovimiento.GASTO)));

        assertThat(service.get(USUARIO, 5L).getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("get lanza CategoriaNotFoundException, no 403, si es de otro usuario")
    void getLanzaNotFoundSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(categoria(5L, OTRO_USUARIO, "Comida", TipoMovimiento.GASTO)));

        assertThatThrownBy(() -> service.get(USUARIO, 5L))
                .isInstanceOf(CategoriaNotFoundException.class);
    }

    @Test
    @DisplayName("get lanza CategoriaNotFoundException si el id no existe")
    void getLanzaNotFoundSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(USUARIO, 42L))
                .isInstanceOf(CategoriaNotFoundException.class);
    }

    @Test
    @DisplayName("list delega usuarioId y filtro tal cual en el repositorio")
    void listDelegaUsuarioYFiltro() {
        CategoriaFilter filtro = CategoriaFilter.builder().tipo(TipoMovimiento.GASTO).build();
        List<Categoria> esperado = List.of(categoria(1L, USUARIO, "Comida", TipoMovimiento.GASTO));
        when(repository.findAll(USUARIO, filtro)).thenReturn(esperado);

        assertThat(service.list(USUARIO, filtro)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("update conserva id y usuarioId de la existente, y permite guardar con su propio nombre")
    void updateConservaIdYUsuarioDeLaExistente() {
        Categoria existente = categoria(5L, USUARIO, "Comida", TipoMovimiento.GASTO);
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(repository.findByUsuarioIdAndNombreAndTipo(USUARIO, "Comida", TipoMovimiento.GASTO))
                .thenReturn(Optional.of(existente));
        when(repository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        Categoria cambios = categoria(999L, 999L, "Comida", TipoMovimiento.GASTO);
        cambios.setColor("#FF8800");
        Categoria actualizada = service.update(USUARIO, 5L, cambios);

        assertThat(actualizada.getId()).isEqualTo(5L);
        assertThat(actualizada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(actualizada.getColor()).isEqualTo("#FF8800");
    }

    @Test
    @DisplayName("update lanza DuplicateResourceException y no guarda si otra categoria ya usa ese nombre y tipo")
    void updateLanzaSiElNombreLoUsaOtra() {
        when(repository.findById(5L)).thenReturn(Optional.of(categoria(5L, USUARIO, "Comida", TipoMovimiento.GASTO)));
        when(repository.findByUsuarioIdAndNombreAndTipo(USUARIO, "Ocio", TipoMovimiento.GASTO))
                .thenReturn(Optional.of(categoria(6L, USUARIO, "Ocio", TipoMovimiento.GASTO)));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, categoria(null, null, "Ocio", TipoMovimiento.GASTO)))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza CategoriaNotFoundException y no guarda si es de otro usuario")
    void updateLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(categoria(5L, OTRO_USUARIO, "Comida", TipoMovimiento.GASTO)));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, categoria(null, null, "Comida", TipoMovimiento.GASTO)))
                .isInstanceOf(CategoriaNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("delete comprueba propiedad antes de borrar")
    void deleteComprobarPropiedadAntesDeBorrar() {
        when(repository.findById(5L)).thenReturn(Optional.of(categoria(5L, USUARIO, "Comida", TipoMovimiento.GASTO)));
        when(movimientoRepository.existsByCategoriaId(5L)).thenReturn(false);

        service.delete(USUARIO, 5L);

        verify(repository).deleteById(5L);
    }

    @Test
    @DisplayName("delete lanza ValidationException y no borra si la categoria tiene movimientos")
    void deleteLanzaSiTieneMovimientos() {
        when(repository.findById(5L)).thenReturn(Optional.of(categoria(5L, USUARIO, "Comida", TipoMovimiento.GASTO)));
        when(movimientoRepository.existsByCategoriaId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(USUARIO, 5L))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete lanza ValidationException y no borra si la categoria tiene presupuestos")
    void deleteLanzaSiTienePresupuestos() {
        when(repository.findById(5L)).thenReturn(Optional.of(categoria(5L, USUARIO, "Comida", TipoMovimiento.GASTO)));
        when(movimientoRepository.existsByCategoriaId(5L)).thenReturn(false);
        when(presupuestoRepository.existsByCategoriaId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(USUARIO, 5L))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("update lanza ValidationException y no guarda si cambia el tipo de una categoria con presupuestos")
    void updateLanzaSiCambiaTipoConPresupuestos() {
        when(repository.findById(5L)).thenReturn(Optional.of(categoria(5L, USUARIO, "Comida", TipoMovimiento.GASTO)));
        when(repository.findByUsuarioIdAndNombreAndTipo(USUARIO, "Comida", TipoMovimiento.INGRESO))
                .thenReturn(Optional.empty());
        when(movimientoRepository.existsByCategoriaId(5L)).thenReturn(false);
        when(presupuestoRepository.existsByCategoriaId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(USUARIO, 5L, categoria(null, null, "Comida", TipoMovimiento.INGRESO)))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza ValidationException y no guarda si cambia el tipo de una categoria con movimientos")
    void updateLanzaSiCambiaTipoConMovimientos() {
        when(repository.findById(5L)).thenReturn(Optional.of(categoria(5L, USUARIO, "Comida", TipoMovimiento.GASTO)));
        when(repository.findByUsuarioIdAndNombreAndTipo(USUARIO, "Comida", TipoMovimiento.INGRESO))
                .thenReturn(Optional.empty());
        when(movimientoRepository.existsByCategoriaId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(USUARIO, 5L, categoria(null, null, "Comida", TipoMovimiento.INGRESO)))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update permite cambiar el tipo si la categoria no tiene movimientos")
    void updateCambiaTipoSinMovimientos() {
        when(repository.findById(5L)).thenReturn(Optional.of(categoria(5L, USUARIO, "Comida", TipoMovimiento.GASTO)));
        when(repository.findByUsuarioIdAndNombreAndTipo(USUARIO, "Comida", TipoMovimiento.INGRESO))
                .thenReturn(Optional.empty());
        when(movimientoRepository.existsByCategoriaId(5L)).thenReturn(false);
        when(repository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.update(USUARIO, 5L, categoria(null, null, "Comida", TipoMovimiento.INGRESO)).getTipo())
                .isEqualTo(TipoMovimiento.INGRESO);
    }

    @Test
    @DisplayName("delete no borra si es de otro usuario")
    void deleteNoBorraSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(categoria(5L, OTRO_USUARIO, "Comida", TipoMovimiento.GASTO)));

        assertThatThrownBy(() -> service.delete(USUARIO, 5L))
                .isInstanceOf(CategoriaNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
