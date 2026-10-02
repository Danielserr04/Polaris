package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.ComprobarPresupuestoInterface;
import com.polaris.kuiper.application.out.CategoriaRepositoryPort;
import com.polaris.kuiper.application.out.CuentaRepositoryPort;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaNotFoundException;
import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.domain.model.CuentaNotFoundException;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.MovimientoNotFoundException;
import com.polaris.kuiper.domain.model.TipoMovimiento;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo que mas importa: la categoria tiene que ser del usuario y de su mismo
 * tipo, y un usuario nunca ve ni toca el movimiento de otro. Ver
 * docs/decisiones/012-movimiento-categoria-mismo-tipo.md.
 */
@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;
    private static final LocalDate HOY = LocalDate.of(2026, 9, 30);

    @Mock
    private MovimientoRepositoryPort repository;

    @Mock
    private CategoriaRepositoryPort categoriaRepository;

    @Mock
    private ComprobarPresupuestoInterface comprobarPresupuesto;

    @Mock
    private CuentaRepositoryPort cuentaRepository;

    @InjectMocks
    private MovimientoService service;

    private static Categoria categoria(Long id, Long usuarioId, TipoMovimiento tipo) {
        return Categoria.builder().id(id).usuarioId(usuarioId).nombre("Cat " + id).tipo(tipo).build();
    }

    private static Movimiento movimiento(Long id, Long usuarioId, Long categoriaId, TipoMovimiento tipo) {
        return Movimiento.builder().id(id).usuarioId(usuarioId).fecha(HOY).importe(new BigDecimal("12.50"))
                .tipo(tipo).categoriaId(categoriaId).build();
    }

    @Test
    @DisplayName("create fija el usuarioId del JWT y anula el id que traiga")
    void createFijaUsuarioYAnulaId() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria(10L, USUARIO, TipoMovimiento.GASTO)));
        when(repository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        Movimiento creado = service.create(USUARIO, movimiento(999L, 999L, 10L, TipoMovimiento.GASTO));

        assertThat(creado.getId()).isNull();
        assertThat(creado.getUsuarioId()).isEqualTo(USUARIO);
    }

    @Test
    @DisplayName("create lanza CategoriaNotFoundException y no guarda si la categoria no existe")
    void createLanzaSiLaCategoriaNoExiste() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(USUARIO, movimiento(null, null, 10L, TipoMovimiento.GASTO)))
                .isInstanceOf(CategoriaNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create lanza CategoriaNotFoundException, no 403, si la categoria es de otro usuario")
    void createLanzaSiLaCategoriaEsDeOtroUsuario() {
        when(categoriaRepository.findById(10L))
                .thenReturn(Optional.of(categoria(10L, OTRO_USUARIO, TipoMovimiento.GASTO)));

        assertThatThrownBy(() -> service.create(USUARIO, movimiento(null, null, 10L, TipoMovimiento.GASTO)))
                .isInstanceOf(CategoriaNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create lanza ValidationException y no guarda si el tipo no coincide con el de la categoria")
    void createLanzaSiElTipoNoCoincide() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria(10L, USUARIO, TipoMovimiento.INGRESO)));

        assertThatThrownBy(() -> service.create(USUARIO, movimiento(null, null, 10L, TipoMovimiento.GASTO)))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("get devuelve el movimiento cuando es del usuario")
    void getDevuelveMovimientoPropio() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, USUARIO, 10L, TipoMovimiento.GASTO)));

        assertThat(service.get(USUARIO, 5L).getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("get lanza MovimientoNotFoundException, no 403, si es de otro usuario")
    void getLanzaNotFoundSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, OTRO_USUARIO, 10L, TipoMovimiento.GASTO)));

        assertThatThrownBy(() -> service.get(USUARIO, 5L))
                .isInstanceOf(MovimientoNotFoundException.class);
    }

    @Test
    @DisplayName("get lanza MovimientoNotFoundException si el id no existe")
    void getLanzaNotFoundSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(USUARIO, 42L))
                .isInstanceOf(MovimientoNotFoundException.class);
    }

    @Test
    @DisplayName("list delega usuarioId y filtro tal cual en el repositorio")
    void listDelegaUsuarioYFiltro() {
        MovimientoFilter filtro = MovimientoFilter.builder().tipo(TipoMovimiento.GASTO).categoriaId(10L).build();
        List<Movimiento> esperado = List.of(movimiento(1L, USUARIO, 10L, TipoMovimiento.GASTO));
        when(repository.findAll(USUARIO, filtro)).thenReturn(esperado);

        assertThat(service.list(USUARIO, filtro)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("update conserva id y usuarioId del movimiento existente")
    void updateConservaIdYUsuarioDelExistente() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, USUARIO, 10L, TipoMovimiento.GASTO)));
        when(categoriaRepository.findById(11L)).thenReturn(Optional.of(categoria(11L, USUARIO, TipoMovimiento.GASTO)));
        when(repository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        Movimiento actualizado = service.update(USUARIO, 5L, movimiento(999L, 999L, 11L, TipoMovimiento.GASTO));

        assertThat(actualizado.getId()).isEqualTo(5L);
        assertThat(actualizado.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(actualizado.getCategoriaId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("update lanza ValidationException y no guarda si la nueva categoria es de otro tipo")
    void updateLanzaSiElTipoNoCoincide() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, USUARIO, 10L, TipoMovimiento.GASTO)));
        when(categoriaRepository.findById(11L)).thenReturn(Optional.of(categoria(11L, USUARIO, TipoMovimiento.INGRESO)));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, movimiento(null, null, 11L, TipoMovimiento.GASTO)))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza CategoriaNotFoundException, no 403, y no guarda si la nueva categoria es de otro usuario")
    void updateLanzaSiLaCategoriaEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, USUARIO, 10L, TipoMovimiento.GASTO)));
        when(categoriaRepository.findById(11L))
                .thenReturn(Optional.of(categoria(11L, OTRO_USUARIO, TipoMovimiento.GASTO)));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, movimiento(null, null, 11L, TipoMovimiento.GASTO)))
                .isInstanceOf(CategoriaNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza CategoriaNotFoundException y no guarda si la nueva categoria no existe")
    void updateLanzaSiLaCategoriaNoExiste() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, USUARIO, 10L, TipoMovimiento.GASTO)));
        when(categoriaRepository.findById(11L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(USUARIO, 5L, movimiento(null, null, 11L, TipoMovimiento.GASTO)))
                .isInstanceOf(CategoriaNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza ValidationException y no guarda si un ingreso apunta a una categoria de gasto")
    void updateLanzaSiUnIngresoApuntaACategoriaDeGasto() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, USUARIO, 12L, TipoMovimiento.INGRESO)));
        when(categoriaRepository.findById(11L)).thenReturn(Optional.of(categoria(11L, USUARIO, TipoMovimiento.GASTO)));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, movimiento(null, null, 11L, TipoMovimiento.INGRESO)))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza MovimientoNotFoundException y no guarda si es de otro usuario")
    void updateLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, OTRO_USUARIO, 10L, TipoMovimiento.GASTO)));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, movimiento(null, null, 10L, TipoMovimiento.GASTO)))
                .isInstanceOf(MovimientoNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("delete comprueba propiedad y manda a la papelera, sin borrar de verdad")
    void deleteComprobarPropiedadAntesDeBorrar() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, USUARIO, 10L, TipoMovimiento.GASTO)));

        service.delete(USUARIO, 5L);

        verify(repository).moverAPapelera(eq(USUARIO), eq(List.of(5L)), any());
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete de un movimiento que ya esta en la papelera es 404 (findById no lo ve)")
    void deleteEnPapeleraEs404() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(USUARIO, 5L))
                .isInstanceOf(MovimientoNotFoundException.class);

        verify(repository, never()).moverAPapelera(any(), anyList(), any());
    }

    @Test
    @DisplayName("delete no borra si es de otro usuario")
    void deleteNoBorraSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, OTRO_USUARIO, 10L, TipoMovimiento.GASTO)));

        assertThatThrownBy(() -> service.delete(USUARIO, 5L))
                .isInstanceOf(MovimientoNotFoundException.class);

        verify(repository, never()).deleteById(any());
        verify(repository, never()).moverAPapelera(any(), anyList(), any());
    }

    @Test
    @DisplayName("duplicar copia todo salvo id y fecha, sin la marca recurrente, con la fecha pedida")
    void duplicarCopiaConLaFechaPedida() {
        Movimiento original = movimiento(5L, USUARIO, 10L, TipoMovimiento.GASTO);
        original.setConcepto("Cafe");
        original.setMetodoPago("Tarjeta");
        original.setRecurrente(true);
        when(repository.findById(5L)).thenReturn(Optional.of(original));
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria(10L, USUARIO, TipoMovimiento.GASTO)));
        when(repository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        Movimiento copia = service.duplicar(USUARIO, 5L, LocalDate.of(2026, 1, 15));

        assertThat(copia.getId()).isNull();
        assertThat(copia.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(copia.getFecha()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(copia.getImporte()).isEqualByComparingTo("12.50");
        assertThat(copia.getTipo()).isEqualTo(TipoMovimiento.GASTO);
        assertThat(copia.getCategoriaId()).isEqualTo(10L);
        assertThat(copia.getConcepto()).isEqualTo("Cafe");
        assertThat(copia.getMetodoPago()).isEqualTo("Tarjeta");
        assertThat(copia.isRecurrente()).isFalse();
        assertThat(copia.getBorradoEn()).isNull();
        assertThat(copia).isNotSameAs(original);
    }

    @Test
    @DisplayName("duplicar sin fecha usa hoy")
    void duplicarSinFechaUsaHoy() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, USUARIO, 10L, TipoMovimiento.GASTO)));
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria(10L, USUARIO, TipoMovimiento.GASTO)));
        when(repository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.duplicar(USUARIO, 5L, null).getFecha()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("duplicar con fecha futura es 400 y no guarda")
    void duplicarConFechaFuturaLanza() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, USUARIO, 10L, TipoMovimiento.GASTO)));

        assertThatThrownBy(() -> service.duplicar(USUARIO, 5L, LocalDate.now().plusDays(1)))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("duplicar el movimiento de otro usuario es 404 y no guarda")
    void duplicarDeOtroUsuarioLanza() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, OTRO_USUARIO, 10L, TipoMovimiento.GASTO)));

        assertThatThrownBy(() -> service.duplicar(USUARIO, 5L, null))
                .isInstanceOf(MovimientoNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create de un gasto comprueba el presupuesto de su categoria con la fecha del movimiento")
    void createDeGastoCompruebaPresupuesto() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria(10L, USUARIO, TipoMovimiento.GASTO)));
        when(repository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(USUARIO, movimiento(null, null, 10L, TipoMovimiento.GASTO));

        verify(comprobarPresupuesto).comprobar(USUARIO, 10L, HOY);
    }

    @Test
    @DisplayName("update de un gasto tambien comprueba el presupuesto")
    void updateDeGastoCompruebaPresupuesto() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, USUARIO, 10L, TipoMovimiento.GASTO)));
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria(10L, USUARIO, TipoMovimiento.GASTO)));
        when(repository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        service.update(USUARIO, 5L, movimiento(null, null, 10L, TipoMovimiento.GASTO));

        verify(comprobarPresupuesto).comprobar(USUARIO, 10L, HOY);
    }

    @Test
    @DisplayName("Un ingreso no comprueba presupuestos")
    void ingresoNoCompruebaPresupuesto() {
        when(categoriaRepository.findById(20L)).thenReturn(Optional.of(categoria(20L, USUARIO, TipoMovimiento.INGRESO)));
        when(repository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(USUARIO, movimiento(null, null, 20L, TipoMovimiento.INGRESO));

        verify(comprobarPresupuesto, never()).comprobar(any(), any(), any());
    }

    @Test
    @DisplayName("Si la categoria no es valida no se guarda ni se comprueba nada")
    void sinGuardadoNoHayComprobacion() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(USUARIO, movimiento(null, null, 10L, TipoMovimiento.GASTO)))
                .isInstanceOf(CategoriaNotFoundException.class);

        verify(comprobarPresupuesto, never()).comprobar(any(), any(), any());
    }

    @Test
    @DisplayName("create acepta una cuenta propia y no la busca si no viene")
    void createConYSinCuenta() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria(10L, USUARIO, TipoMovimiento.GASTO)));
        when(cuentaRepository.findById(3L)).thenReturn(Optional.of(Cuenta.builder().id(3L).usuarioId(USUARIO).build()));
        when(repository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        Movimiento conCuenta = movimiento(null, null, 10L, TipoMovimiento.GASTO);
        conCuenta.setCuentaId(3L);

        assertThat(service.create(USUARIO, conCuenta).getCuentaId()).isEqualTo(3L);
        assertThat(service.create(USUARIO, movimiento(null, null, 10L, TipoMovimiento.GASTO)).getCuentaId()).isNull();
        verify(cuentaRepository).findById(3L);
    }

    @Test
    @DisplayName("create y update lanzan CuentaNotFoundException si la cuenta es de otro usuario o no existe")
    void cuentaAjenaONoExistente() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria(10L, USUARIO, TipoMovimiento.GASTO)));
        when(cuentaRepository.findById(3L))
                .thenReturn(Optional.of(Cuenta.builder().id(3L).usuarioId(OTRO_USUARIO).build()));
        when(cuentaRepository.findById(4L)).thenReturn(Optional.empty());
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, USUARIO, 10L, TipoMovimiento.GASTO)));

        Movimiento ajena = movimiento(null, null, 10L, TipoMovimiento.GASTO);
        ajena.setCuentaId(3L);
        Movimiento inexistente = movimiento(null, null, 10L, TipoMovimiento.GASTO);
        inexistente.setCuentaId(4L);

        assertThatThrownBy(() -> service.create(USUARIO, ajena)).isInstanceOf(CuentaNotFoundException.class);
        assertThatThrownBy(() -> service.update(USUARIO, 5L, inexistente)).isInstanceOf(CuentaNotFoundException.class);
        verify(repository, never()).save(any());
    }
}
