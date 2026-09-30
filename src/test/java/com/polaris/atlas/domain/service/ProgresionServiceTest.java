package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.out.EjercicioRepositoryPort;
import com.polaris.atlas.application.out.ProgresionRepositoryPort;
import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.EjercicioNotFoundException;
import com.polaris.atlas.domain.model.ProgresionFilter;
import com.polaris.atlas.domain.model.ProgresionSesion;
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
 * La progresion se pide siempre para el usuario del JWT, el ejercicio tiene que
 * ser visible para el (catalogo o suyo; si no, 404 sin distinguir), el rango de
 * fechas tiene que ser coherente y los importes salen con escala 2. Ver
 * docs/decisiones/026-progresion-y-records-por-volumen.md.
 */
@ExtendWith(MockitoExtension.class)
class ProgresionServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;

    @Mock
    private ProgresionRepositoryPort repository;

    @Mock
    private EjercicioRepositoryPort ejercicioRepository;

    @InjectMocks
    private ProgresionService service;

    private static Ejercicio ejercicio(Long id, Long usuarioId) {
        return Ejercicio.builder().id(id).usuarioId(usuarioId).nombre("Press banca").grupoMuscular("Pecho").build();
    }

    private static ProgresionFilter filtro(Long ejercicioId, LocalDate desde, LocalDate hasta) {
        return ProgresionFilter.builder().ejercicioId(ejercicioId).desde(desde).hasta(hasta).build();
    }

    private static ProgresionSesion sesion(Long id, LocalDate fecha, String volumen, int series, String peso, long reps) {
        return ProgresionSesion.builder().sesionId(id).fecha(fecha).volumen(new BigDecimal(volumen))
                .numeroSeries(series).pesoMaximo(new BigDecimal(peso)).repsTotales(reps).build();
    }

    @Test
    @DisplayName("devuelve las sesiones del repositorio en su orden, con escala 2, y lo pide para el usuario y el filtro dados")
    void devuelveLasFilas() {
        when(ejercicioRepository.findById(5L)).thenReturn(Optional.of(ejercicio(5L, null)));
        ProgresionFilter filtro = filtro(5L, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));
        when(repository.findProgresion(USUARIO, filtro)).thenReturn(List.of(
                sesion(10L, LocalDate.of(2026, 9, 2), "2400", 3, "80", 30),
                sesion(11L, LocalDate.of(2026, 9, 9), "2550.5", 3, "82.5", 30)));

        List<ProgresionSesion> resultado = service.get(USUARIO, filtro);

        assertThat(resultado).extracting(ProgresionSesion::getSesionId).containsExactly(10L, 11L);
        assertThat(resultado.get(0).getVolumen()).isEqualByComparingTo("2400").hasScaleOf(2);
        assertThat(resultado.get(0).getPesoMaximo()).isEqualByComparingTo("80").hasScaleOf(2);
        assertThat(resultado.get(1).getVolumen()).isEqualByComparingTo("2550.50").hasScaleOf(2);
        assertThat(resultado.get(1).getNumeroSeries()).isEqualTo(3);
        assertThat(resultado.get(1).getRepsTotales()).isEqualTo(30L);
    }

    @Test
    @DisplayName("un ejercicio sin series en el rango da una lista vacia, no un error")
    void sinDatos() {
        when(ejercicioRepository.findById(5L)).thenReturn(Optional.of(ejercicio(5L, USUARIO)));
        when(repository.findProgresion(any(), any())).thenReturn(List.of());

        assertThat(service.get(USUARIO, filtro(5L, null, null))).isEmpty();
    }

    @Test
    @DisplayName("un ejercicio del catalogo y uno propio son visibles")
    void ejerciciosVisibles() {
        when(ejercicioRepository.findById(5L)).thenReturn(Optional.of(ejercicio(5L, null)));
        when(ejercicioRepository.findById(6L)).thenReturn(Optional.of(ejercicio(6L, USUARIO)));
        when(repository.findProgresion(any(), any())).thenReturn(List.of());

        assertThat(service.get(USUARIO, filtro(5L, null, null))).isEmpty();
        assertThat(service.get(USUARIO, filtro(6L, null, null))).isEmpty();
    }

    @Test
    @DisplayName("un ejercicio inexistente da 404")
    void ejercicioInexistente() {
        when(ejercicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(USUARIO, filtro(99L, null, null)))
                .isInstanceOf(EjercicioNotFoundException.class);
        verify(repository, never()).findProgresion(any(), any());
    }

    @Test
    @DisplayName("el ejercicio propio de otro usuario da el mismo 404: no se confirma que el id existe")
    void ejercicioAjeno() {
        when(ejercicioRepository.findById(7L)).thenReturn(Optional.of(ejercicio(7L, OTRO_USUARIO)));

        assertThatThrownBy(() -> service.get(USUARIO, filtro(7L, null, null)))
                .isInstanceOf(EjercicioNotFoundException.class)
                .hasMessage("Ejercicio no encontrado: 7");
        verify(repository, never()).findProgresion(any(), any());
    }

    @Test
    @DisplayName("sin ejercicioId da 400")
    void ejercicioObligatorio() {
        assertThatThrownBy(() -> service.get(USUARIO, filtro(null, null, null)))
                .isInstanceOf(ValidationException.class)
                .hasMessage("El ejercicio es obligatorio");
        assertThatThrownBy(() -> service.get(USUARIO, null)).isInstanceOf(ValidationException.class);
        verify(repository, never()).findProgresion(any(), any());
    }

    @Test
    @DisplayName("desde posterior a hasta da 400 con un mensaje legible; desde igual a hasta vale")
    void rangoDeFechas() {
        LocalDate dia = LocalDate.of(2026, 9, 15);

        assertThatThrownBy(() -> service.get(USUARIO, filtro(5L, dia.plusDays(1), dia)))
                .isInstanceOf(ValidationException.class)
                .hasMessage("La fecha 'desde' no puede ser posterior a 'hasta'");
        verify(repository, never()).findProgresion(any(), any());

        when(ejercicioRepository.findById(5L)).thenReturn(Optional.of(ejercicio(5L, null)));
        when(repository.findProgresion(any(), any())).thenReturn(List.of());
        assertThat(service.get(USUARIO, filtro(5L, dia, dia))).isEmpty();
    }

    @Test
    @DisplayName("un solo extremo (solo desde o solo hasta) es valido")
    void extremosSueltos() {
        when(ejercicioRepository.findById(5L)).thenReturn(Optional.of(ejercicio(5L, null)));
        when(repository.findProgresion(any(), any())).thenReturn(List.of());

        assertThat(service.get(USUARIO, filtro(5L, LocalDate.of(2026, 1, 1), null))).isEmpty();
        assertThat(service.get(USUARIO, filtro(5L, null, LocalDate.of(2026, 1, 1)))).isEmpty();
    }

    @Test
    @DisplayName("el volumen de una serie con peso 0 (peso corporal) llega como 0.00")
    void pesoCorporalVolumenCero() {
        when(ejercicioRepository.findById(5L)).thenReturn(Optional.of(ejercicio(5L, USUARIO)));
        when(repository.findProgresion(any(), any())).thenReturn(List.of(
                sesion(10L, LocalDate.of(2026, 9, 2), "0", 3, "0", 30)));

        ProgresionSesion punto = service.get(USUARIO, filtro(5L, null, null)).get(0);

        assertThat(punto.getVolumen()).isEqualByComparingTo("0").hasScaleOf(2);
        assertThat(punto.getRepsTotales()).isEqualTo(30L);
    }
}
