package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.out.CategoriaRepositoryPort;
import com.polaris.kuiper.application.out.CuentaRepositoryPort;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.RecurrenteRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaNotFoundException;
import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.domain.model.CuentaNotFoundException;
import com.polaris.kuiper.domain.model.FrecuenciaRecurrente;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.domain.model.RecurrenteNotFoundException;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo que mas importa: que el job genere un movimiento por cada cargo atrasado
 * (ni uno mas ni uno menos), que respete el dia de cobro aunque el mes sea
 * corto, que los plazos se cierren solos y que reactivar no cobre la pausa.
 * Ver docs/decisiones/034-recurrente-genera-movimientos.md.
 */
@ExtendWith(MockitoExtension.class)
class RecurrenteServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;

    @Mock
    private RecurrenteRepositoryPort repository;

    @Mock
    private CategoriaRepositoryPort categoriaRepository;

    @Mock
    private MovimientoRepositoryPort movimientoRepository;

    @Mock
    private CuentaRepositoryPort cuentaRepository;

    @InjectMocks
    private RecurrenteService service;

    private static Recurrente recurrente(FrecuenciaRecurrente frecuencia, LocalDate inicio) {
        return Recurrente.builder().usuarioId(USUARIO).concepto("Netflix").importe(new BigDecimal("12.99"))
                .tipo(TipoMovimiento.GASTO).categoriaId(10L).frecuencia(frecuencia).fechaInicio(inicio)
                .proximaFecha(inicio).activo(true).build();
    }

    private void categoriaDeGasto() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(
                Categoria.builder().id(10L).usuarioId(USUARIO).nombre("Ocio").tipo(TipoMovimiento.GASTO).build()));
    }

    @Test
    @DisplayName("Mensual: el dia 31 cae el 30 en abril, el 28 en febrero y vuelve al 31")
    void mensualConservaElDiaDeCobro() {
        Recurrente r = recurrente(FrecuenciaRecurrente.MENSUAL, LocalDate.of(2026, 1, 31));

        assertThat(r.siguienteDespuesDe(LocalDate.of(2026, 1, 31))).isEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(r.siguienteDespuesDe(LocalDate.of(2026, 2, 28))).isEqualTo(LocalDate.of(2026, 3, 31));
        assertThat(r.siguienteDespuesDe(LocalDate.of(2026, 3, 31))).isEqualTo(LocalDate.of(2026, 4, 30));
    }

    @Test
    @DisplayName("Anual del 29 de febrero cae el 28 los años no bisiestos; semanal suma 7 dias")
    void anualYSemanal() {
        Recurrente anual = recurrente(FrecuenciaRecurrente.ANUAL, LocalDate.of(2028, 2, 29));
        Recurrente semanal = recurrente(FrecuenciaRecurrente.SEMANAL, LocalDate.of(2026, 9, 28));

        assertThat(anual.siguienteDespuesDe(LocalDate.of(2028, 2, 29))).isEqualTo(LocalDate.of(2029, 2, 28));
        assertThat(anual.siguienteDespuesDe(LocalDate.of(2031, 2, 28))).isEqualTo(LocalDate.of(2032, 2, 29));
        assertThat(semanal.siguienteDespuesDe(LocalDate.of(2026, 9, 28))).isEqualTo(LocalDate.of(2026, 10, 5));
    }

    @Test
    @DisplayName("create fija usuario, cero cuotas pagadas y el primer cargo en fechaInicio")
    void createFijaEstadoInicial() {
        categoriaDeGasto();
        when(repository.save(any(Recurrente.class))).thenAnswer(inv -> inv.getArgument(0));
        Recurrente nuevo = recurrente(FrecuenciaRecurrente.MENSUAL, LocalDate.of(2026, 9, 5));
        nuevo.setId(99L);
        nuevo.setUsuarioId(OTRO_USUARIO);
        nuevo.setCuotasPagadas(7);
        nuevo.setProximaFecha(LocalDate.of(2030, 1, 1));

        Recurrente creado = service.create(USUARIO, nuevo);

        assertThat(creado.getId()).isNull();
        assertThat(creado.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(creado.getCuotasPagadas()).isZero();
        assertThat(creado.getProximaFecha()).isEqualTo(LocalDate.of(2026, 9, 5));
    }

    @Test
    @DisplayName("create rechaza una categoria de otro tipo (400) o de otro usuario (404)")
    void createValidaCategoria() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(
                Categoria.builder().id(10L).usuarioId(USUARIO).tipo(TipoMovimiento.INGRESO).build()));
        assertThatThrownBy(() -> service.create(USUARIO, recurrente(FrecuenciaRecurrente.MENSUAL, LocalDate.now())))
                .isInstanceOf(ValidationException.class);

        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(
                Categoria.builder().id(10L).usuarioId(OTRO_USUARIO).tipo(TipoMovimiento.GASTO).build()));
        assertThatThrownBy(() -> service.create(USUARIO, recurrente(FrecuenciaRecurrente.MENSUAL, LocalDate.now())))
                .isInstanceOf(CategoriaNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("generar crea un movimiento por cada cargo atrasado y deja la proxima fecha en el futuro")
    void generarRecuperaLosAtrasados() {
        Recurrente r = recurrente(FrecuenciaRecurrente.MENSUAL, LocalDate.of(2026, 7, 5));
        when(repository.findPendientes(LocalDate.of(2026, 9, 10))).thenReturn(List.of(r));

        int creados = service.generar(LocalDate.of(2026, 9, 10));

        ArgumentCaptor<Movimiento> captor = ArgumentCaptor.forClass(Movimiento.class);
        verify(movimientoRepository, times(3)).save(captor.capture());
        assertThat(creados).isEqualTo(3);
        assertThat(captor.getAllValues()).extracting(Movimiento::getFecha).containsExactly(
                LocalDate.of(2026, 7, 5), LocalDate.of(2026, 8, 5), LocalDate.of(2026, 9, 5));
        assertThat(captor.getAllValues()).allSatisfy(m -> {
            assertThat(m.isRecurrente()).isTrue();
            assertThat(m.getUsuarioId()).isEqualTo(USUARIO);
            assertThat(m.getConcepto()).isEqualTo("Netflix");
        });
        assertThat(r.getProximaFecha()).isEqualTo(LocalDate.of(2026, 10, 5));
        assertThat(r.getCuotasPagadas()).isEqualTo(3);
        verify(repository).save(r);
    }

    @Test
    @DisplayName("Con plazos, numera el concepto y se desactiva al pagar la ultima cuota")
    void plazosSeCierranSolos() {
        Recurrente r = recurrente(FrecuenciaRecurrente.MENSUAL, LocalDate.of(2026, 7, 1));
        r.setConcepto("Movil");
        r.setCuotasTotal(2);
        when(repository.findPendientes(any())).thenReturn(List.of(r));

        int creados = service.generar(LocalDate.of(2026, 12, 1));

        ArgumentCaptor<Movimiento> captor = ArgumentCaptor.forClass(Movimiento.class);
        verify(movimientoRepository, times(2)).save(captor.capture());
        assertThat(creados).isEqualTo(2);
        assertThat(captor.getAllValues()).extracting(Movimiento::getConcepto)
                .containsExactly("Movil (1/2)", "Movil (2/2)");
        assertThat(r.isActivo()).isFalse();
        assertThat(r.getCuotasPagadas()).isEqualTo(2);
    }

    @Test
    @DisplayName("update conserva cuotas y proximo cargo si no cambia la fecha de inicio")
    void updateConservaElProximoCargo() {
        Recurrente existente = recurrente(FrecuenciaRecurrente.MENSUAL, LocalDate.of(2026, 1, 5));
        existente.setId(3L);
        existente.setCuotasPagadas(9);
        existente.setProximaFecha(LocalDate.of(2026, 10, 5));
        when(repository.findById(3L)).thenReturn(Optional.of(existente));
        categoriaDeGasto();
        when(repository.save(any(Recurrente.class))).thenAnswer(inv -> inv.getArgument(0));

        Recurrente cambios = recurrente(FrecuenciaRecurrente.MENSUAL, LocalDate.of(2026, 1, 5));
        cambios.setImporte(new BigDecimal("15.99"));
        Recurrente actualizado = service.update(USUARIO, 3L, cambios);

        assertThat(actualizado.getCuotasPagadas()).isEqualTo(9);
        assertThat(actualizado.getProximaFecha()).isEqualTo(LocalDate.of(2026, 10, 5));
        assertThat(actualizado.getImporte()).isEqualByComparingTo("15.99");
    }

    @Test
    @DisplayName("Reactivar un recurrente pausado no cobra la pausa: el proximo cargo es el primero desde hoy")
    void reactivarNoCobraLaPausa() {
        Recurrente existente = recurrente(FrecuenciaRecurrente.SEMANAL, LocalDate.of(2020, 1, 6));
        existente.setId(3L);
        existente.setActivo(false);
        existente.setProximaFecha(LocalDate.of(2020, 3, 2));
        when(repository.findById(3L)).thenReturn(Optional.of(existente));
        categoriaDeGasto();
        when(repository.save(any(Recurrente.class))).thenAnswer(inv -> inv.getArgument(0));

        Recurrente actualizado = service.update(USUARIO, 3L,
                recurrente(FrecuenciaRecurrente.SEMANAL, LocalDate.of(2020, 1, 6)));

        assertThat(actualizado.isActivo()).isTrue();
        assertThat(actualizado.getProximaFecha()).isAfterOrEqualTo(LocalDate.now())
                .isBefore(LocalDate.now().plusWeeks(1));
    }

    @Test
    @DisplayName("Un recurrente de otro usuario da 404 al leer, editar y borrar")
    void aislamientoEntreUsuarios() {
        Recurrente ajeno = recurrente(FrecuenciaRecurrente.MENSUAL, LocalDate.of(2026, 1, 1));
        ajeno.setId(3L);
        ajeno.setUsuarioId(OTRO_USUARIO);
        when(repository.findById(3L)).thenReturn(Optional.of(ajeno));

        assertThatThrownBy(() -> service.get(USUARIO, 3L)).isInstanceOf(RecurrenteNotFoundException.class);
        assertThatThrownBy(() -> service.update(USUARIO, 3L, ajeno)).isInstanceOf(RecurrenteNotFoundException.class);
        assertThatThrownBy(() -> service.delete(USUARIO, 3L)).isInstanceOf(RecurrenteNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Los movimientos generados heredan la cuenta del recurrente")
    void generarHeredaLaCuenta() {
        Recurrente r = recurrente(FrecuenciaRecurrente.MENSUAL, LocalDate.of(2026, 9, 5));
        r.setCuentaId(3L);
        when(repository.findPendientes(LocalDate.of(2026, 9, 10))).thenReturn(List.of(r));

        service.generar(LocalDate.of(2026, 9, 10));

        ArgumentCaptor<Movimiento> captor = ArgumentCaptor.forClass(Movimiento.class);
        verify(movimientoRepository).save(captor.capture());
        assertThat(captor.getValue().getCuentaId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("create rechaza con 404 una cuenta de otro usuario y acepta una propia")
    void createValidaCuenta() {
        categoriaDeGasto();
        when(cuentaRepository.findById(3L))
                .thenReturn(Optional.of(Cuenta.builder().id(3L).usuarioId(OTRO_USUARIO).build()));
        when(cuentaRepository.findById(4L)).thenReturn(Optional.of(Cuenta.builder().id(4L).usuarioId(USUARIO).build()));
        when(repository.save(any(Recurrente.class))).thenAnswer(inv -> inv.getArgument(0));

        Recurrente ajena = recurrente(FrecuenciaRecurrente.MENSUAL, LocalDate.of(2026, 9, 5));
        ajena.setCuentaId(3L);
        Recurrente propia = recurrente(FrecuenciaRecurrente.MENSUAL, LocalDate.of(2026, 9, 5));
        propia.setCuentaId(4L);

        assertThatThrownBy(() -> service.create(USUARIO, ajena)).isInstanceOf(CuentaNotFoundException.class);
        assertThat(service.create(USUARIO, propia).getCuentaId()).isEqualTo(4L);
    }
}
