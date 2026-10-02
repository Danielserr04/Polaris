package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.out.CuentaRepositoryPort;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.RecurrenteRepositoryPort;
import com.polaris.kuiper.application.out.TransferenciaRepositoryPort;
import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.domain.model.CuentaFilter;
import com.polaris.kuiper.domain.model.CuentaNotFoundException;
import com.polaris.kuiper.domain.model.Patrimonio;
import com.polaris.kuiper.domain.model.TipoCuenta;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Lo que mas importa: que el saldo actual sea saldoInicial + ingresos - gastos
 * + entrantes - salientes con las sumas de los puertos, que el nombre sea
 * unico, que una cuenta en uso no se borre y que un usuario nunca vea la
 * cuenta de otro. Ver docs/decisiones/039-cuentas-y-transferencias.md.
 */
@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;

    @Mock
    private CuentaRepositoryPort repository;

    @Mock
    private MovimientoRepositoryPort movimientoRepository;

    @Mock
    private RecurrenteRepositoryPort recurrenteRepository;

    @Mock
    private TransferenciaRepositoryPort transferenciaRepository;

    @InjectMocks
    private CuentaService service;

    private static Cuenta cuenta(Long id, Long usuarioId, String nombre, String saldoInicial) {
        return Cuenta.builder().id(id).usuarioId(usuarioId).nombre(nombre).tipo(TipoCuenta.CORRIENTE)
                .saldoInicial(new BigDecimal(saldoInicial)).build();
    }

    private void sumas(Map<Long, BigDecimal> ingresos, Map<Long, BigDecimal> gastos,
                       Map<Long, BigDecimal> entrantes, Map<Long, BigDecimal> salientes) {
        when(movimientoRepository.sumarPorCuenta(USUARIO, TipoMovimiento.INGRESO)).thenReturn(ingresos);
        when(movimientoRepository.sumarPorCuenta(USUARIO, TipoMovimiento.GASTO)).thenReturn(gastos);
        when(transferenciaRepository.sumarEntrantesPorCuenta(USUARIO)).thenReturn(entrantes);
        when(transferenciaRepository.sumarSalientesPorCuenta(USUARIO)).thenReturn(salientes);
    }

    @Test
    @DisplayName("list calcula el saldo de cada cuenta con cuatro sumas, sin una consulta por cuenta")
    void listCalculaSaldos() {
        when(repository.findAll(USUARIO, null)).thenReturn(List.of(
                cuenta(1L, USUARIO, "ING", "100"), cuenta(2L, USUARIO, "Hucha", "-50.5"),
                cuenta(3L, USUARIO, "Vacia", "0")));
        sumas(Map.of(1L, new BigDecimal("1500.00")),
                Map.of(1L, new BigDecimal("300.25"), 2L, new BigDecimal("10.00")),
                Map.of(2L, new BigDecimal("200.00")),
                Map.of(1L, new BigDecimal("200.00")));

        List<Cuenta> cuentas = service.list(USUARIO, null);

        // 100 + 1500 - 300.25 - 200 = 1099.75 ; -50.5 - 10 + 200 = 139.50 ; 0.00
        assertThat(cuentas).extracting(c -> c.getSaldoActual().toPlainString())
                .containsExactly("1099.75", "139.50", "0.00");
    }

    @Test
    @DisplayName("list sin cuentas no lanza las sumas")
    void listVacioNoSuma() {
        when(repository.findAll(USUARIO, null)).thenReturn(List.of());

        assertThat(service.list(USUARIO, null)).isEmpty();
        verifyNoInteractions(movimientoRepository, transferenciaRepository);
    }

    @Test
    @DisplayName("get devuelve la cuenta con su saldo; la de otro usuario da 404")
    void getConSaldoYAislamiento() {
        when(repository.findById(1L)).thenReturn(Optional.of(cuenta(1L, USUARIO, "ING", "10")));
        when(repository.findById(9L)).thenReturn(Optional.of(cuenta(9L, OTRO_USUARIO, "Ajena", "10")));
        sumas(Map.of(1L, new BigDecimal("5.00")), Map.of(), Map.of(), Map.of());

        assertThat(service.get(USUARIO, 1L).getSaldoActual()).isEqualByComparingTo("15.00");
        assertThatThrownBy(() -> service.get(USUARIO, 9L)).isInstanceOf(CuentaNotFoundException.class);
        assertThatThrownBy(() -> service.get(USUARIO, 404L)).isInstanceOf(CuentaNotFoundException.class);
    }

    @Test
    @DisplayName("create fija el usuario del JWT, anula el id y el saldo actual es el inicial")
    void createFijaUsuario() {
        when(repository.findByUsuarioIdAndNombre(USUARIO, "ING")).thenReturn(Optional.empty());
        when(repository.save(any(Cuenta.class))).thenAnswer(inv -> inv.getArgument(0));

        Cuenta creada = service.create(USUARIO, cuenta(99L, OTRO_USUARIO, "ING", "250"));

        assertThat(creada.getId()).isNull();
        assertThat(creada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(creada.getSaldoActual().toPlainString()).isEqualTo("250.00");
    }

    @Test
    @DisplayName("create y update dan 409 si otra cuenta tuya ya usa el nombre; renombrar a si misma vale")
    void nombreUnico() {
        when(repository.findByUsuarioIdAndNombre(USUARIO, "ING")).thenReturn(Optional.of(cuenta(1L, USUARIO, "ING", "0")));
        when(repository.findById(1L)).thenReturn(Optional.of(cuenta(1L, USUARIO, "ING", "0")));
        when(repository.findById(2L)).thenReturn(Optional.of(cuenta(2L, USUARIO, "Otra", "0")));
        when(repository.save(any(Cuenta.class))).thenAnswer(inv -> inv.getArgument(0));
        sumas(Map.of(), Map.of(), Map.of(), Map.of());

        assertThatThrownBy(() -> service.create(USUARIO, cuenta(null, null, "ING", "0")))
                .isInstanceOf(DuplicateResourceException.class);
        assertThatThrownBy(() -> service.update(USUARIO, 2L, cuenta(null, null, "ING", "0")))
                .isInstanceOf(DuplicateResourceException.class);
        assertThat(service.update(USUARIO, 1L, cuenta(null, null, "ING", "7")).getSaldoActual())
                .isEqualByComparingTo("7");
    }

    @Test
    @DisplayName("update archiva sin perder id ni usuario")
    void updateArchiva() {
        when(repository.findById(1L)).thenReturn(Optional.of(cuenta(1L, USUARIO, "ING", "0")));
        when(repository.findByUsuarioIdAndNombre(USUARIO, "ING")).thenReturn(Optional.of(cuenta(1L, USUARIO, "ING", "0")));
        when(repository.save(any(Cuenta.class))).thenAnswer(inv -> inv.getArgument(0));
        sumas(Map.of(), Map.of(), Map.of(), Map.of());
        Cuenta cambios = cuenta(null, OTRO_USUARIO, "ING", "0");
        cambios.setArchivada(true);

        Cuenta actualizada = service.update(USUARIO, 1L, cambios);

        assertThat(actualizada.getId()).isEqualTo(1L);
        assertThat(actualizada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(actualizada.isArchivada()).isTrue();
    }

    @Test
    @DisplayName("delete da 400 si la cuenta tiene movimientos, recurrentes o transferencias")
    void deleteEnUso() {
        when(repository.findById(1L)).thenReturn(Optional.of(cuenta(1L, USUARIO, "ING", "0")));

        when(movimientoRepository.existsByCuentaId(1L)).thenReturn(true);
        assertThatThrownBy(() -> service.delete(USUARIO, 1L)).isInstanceOf(ValidationException.class);

        when(movimientoRepository.existsByCuentaId(1L)).thenReturn(false);
        when(recurrenteRepository.existsByCuentaId(1L)).thenReturn(true);
        assertThatThrownBy(() -> service.delete(USUARIO, 1L)).isInstanceOf(ValidationException.class);

        when(recurrenteRepository.existsByCuentaId(1L)).thenReturn(false);
        when(transferenciaRepository.existsByCuentaId(1L)).thenReturn(true);
        assertThatThrownBy(() -> service.delete(USUARIO, 1L)).isInstanceOf(ValidationException.class);

        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete borra una cuenta propia sin uso; la de otro usuario da 404")
    void deleteSinUso() {
        when(repository.findById(1L)).thenReturn(Optional.of(cuenta(1L, USUARIO, "ING", "0")));
        when(repository.findById(9L)).thenReturn(Optional.of(cuenta(9L, OTRO_USUARIO, "Ajena", "0")));

        service.delete(USUARIO, 1L);

        verify(repository).deleteById(1L);
        assertThatThrownBy(() -> service.delete(USUARIO, 9L)).isInstanceOf(CuentaNotFoundException.class);
        verify(repository, never()).deleteById(9L);
    }

    @Test
    @DisplayName("El patrimonio suma el saldo actual de todas las cuentas, archivadas incluidas")
    void patrimonio() {
        Cuenta archivada = cuenta(2L, USUARIO, "Vieja", "30");
        archivada.setArchivada(true);
        when(repository.findAll(any(), any(CuentaFilter.class)))
                .thenReturn(List.of(cuenta(1L, USUARIO, "ING", "100"), archivada));
        sumas(Map.of(), Map.of(1L, new BigDecimal("40.00")), Map.of(), Map.of());

        Patrimonio patrimonio = service.get(USUARIO);

        assertThat(patrimonio.getTotal().toPlainString()).isEqualTo("90.00");
        assertThat(patrimonio.getNumeroCuentas()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sin cuentas el patrimonio es 0.00")
    void patrimonioVacio() {
        when(repository.findAll(any(), any(CuentaFilter.class))).thenReturn(List.of());

        assertThat(service.get(USUARIO).getTotal().toPlainString()).isEqualTo("0.00");
    }
}
