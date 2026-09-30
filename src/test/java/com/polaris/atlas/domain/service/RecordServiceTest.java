package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.out.RecordRepositoryPort;
import com.polaris.atlas.domain.model.MejorPesoEjercicio;
import com.polaris.atlas.domain.model.RecordEjercicio;
import com.polaris.atlas.domain.model.VolumenSesionEjercicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;

/**
 * Un record es el mayor peso en una serie (con sus reps y fecha) y el mayor
 * volumen en una sola sesion (con su fecha). La base agrega; el servicio une,
 * elige la mejor sesion por ejercicio (con empate gana la primera) y omite un
 * volumen maximo de 0. Todo sale con escala 2. Ver
 * docs/decisiones/026-progresion-y-records-por-volumen.md.
 */
@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    private static final Long USUARIO = 1L;

    @Mock
    private RecordRepositoryPort repository;

    @InjectMocks
    private RecordService service;

    private static MejorPesoEjercicio peso(Long ejercicioId, String nombre, String kg, int reps, LocalDate fecha) {
        return MejorPesoEjercicio.builder().ejercicioId(ejercicioId).ejercicioNombre(nombre)
                .ejercicioGrupoMuscular("Pecho").pesoKg(new BigDecimal(kg)).reps(reps).fecha(fecha).build();
    }

    private static VolumenSesionEjercicio volumen(Long ejercicioId, Long sesionId, LocalDate fecha, String valor) {
        return VolumenSesionEjercicio.builder().ejercicioId(ejercicioId).sesionId(sesionId).fecha(fecha)
                .volumen(new BigDecimal(valor)).build();
    }

    @Test
    @DisplayName("sin series no hay records: lista vacia y ni se pide el volumen")
    void sinDatos() {
        when(repository.findMejorPesoPorEjercicio(USUARIO)).thenReturn(List.of());

        assertThat(service.list(USUARIO)).isEmpty();
        verify(repository, never()).findVolumenPorEjercicioYSesion(anyLong());
    }

    @Test
    @DisplayName("une el mejor peso con la sesion de mayor volumen de cada ejercicio, en el orden del repositorio (por nombre)")
    void unePesoYVolumen() {
        when(repository.findMejorPesoPorEjercicio(USUARIO)).thenReturn(List.of(
                peso(2L, "Press banca", "100", 5, LocalDate.of(2026, 9, 9)),
                peso(1L, "Sentadilla", "140.5", 3, LocalDate.of(2026, 9, 2))));
        when(repository.findVolumenPorEjercicioYSesion(USUARIO)).thenReturn(List.of(
                volumen(1L, 10L, LocalDate.of(2026, 9, 2), "4000"),
                volumen(2L, 10L, LocalDate.of(2026, 9, 2), "2400"),
                volumen(2L, 11L, LocalDate.of(2026, 9, 9), "3100.50"),
                volumen(1L, 12L, LocalDate.of(2026, 9, 16), "3900")));

        List<RecordEjercicio> records = service.list(USUARIO);

        assertThat(records).extracting(RecordEjercicio::getEjercicioNombre)
                .containsExactly("Press banca", "Sentadilla");
        RecordEjercicio press = records.get(0);
        assertThat(press.getEjercicioId()).isEqualTo(2L);
        assertThat(press.getEjercicioGrupoMuscular()).isEqualTo("Pecho");
        assertThat(press.getPesoMaximo()).isEqualByComparingTo("100").hasScaleOf(2);
        assertThat(press.getRepsPesoMaximo()).isEqualTo(5);
        assertThat(press.getFechaPesoMaximo()).isEqualTo(LocalDate.of(2026, 9, 9));
        assertThat(press.getVolumenMaximoSesion()).isEqualByComparingTo("3100.50").hasScaleOf(2);
        assertThat(press.getFechaVolumenMaximo()).isEqualTo(LocalDate.of(2026, 9, 9));

        RecordEjercicio sentadilla = records.get(1);
        assertThat(sentadilla.getPesoMaximo()).isEqualByComparingTo("140.50");
        assertThat(sentadilla.getVolumenMaximoSesion()).isEqualByComparingTo("4000").hasScaleOf(2);
        assertThat(sentadilla.getFechaVolumenMaximo()).isEqualTo(LocalDate.of(2026, 9, 2));
    }

    @Test
    @DisplayName("de las series del peso maximo se queda la de mas reps de cada ejercicio, llegue en el orden que llegue, con su fecha")
    void masRepsAlPesoMaximo() {
        when(repository.findMejorPesoPorEjercicio(USUARIO)).thenReturn(List.of(
                peso(2L, "Press banca", "100", 5, LocalDate.of(2026, 9, 8)),
                peso(2L, "Press banca", "100", 7, LocalDate.of(2026, 9, 22)),
                peso(2L, "Press banca", "100", 3, LocalDate.of(2026, 9, 1)),
                peso(1L, "Sentadilla", "140", 3, LocalDate.of(2026, 9, 2)),
                peso(1L, "Sentadilla", "140", 5, LocalDate.of(2026, 9, 9))));
        when(repository.findVolumenPorEjercicioYSesion(USUARIO)).thenReturn(List.of());

        List<RecordEjercicio> records = service.list(USUARIO);

        assertThat(records).hasSize(2);
        assertThat(records).extracting(RecordEjercicio::getEjercicioNombre).containsExactly("Press banca", "Sentadilla");
        assertThat(records.get(0).getRepsPesoMaximo()).isEqualTo(7);
        assertThat(records.get(0).getFechaPesoMaximo()).isEqualTo(LocalDate.of(2026, 9, 22));
        assertThat(records.get(1).getRepsPesoMaximo()).isEqualTo(5);
        assertThat(records.get(1).getFechaPesoMaximo()).isEqualTo(LocalDate.of(2026, 9, 9));
        assertThat(records.get(0).getVolumenMaximoSesion()).isNull();
    }

    @Test
    @DisplayName("con dos sesiones de igual volumen maximo gana la primera (cuando se logro por primera vez)")
    void empateDeVolumen() {
        when(repository.findMejorPesoPorEjercicio(USUARIO)).thenReturn(List.of(
                peso(1L, "Press banca", "80", 8, LocalDate.of(2026, 9, 2))));
        when(repository.findVolumenPorEjercicioYSesion(USUARIO)).thenReturn(List.of(
                volumen(1L, 10L, LocalDate.of(2026, 9, 2), "1920.00"),
                volumen(1L, 11L, LocalDate.of(2026, 9, 9), "1920.00")));

        RecordEjercicio record = service.list(USUARIO).get(0);

        assertThat(record.getFechaVolumenMaximo()).isEqualTo(LocalDate.of(2026, 9, 2));
    }

    @Test
    @DisplayName("un ejercicio solo con peso corporal sale con su marca de peso (0 kg, mas reps) y sin record de volumen")
    void soloPesoCorporal() {
        when(repository.findMejorPesoPorEjercicio(USUARIO)).thenReturn(List.of(
                peso(3L, "Dominadas", "0", 12, LocalDate.of(2026, 9, 9))));
        when(repository.findVolumenPorEjercicioYSesion(USUARIO)).thenReturn(List.of(
                volumen(3L, 10L, LocalDate.of(2026, 9, 2), "0.00"),
                volumen(3L, 11L, LocalDate.of(2026, 9, 9), "0.00")));

        RecordEjercicio record = service.list(USUARIO).get(0);

        assertThat(record.getPesoMaximo()).isEqualByComparingTo("0").hasScaleOf(2);
        assertThat(record.getRepsPesoMaximo()).isEqualTo(12);
        assertThat(record.getVolumenMaximoSesion()).isNull();
        assertThat(record.getFechaVolumenMaximo()).isNull();
    }

    @Test
    @DisplayName("todo se pide para el usuario del JWT")
    void soloElUsuarioActual() {
        when(repository.findMejorPesoPorEjercicio(USUARIO)).thenReturn(List.of(
                peso(1L, "Press banca", "80", 8, LocalDate.of(2026, 9, 2))));
        when(repository.findVolumenPorEjercicioYSesion(USUARIO)).thenReturn(List.of());

        service.list(USUARIO);

        verify(repository).findMejorPesoPorEjercicio(USUARIO);
        verify(repository).findVolumenPorEjercicioYSesion(USUARIO);
    }
}
