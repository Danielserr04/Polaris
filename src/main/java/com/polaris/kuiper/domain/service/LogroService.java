package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.ListLogrosInterface;
import com.polaris.kuiper.application.out.EstadisticasLogrosPort;
import com.polaris.kuiper.domain.model.EstadisticasLogros;
import com.polaris.kuiper.domain.model.EstadisticasLogros.MetaLogro;
import com.polaris.kuiper.domain.model.MetricaLogro;
import com.polaris.shared.logro.CalculoLogros;
import com.polaris.shared.logro.CalculoLogros.FechaImporte;
import com.polaris.shared.logro.Hito;
import com.polaris.shared.logro.Logro;
import com.polaris.shared.logro.NivelLogro;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.polaris.shared.logro.Logro.definicion;

/**
 * Logros de gastos y ahorro, calculados al vuelo. Un mes "en verde" solo
 * cuenta cuando ha terminado, y su fecha es su ultimo dia. Una meta se
 * consigue el dia de la aportacion que llega al objetivo. Ver
 * docs/decisiones/044-logros-globales-con-fecha-calculada.md.
 */
@Service("kuiperLogroService")
@RequiredArgsConstructor
public class LogroService implements ListLogrosInterface {

    /** El catalogo, en el orden en que se muestra. */
    static final List<Definicion> CATALOGO = List.of(
            new Definicion(MetricaLogro.MOVIMIENTOS, definicion("PRIMER_MOVIMIENTO", "Primer apunte",
                    "Apunta tu primer movimiento", "wallet", NivelLogro.BRONCE, "movimientos", 1)),
            new Definicion(MetricaLogro.MOVIMIENTOS, definicion("MOVIMIENTOS_100", "Contable",
                    "Apunta 100 movimientos", "wallet", NivelLogro.PLATA, "movimientos", 100)),
            new Definicion(MetricaLogro.MOVIMIENTOS, definicion("MOVIMIENTOS_1000", "Mil apuntes",
                    "Apunta 1.000 movimientos", "wallet", NivelLogro.ORO, "movimientos", 1000)),
            new Definicion(MetricaLogro.RACHA_MESES, definicion("RACHA_MESES_3", "Al día",
                    "Apunta movimientos 3 meses seguidos", "calendar", NivelLogro.BRONCE, "meses", 3)),
            new Definicion(MetricaLogro.RACHA_MESES, definicion("RACHA_MESES_12", "Un año de cuentas claras",
                    "Apunta movimientos 12 meses seguidos", "calendar", NivelLogro.ORO, "meses", 12)),
            new Definicion(MetricaLogro.MESES_EN_VERDE, definicion("MES_EN_VERDE", "Mes en verde",
                    "Cierra un mes ingresando más de lo que gastas", "arrow-up-right", NivelLogro.BRONCE, "meses", 1)),
            new Definicion(MetricaLogro.MESES_EN_VERDE, definicion("MESES_EN_VERDE_6", "Hormiguita",
                    "Cierra 6 meses en verde", "arrow-up-right", NivelLogro.PLATA, "meses", 6)),
            new Definicion(MetricaLogro.MESES_EN_VERDE, definicion("MESES_EN_VERDE_12", "Ahorrador nato",
                    "Cierra 12 meses en verde", "arrow-up-right", NivelLogro.ORO, "meses", 12)),
            new Definicion(MetricaLogro.PRESUPUESTOS, definicion("PRIMER_PRESUPUESTO", "Bajo control",
                    "Crea tu primer presupuesto", "check", NivelLogro.BRONCE, "presupuestos", 1)),
            new Definicion(MetricaLogro.APORTACIONES, definicion("PRIMERA_APORTACION", "Primera hucha",
                    "Aporta a una meta de ahorro", "sparkles", NivelLogro.BRONCE, "aportaciones", 1)),
            new Definicion(MetricaLogro.METAS_CONSEGUIDAS, definicion("META_CONSEGUIDA", "Meta cumplida",
                    "Completa una meta de ahorro", "star", NivelLogro.PLATA, "metas", 1)),
            new Definicion(MetricaLogro.METAS_CONSEGUIDAS, definicion("METAS_5", "Cazador de metas",
                    "Completa 5 metas de ahorro", "star", NivelLogro.ORO, "metas", 5)));

    private final EstadisticasLogrosPort estadisticasPort;

    @Override
    public List<Logro> list(Long usuarioId, LocalDate hoy) {
        EstadisticasLogros e = estadisticasPort.find(usuarioId);
        Map<MetricaLogro, List<Hito>> hitos = Map.of(
                MetricaLogro.MOVIMIENTOS, CalculoLogros.unoPorFecha(e.getFechasMovimiento()),
                MetricaLogro.RACHA_MESES, CalculoLogros.rachaMasLarga(e.getFechasMovimiento(), ChronoUnit.MONTHS),
                MetricaLogro.MESES_EN_VERDE, mesesEnVerde(e.getBalancePorDia(), hoy),
                MetricaLogro.APORTACIONES, CalculoLogros.unoPorFecha(e.getMetas().stream()
                        .flatMap(m -> m.aportaciones().stream()).map(FechaImporte::fecha).toList()),
                MetricaLogro.METAS_CONSEGUIDAS, metasConseguidas(e.getMetas()),
                MetricaLogro.PRESUPUESTOS, e.getNumeroPresupuestos() > 0
                        ? List.of(new Hito(null, e.getNumeroPresupuestos())) : List.of());
        return CATALOGO.stream()
                .map(d -> CalculoLogros.evaluar(d.logro(), hitos.get(d.metrica())))
                .toList();
    }

    /** Un hito por mes ya terminado con balance positivo, fechado en su ultimo dia. */
    static List<Hito> mesesEnVerde(List<FechaImporte> balancePorDia, LocalDate hoy) {
        Map<YearMonth, BigDecimal> porMes = new TreeMap<>();
        balancePorDia.forEach(b -> porMes.merge(YearMonth.from(b.fecha()), b.importe(), BigDecimal::add));
        YearMonth actual = YearMonth.from(hoy);
        return porMes.entrySet().stream()
                .filter(m -> m.getKey().isBefore(actual) && m.getValue().signum() > 0)
                .map(m -> new Hito(m.getKey().atEndOfMonth(), 1))
                .toList();
    }

    /** Un hito por meta completada, fechado el dia de la aportacion que llega al objetivo. */
    static List<Hito> metasConseguidas(List<MetaLogro> metas) {
        List<Hito> hitos = new ArrayList<>();
        for (MetaLogro meta : metas) {
            BigDecimal acumulado = BigDecimal.ZERO;
            for (FechaImporte a : meta.aportaciones().stream()
                    .sorted(Comparator.comparing(FechaImporte::fecha)).toList()) {
                acumulado = acumulado.add(a.importe());
                if (acumulado.compareTo(meta.importeObjetivo()) >= 0) {
                    hitos.add(new Hito(a.fecha(), 1));
                    break;
                }
            }
        }
        return hitos;
    }

    /** Un logro del catalogo con lo que mide. */
    record Definicion(MetricaLogro metrica, Logro logro) {
    }
}
