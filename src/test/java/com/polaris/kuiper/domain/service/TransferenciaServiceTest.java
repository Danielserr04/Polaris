package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.out.CuentaRepositoryPort;
import com.polaris.kuiper.application.out.TransferenciaRepositoryPort;
import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.domain.model.CuentaNotFoundException;
import com.polaris.kuiper.domain.model.Transferencia;
import com.polaris.kuiper.domain.model.TransferenciaFilter;
import com.polaris.kuiper.domain.model.TransferenciaNotFoundException;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Lo que mas importa: las dos cuentas son del usuario y distintas, y un
 * usuario nunca ve ni toca la transferencia de otro. Ver
 * docs/decisiones/039-cuentas-y-transferencias.md.
 */
@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;

    @Mock
    private TransferenciaRepositoryPort repository;

    @Mock
    private CuentaRepositoryPort cuentaRepository;

    @InjectMocks
    private TransferenciaService service;

    private static Transferencia transferencia(Long id, Long usuarioId, Long origen, Long destino) {
        return Transferencia.builder().id(id).usuarioId(usuarioId).cuentaOrigenId(origen).cuentaDestinoId(destino)
                .importe(new BigDecimal("200.00")).fecha(LocalDate.of(2026, 10, 1)).concepto("Ahorro").build();
    }

    private void cuentaDe(Long cuentaId, Long usuarioId) {
        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(Cuenta.builder().id(cuentaId).usuarioId(usuarioId).build()));
    }

    @Test
    @DisplayName("create fija el usuario del JWT y anula el id")
    void createFijaUsuario() {
        cuentaDe(1L, USUARIO);
        cuentaDe(2L, USUARIO);
        when(repository.save(any(Transferencia.class))).thenAnswer(inv -> inv.getArgument(0));

        Transferencia creada = service.create(USUARIO, transferencia(99L, OTRO_USUARIO, 1L, 2L));

        assertThat(creada.getId()).isNull();
        assertThat(creada.getUsuarioId()).isEqualTo(USUARIO);
    }

    @Test
    @DisplayName("create da 400 si origen y destino son la misma cuenta, sin mirar las cuentas")
    void mismaCuenta() {
        assertThatThrownBy(() -> service.create(USUARIO, transferencia(null, null, 1L, 1L)))
                .isInstanceOf(ValidationException.class);
        verifyNoInteractions(cuentaRepository);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create da 404 si el origen o el destino son de otro usuario o no existen")
    void cuentaAjena() {
        cuentaDe(1L, USUARIO);
        cuentaDe(2L, OTRO_USUARIO);
        when(cuentaRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(USUARIO, transferencia(null, null, 1L, 2L)))
                .isInstanceOf(CuentaNotFoundException.class);
        assertThatThrownBy(() -> service.create(USUARIO, transferencia(null, null, 3L, 1L)))
                .isInstanceOf(CuentaNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update conserva id y usuario y vuelve a validar las cuentas")
    void update() {
        when(repository.findById(5L)).thenReturn(Optional.of(transferencia(5L, USUARIO, 1L, 2L)));
        cuentaDe(2L, USUARIO);
        cuentaDe(1L, USUARIO);
        when(repository.save(any(Transferencia.class))).thenAnswer(inv -> inv.getArgument(0));

        Transferencia actualizada = service.update(USUARIO, 5L, transferencia(null, OTRO_USUARIO, 2L, 1L));

        assertThat(actualizada.getId()).isEqualTo(5L);
        assertThat(actualizada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(actualizada.getCuentaOrigenId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Una transferencia de otro usuario da 404 al leer, editar y borrar")
    void aislamiento() {
        when(repository.findById(5L)).thenReturn(Optional.of(transferencia(5L, OTRO_USUARIO, 1L, 2L)));

        assertThatThrownBy(() -> service.get(USUARIO, 5L)).isInstanceOf(TransferenciaNotFoundException.class);
        assertThatThrownBy(() -> service.update(USUARIO, 5L, transferencia(null, null, 1L, 2L)))
                .isInstanceOf(TransferenciaNotFoundException.class);
        assertThatThrownBy(() -> service.delete(USUARIO, 5L)).isInstanceOf(TransferenciaNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete borra la propia y list delega en el puerto con el usuario")
    void deleteYList() {
        when(repository.findById(5L)).thenReturn(Optional.of(transferencia(5L, USUARIO, 1L, 2L)));
        TransferenciaFilter filtro = TransferenciaFilter.builder().cuentaId(1L).build();
        when(repository.findAll(USUARIO, filtro)).thenReturn(List.of(transferencia(5L, USUARIO, 1L, 2L)));

        service.delete(USUARIO, 5L);

        verify(repository).deleteById(5L);
        assertThat(service.list(USUARIO, filtro)).hasSize(1);
    }
}
