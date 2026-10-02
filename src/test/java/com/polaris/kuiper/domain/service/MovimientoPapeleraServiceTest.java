package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.Movimiento;
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
import java.time.LocalDateTime;
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
 * La papelera: borrar varios es todo o nada, restaurar y borrar de verdad solo
 * ven lo que esta en la papelera del usuario, y la purga corta a 30 dias. Ver
 * docs/decisiones/038-movimiento-papelera-y-duplicar.md.
 */
@ExtendWith(MockitoExtension.class)
class MovimientoPapeleraServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;
    private static final LocalDateTime BORRADO = LocalDateTime.of(2026, 9, 20, 10, 0);

    @Mock
    private MovimientoRepositoryPort repository;

    @InjectMocks
    private MovimientoPapeleraService service;

    private static Movimiento movimiento(Long id, Long usuarioId, LocalDateTime borradoEn) {
        return Movimiento.builder().id(id).usuarioId(usuarioId).fecha(LocalDate.of(2026, 9, 1))
                .importe(new BigDecimal("12.50")).tipo(TipoMovimiento.GASTO).categoriaId(10L)
                .categoria(Categoria.builder().id(10L).usuarioId(usuarioId).nombre("Comida")
                        .tipo(TipoMovimiento.GASTO).build())
                .borradoEn(borradoEn).build();
    }

    @Test
    @DisplayName("borrar varios comprueba todos y los mueve en una sola llamada, sin repetidos")
    void borrarVariosLosMueveJuntos() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, USUARIO, null)));
        when(repository.findById(6L)).thenReturn(Optional.of(movimiento(6L, USUARIO, null)));

        service.borrar(USUARIO, List.of(5L, 6L, 5L));

        verify(repository).moverAPapelera(eq(USUARIO), eq(List.of(5L, 6L)), any());
    }

    @Test
    @DisplayName("borrar varios con un id de otro usuario es 404 y no mueve ninguno")
    void borrarVariosConUnoAjenoNoMueveNada() {
        when(repository.findById(5L)).thenReturn(Optional.of(movimiento(5L, USUARIO, null)));
        when(repository.findById(6L)).thenReturn(Optional.of(movimiento(6L, OTRO_USUARIO, null)));

        assertThatThrownBy(() -> service.borrar(USUARIO, List.of(5L, 6L)))
                .isInstanceOf(MovimientoNotFoundException.class);

        verify(repository, never()).moverAPapelera(any(), anyList(), any());
    }

    @Test
    @DisplayName("borrar varios con uno que no existe o ya esta en la papelera es 404 y no mueve ninguno")
    void borrarVariosConUnoInexistenteNoMueveNada() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.borrar(USUARIO, List.of(5L, 6L)))
                .isInstanceOf(MovimientoNotFoundException.class);

        verify(repository, never()).moverAPapelera(any(), anyList(), any());
    }

    @Test
    @DisplayName("listPapelera devuelve la del usuario tal cual la ordena el puerto")
    void listPapelera() {
        List<Movimiento> papelera = List.of(movimiento(5L, USUARIO, BORRADO));
        when(repository.findPapelera(USUARIO)).thenReturn(papelera);

        assertThat(service.listPapelera(USUARIO)).isSameAs(papelera);
    }

    @Test
    @DisplayName("restaurar quita borradoEn y guarda")
    void restaurarQuitaBorradoEn() {
        when(repository.findEnPapeleraById(5L)).thenReturn(Optional.of(movimiento(5L, USUARIO, BORRADO)));
        when(repository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        Movimiento restaurado = service.restaurar(USUARIO, 5L);

        assertThat(restaurado.getBorradoEn()).isNull();
        assertThat(restaurado.getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("restaurar uno de otro usuario o que no esta en la papelera es 404")
    void restaurarAjenoONoEnPapeleraEs404() {
        when(repository.findEnPapeleraById(5L)).thenReturn(Optional.of(movimiento(5L, OTRO_USUARIO, BORRADO)));
        when(repository.findEnPapeleraById(6L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.restaurar(USUARIO, 5L)).isInstanceOf(MovimientoNotFoundException.class);
        assertThatThrownBy(() -> service.restaurar(USUARIO, 6L)).isInstanceOf(MovimientoNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("restaurar es 400 si su categoria cambio de tipo mientras estaba en la papelera")
    void restaurarConCategoriaDeOtroTipoLanza() {
        Movimiento enPapelera = movimiento(5L, USUARIO, BORRADO);
        enPapelera.getCategoria().setTipo(TipoMovimiento.INGRESO);
        when(repository.findEnPapeleraById(5L)).thenReturn(Optional.of(enPapelera));

        assertThatThrownBy(() -> service.restaurar(USUARIO, 5L)).isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deleteDefinitivo borra de verdad uno de la papelera propia")
    void deleteDefinitivoBorra() {
        when(repository.findEnPapeleraById(5L)).thenReturn(Optional.of(movimiento(5L, USUARIO, BORRADO)));

        service.deleteDefinitivo(USUARIO, 5L);

        verify(repository).deleteById(5L);
    }

    @Test
    @DisplayName("deleteDefinitivo de uno fuera de la papelera o ajeno es 404 y no borra")
    void deleteDefinitivoFueraDePapeleraEs404() {
        when(repository.findEnPapeleraById(5L)).thenReturn(Optional.empty());
        when(repository.findEnPapeleraById(6L)).thenReturn(Optional.of(movimiento(6L, OTRO_USUARIO, BORRADO)));

        assertThatThrownBy(() -> service.deleteDefinitivo(USUARIO, 5L))
                .isInstanceOf(MovimientoNotFoundException.class);
        assertThatThrownBy(() -> service.deleteDefinitivo(USUARIO, 6L))
                .isInstanceOf(MovimientoNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("vaciar delega en el puerto con el usuario")
    void vaciar() {
        when(repository.vaciarPapelera(USUARIO)).thenReturn(3);

        assertThat(service.vaciar(USUARIO)).isEqualTo(3);
    }

    @Test
    @DisplayName("purgar borra lo que entro en la papelera hace mas de 30 dias")
    void purgarCortaA30Dias() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 31, 0, 15);
        when(repository.purgarPapelera(LocalDateTime.of(2026, 10, 1, 0, 15))).thenReturn(2);

        assertThat(service.purgar(ahora)).isEqualTo(2);
    }
}
