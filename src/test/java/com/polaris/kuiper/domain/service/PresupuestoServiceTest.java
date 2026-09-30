package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.out.CategoriaRepositoryPort;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaNotFoundException;
import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.domain.model.PresupuestoFilter;
import com.polaris.kuiper.domain.model.PresupuestoNotFoundException;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo que mas importa: solo categorias de gasto, un presupuesto por categoria y
 * periodo, y que un usuario nunca vea ni toque el de otro. Ver
 * docs/decisiones/013-presupuesto-solo-gastos-uno-por-periodo.md.
 */
@ExtendWith(MockitoExtension.class)
class PresupuestoServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;
    private static final PeriodoPresupuesto MENSUAL = PeriodoPresupuesto.MENSUAL;

    @Mock
    private PresupuestoRepositoryPort repository;

    @Mock
    private CategoriaRepositoryPort categoriaRepository;

    @InjectMocks
    private PresupuestoService service;

    private static Categoria categoria(Long id, Long usuarioId, TipoMovimiento tipo) {
        return Categoria.builder().id(id).usuarioId(usuarioId).nombre("Cat " + id).tipo(tipo).build();
    }

    private static Presupuesto presupuesto(Long id, Long usuarioId, Long categoriaId, PeriodoPresupuesto periodo) {
        return Presupuesto.builder().id(id).usuarioId(usuarioId).categoriaId(categoriaId).periodo(periodo)
                .importeLimite(new BigDecimal("200.00")).build();
    }

    @Test
    @DisplayName("create fija el usuarioId del JWT y anula el id que traiga")
    void createFijaUsuarioYAnulaId() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria(10L, USUARIO, TipoMovimiento.GASTO)));
        when(repository.findByUsuarioIdAndCategoriaIdAndPeriodo(USUARIO, 10L, MENSUAL)).thenReturn(Optional.empty());
        when(repository.save(any(Presupuesto.class))).thenAnswer(inv -> inv.getArgument(0));

        Presupuesto creado = service.create(USUARIO, presupuesto(999L, 999L, 10L, MENSUAL));

        assertThat(creado.getId()).isNull();
        assertThat(creado.getUsuarioId()).isEqualTo(USUARIO);
    }

    @Test
    @DisplayName("create lanza CategoriaNotFoundException y no guarda si la categoria no existe")
    void createLanzaSiLaCategoriaNoExiste() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(USUARIO, presupuesto(null, null, 10L, MENSUAL)))
                .isInstanceOf(CategoriaNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create lanza CategoriaNotFoundException, no 403, si la categoria es de otro usuario")
    void createLanzaSiLaCategoriaEsDeOtroUsuario() {
        when(categoriaRepository.findById(10L))
                .thenReturn(Optional.of(categoria(10L, OTRO_USUARIO, TipoMovimiento.GASTO)));

        assertThatThrownBy(() -> service.create(USUARIO, presupuesto(null, null, 10L, MENSUAL)))
                .isInstanceOf(CategoriaNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create lanza ValidationException y no guarda si la categoria es de ingreso")
    void createLanzaSiLaCategoriaEsDeIngreso() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria(10L, USUARIO, TipoMovimiento.INGRESO)));

        assertThatThrownBy(() -> service.create(USUARIO, presupuesto(null, null, 10L, MENSUAL)))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create lanza DuplicateResourceException y no guarda si ya hay uno para esa categoria y periodo")
    void createLanzaSiYaExisteParaEsePeriodo() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria(10L, USUARIO, TipoMovimiento.GASTO)));
        when(repository.findByUsuarioIdAndCategoriaIdAndPeriodo(USUARIO, 10L, MENSUAL))
                .thenReturn(Optional.of(presupuesto(3L, USUARIO, 10L, MENSUAL)));

        assertThatThrownBy(() -> service.create(USUARIO, presupuesto(null, null, 10L, MENSUAL)))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create permite la misma categoria con otro periodo")
    void createPermiteMismaCategoriaConOtroPeriodo() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria(10L, USUARIO, TipoMovimiento.GASTO)));
        when(repository.findByUsuarioIdAndCategoriaIdAndPeriodo(USUARIO, 10L, PeriodoPresupuesto.ANUAL))
                .thenReturn(Optional.empty());
        when(repository.save(any(Presupuesto.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.create(USUARIO, presupuesto(null, null, 10L, PeriodoPresupuesto.ANUAL)).getPeriodo())
                .isEqualTo(PeriodoPresupuesto.ANUAL);
    }

    @Test
    @DisplayName("get devuelve el presupuesto cuando es del usuario")
    void getDevuelvePresupuestoPropio() {
        when(repository.findById(5L)).thenReturn(Optional.of(presupuesto(5L, USUARIO, 10L, MENSUAL)));

        assertThat(service.get(USUARIO, 5L).getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("get lanza PresupuestoNotFoundException, no 403, si es de otro usuario")
    void getLanzaNotFoundSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(presupuesto(5L, OTRO_USUARIO, 10L, MENSUAL)));

        assertThatThrownBy(() -> service.get(USUARIO, 5L))
                .isInstanceOf(PresupuestoNotFoundException.class);
    }

    @Test
    @DisplayName("get lanza PresupuestoNotFoundException si el id no existe")
    void getLanzaNotFoundSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(USUARIO, 42L))
                .isInstanceOf(PresupuestoNotFoundException.class);
    }

    @Test
    @DisplayName("list delega usuarioId y filtro tal cual en el repositorio")
    void listDelegaUsuarioYFiltro() {
        PresupuestoFilter filtro = PresupuestoFilter.builder().periodo(MENSUAL).build();
        List<Presupuesto> esperado = List.of(presupuesto(1L, USUARIO, 10L, MENSUAL));
        when(repository.findAll(USUARIO, filtro)).thenReturn(esperado);

        assertThat(service.list(USUARIO, filtro)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("update conserva id y usuarioId del existente, y permite guardar con su propia categoria y periodo")
    void updateConservaIdYUsuarioDelExistente() {
        Presupuesto existente = presupuesto(5L, USUARIO, 10L, MENSUAL);
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria(10L, USUARIO, TipoMovimiento.GASTO)));
        when(repository.findByUsuarioIdAndCategoriaIdAndPeriodo(USUARIO, 10L, MENSUAL))
                .thenReturn(Optional.of(existente));
        when(repository.save(any(Presupuesto.class))).thenAnswer(inv -> inv.getArgument(0));

        Presupuesto cambios = presupuesto(999L, 999L, 10L, MENSUAL);
        cambios.setImporteLimite(new BigDecimal("250.00"));
        Presupuesto actualizado = service.update(USUARIO, 5L, cambios);

        assertThat(actualizado.getId()).isEqualTo(5L);
        assertThat(actualizado.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(actualizado.getImporteLimite()).isEqualByComparingTo("250.00");
    }

    @Test
    @DisplayName("update lanza DuplicateResourceException y no guarda si otro presupuesto ya cubre esa categoria y periodo")
    void updateLanzaSiOtroYaCubreEsePeriodo() {
        when(repository.findById(5L)).thenReturn(Optional.of(presupuesto(5L, USUARIO, 10L, MENSUAL)));
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria(10L, USUARIO, TipoMovimiento.GASTO)));
        when(repository.findByUsuarioIdAndCategoriaIdAndPeriodo(USUARIO, 10L, PeriodoPresupuesto.ANUAL))
                .thenReturn(Optional.of(presupuesto(6L, USUARIO, 10L, PeriodoPresupuesto.ANUAL)));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, presupuesto(null, null, 10L, PeriodoPresupuesto.ANUAL)))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza PresupuestoNotFoundException y no guarda si es de otro usuario")
    void updateLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(presupuesto(5L, OTRO_USUARIO, 10L, MENSUAL)));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, presupuesto(null, null, 10L, MENSUAL)))
                .isInstanceOf(PresupuestoNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("delete comprueba propiedad antes de borrar")
    void deleteComprobarPropiedadAntesDeBorrar() {
        when(repository.findById(5L)).thenReturn(Optional.of(presupuesto(5L, USUARIO, 10L, MENSUAL)));

        service.delete(USUARIO, 5L);

        verify(repository).deleteById(5L);
    }

    @Test
    @DisplayName("delete no borra si es de otro usuario")
    void deleteNoBorraSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(presupuesto(5L, OTRO_USUARIO, 10L, MENSUAL)));

        assertThatThrownBy(() -> service.delete(USUARIO, 5L))
                .isInstanceOf(PresupuestoNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
