package com.polaris.shared.logro;

import com.polaris.shared.logro.CalculoLogros.FechaImporte;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Las cuentas comunes de los logros: progreso, fecha de conseguido, rachas y unidades enteras. */
class CalculoLogrosTest {

    private static final LocalDate D1 = LocalDate.of(2026, 3, 1);
    private static final Logro TRES = Logro.definicion("TRES", "Tres", "Llega a 3", "star", NivelLogro.BRONCE, "cosas", 3);

    @Test
    @DisplayName("la fecha es la del hito que llega al objetivo, aunque lleguen desordenados")
    void fechaDelHitoQueAlcanza() {
        Logro l = CalculoLogros.evaluar(TRES, List.of(
                new Hito(D1.plusDays(9), 1), new Hito(D1, 1), new Hito(D1.plusDays(4), 1), new Hito(D1.plusDays(2), 1)));

        assertThat(l.getProgreso()).isEqualTo(4);
        assertThat(l.isConseguido()).isTrue();
        assertThat(l.getFechaConseguido()).isEqualTo(D1.plusDays(4));
        // La definicion del catalogo no se toca.
        assertThat(TRES.getProgreso()).isZero();
    }

    @Test
    @DisplayName("sin llegar al objetivo no hay fecha; un hito sin fecha cuenta pero deja el logro sin dia")
    void sinFecha() {
        assertThat(CalculoLogros.evaluar(TRES, List.of(new Hito(D1, 2))).getFechaConseguido()).isNull();

        Logro conNulo = CalculoLogros.evaluar(TRES, List.of(new Hito(null, 1), new Hito(D1, 2)));
        assertThat(conNulo.isConseguido()).isTrue();
        assertThat(conNulo.getFechaConseguido()).isNull();

        // Los que tienen fecha van antes: aqui ya llegan solos a 3.
        Logro fechadosPrimero = CalculoLogros.evaluar(TRES, List.of(new Hito(null, 5), new Hito(D1, 3)));
        assertThat(fechadosPrimero.getProgreso()).isEqualTo(8);
        assertThat(fechadosPrimero.getFechaConseguido()).isEqualTo(D1);
    }

    @Test
    @DisplayName("dias distintos: el mismo dia dos veces cuenta una")
    void diasDistintos() {
        assertThat(CalculoLogros.diasDistintos(List.of(D1, D1, D1.plusDays(1)))).hasSize(2);
        assertThat(CalculoLogros.unoPorFecha(List.of(D1, D1, D1.plusDays(1)))).hasSize(3);
    }

    @Test
    @DisplayName("racha de dias: el k-esimo hito es el dia en que la mejor racha llego a k")
    void rachaDias() {
        List<LocalDate> fechas = new ArrayList<>(List.of(D1, D1.plusDays(1), D1.plusDays(5)));
        for (int i = 10; i < 14; i++) {
            fechas.add(D1.plusDays(i));
        }

        List<Hito> racha = CalculoLogros.rachaMasLarga(fechas, ChronoUnit.DAYS);

        assertThat(racha).hasSize(4);
        assertThat(racha).extracting(Hito::fecha)
                .containsExactly(D1, D1.plusDays(1), D1.plusDays(12), D1.plusDays(13));
        assertThat(CalculoLogros.rachaMasLarga(List.of(), ChronoUnit.DAYS)).isEmpty();
    }

    @Test
    @DisplayName("racha de meses: diciembre y enero son seguidos")
    void rachaMeses() {
        List<Hito> racha = CalculoLogros.rachaMasLarga(List.of(
                LocalDate.of(2025, 11, 20), LocalDate.of(2025, 12, 3), LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 2)),
                ChronoUnit.MONTHS);

        assertThat(racha).extracting(Hito::fecha)
                .containsExactly(LocalDate.of(2025, 11, 20), LocalDate.of(2025, 12, 3), LocalDate.of(2026, 1, 2));
    }

    @Test
    @DisplayName("unidades enteras: un hito cada vez que el acumulado cruza una unidad, con las que salte de golpe")
    void unidadesEnteras() {
        List<Hito> t = CalculoLogros.unidadesEnteras(List.of(
                new FechaImporte(D1.plusDays(2), new BigDecimal("2500")),
                new FechaImporte(D1, new BigDecimal("999.99")),
                new FechaImporte(D1.plusDays(1), new BigDecimal("0.01"))), BigDecimal.valueOf(1000));

        assertThat(t).containsExactly(new Hito(D1.plusDays(1), 1), new Hito(D1.plusDays(2), 2));
    }
}
