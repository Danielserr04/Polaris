package com.polaris.shared.logro;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Las cuentas comunes a los logros de todos los modulos, sin Spring ni base de
 * datos. La fecha de un logro no se guarda: es la del hito con el que el
 * acumulado llega al objetivo. Asi sale bien tambien para lo apuntado antes de
 * que existieran los logros, y borrar un dato mal apuntado corrige el logro.
 * Ver docs/decisiones/044-logros-globales-con-fecha-calculada.md.
 *
 * <p>Vive en shared/ porque lo usan todos los modulos y no conoce a ninguno.
 */
public final class CalculoLogros {

    private static final Comparator<Hito> POR_FECHA =
            Comparator.comparing(Hito::fecha, Comparator.nullsLast(Comparator.naturalOrder()));

    private CalculoLogros() {
    }

    /**
     * El logro con el progreso (suma de cantidades) y la fecha del hito que
     * alcanza el objetivo. Los hitos sin fecha cuentan, pero van al final: si
     * es uno de ellos el que alcanza el objetivo, el logro queda sin fecha.
     */
    public static Logro evaluar(Logro definicion, List<Hito> hitos) {
        long acumulado = 0;
        boolean alcanzado = false;
        LocalDate fecha = null;
        for (Hito h : hitos.stream().sorted(POR_FECHA).toList()) {
            acumulado += h.cantidad();
            if (!alcanzado && acumulado >= definicion.getObjetivo()) {
                alcanzado = true;
                fecha = h.fecha();
            }
        }
        return definicion.toBuilder().progreso(acumulado).fechaConseguido(fecha).build();
    }

    /** Un hito de 1 por fecha, repetidas incluidas: para contar cosas (sesiones, comidas). */
    public static List<Hito> unoPorFecha(List<LocalDate> fechas) {
        return fechas.stream().map(f -> new Hito(f, 1)).toList();
    }

    /** Un hito de 1 por dia distinto: para contar dias con algo apuntado. */
    public static List<Hito> diasDistintos(List<LocalDate> fechas) {
        return fechas.stream().filter(Objects::nonNull).distinct().map(f -> new Hito(f, 1)).toList();
    }

    /**
     * La racha mas larga de periodos seguidos (dias o semanas de lunes a
     * domingo) con al menos una fecha, como hitos: el k-esimo hito es el dia
     * en que la mejor racha llego a k. Asi "4 semanas seguidas" tiene fecha.
     */
    public static List<Hito> rachaMasLarga(List<LocalDate> fechas, TemporalUnit periodo) {
        // Cada periodo con su primer dia apuntado, que es la fecha que se ve.
        TreeMap<LocalDate, LocalDate> primerDia = new TreeMap<>();
        fechas.stream().filter(Objects::nonNull)
                .forEach(f -> primerDia.merge(inicioDe(f, periodo), f, (a, b) -> a.isBefore(b) ? a : b));
        List<Hito> hitos = new ArrayList<>();
        long mejor = 0;
        long actual = 0;
        LocalDate anterior = null;
        for (Map.Entry<LocalDate, LocalDate> e : primerDia.entrySet()) {
            actual = anterior != null && periodo.between(anterior, e.getKey()) == 1 ? actual + 1 : 1;
            if (actual > mejor) {
                mejor = actual;
                hitos.add(new Hito(e.getValue(), 1));
            }
            anterior = e.getKey();
        }
        return hitos;
    }

    /**
     * Convierte cantidades sueltas (kg por sesion) en hitos de unidades enteras
     * (toneladas): un hito cada vez que el acumulado pasa de una unidad entera
     * a la siguiente. 10.999 kg son 10 t.
     */
    public static List<Hito> unidadesEnteras(List<FechaImporte> importes, BigDecimal tamanoUnidad) {
        List<Hito> hitos = new ArrayList<>();
        BigDecimal acumulado = BigDecimal.ZERO;
        long unidades = 0;
        for (FechaImporte i : importes.stream()
                .sorted(Comparator.comparing(FechaImporte::fecha, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList()) {
            acumulado = acumulado.add(i.importe());
            long ahora = acumulado.divideToIntegralValue(tamanoUnidad).longValue();
            if (ahora > unidades) {
                hitos.add(new Hito(i.fecha(), ahora - unidades));
                unidades = ahora;
            }
        }
        return hitos;
    }

    private static LocalDate inicioDe(LocalDate fecha, TemporalUnit periodo) {
        if (periodo == ChronoUnit.WEEKS) {
            return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
        if (periodo == ChronoUnit.MONTHS) {
            return fecha.withDayOfMonth(1);
        }
        return fecha;
    }

    /** Una cantidad no entera con su fecha: los kg de una sesion, los euros de una aportacion. */
    public record FechaImporte(LocalDate fecha, BigDecimal importe) {
    }
}
