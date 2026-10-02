package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.ComprobarPresupuestoInterface;
import com.polaris.kuiper.application.in.CrearNotificacionInterface;
import com.polaris.kuiper.application.in.GenerarAvisosInterface;
import com.polaris.kuiper.application.in.GetResumenMensualInterface;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.application.out.RecurrenteRepositoryPort;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.domain.model.ResumenMensual;
import com.polaris.kuiper.domain.model.TextoAviso;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.kuiper.domain.model.TipoNotificacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * Decide cuando hay algo que avisar: cargos que vienen, presupuestos que se
 * acaban y el resumen del mes. Crear la notificacion es cosa de
 * CrearNotificacionInterface, que ya evita duplicados por clave. Ver
 * docs/decisiones/040-notificaciones-de-kuiper.md.
 *
 * <p>Nada de aqui lanza hacia fuera: cada aviso va en su propio try, y un
 * fallo con un presupuesto o un usuario no impide avisar a los demas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvisoService implements ComprobarPresupuestoInterface, GenerarAvisosInterface {

    /** Cuantos dias antes se avisa de un cargo recurrente. */
    static final int DIAS_CARGO_PROXIMO = 3;

    /** Porcentaje del limite a partir del cual se avisa, mientras no haya uno por presupuesto. */
    static final int UMBRAL_AVISO_POR_DEFECTO = 80;

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final CrearNotificacionInterface crearNotificacion;
    private final PresupuestoRepositoryPort presupuestoRepository;
    private final MovimientoRepositoryPort movimientoRepository;
    private final RecurrenteRepositoryPort recurrenteRepository;
    private final GetResumenMensualInterface resumenMensual;

    @Override
    public void comprobar(Long usuarioId, Long categoriaId, LocalDate fecha) {
        try {
            YearMonth mes = YearMonth.now();
            if (fecha == null || !YearMonth.from(fecha).equals(mes)) {
                return;
            }
            presupuestoRepository.findByUsuarioIdAndCategoriaIdAndPeriodo(usuarioId, categoriaId,
                            PeriodoPresupuesto.MENSUAL)
                    .ifPresent(p -> avisarPresupuesto(p, mes));
        } catch (RuntimeException e) {
            log.warn("No se pudo comprobar el presupuesto de la categoria {} del usuario {}: {}",
                    categoriaId, usuarioId, e.toString());
        }
    }

    /** Cada bloque por separado: si falla la lectura de uno, los otros avisan igual. */
    @Override
    public int generar(LocalDate hoy) {
        YearMonth mes = YearMonth.from(hoy);
        int creadas = seguro("cargos proximos", () -> avisarCargosProximos(hoy))
                + seguro("presupuestos", () -> avisarPresupuestos(mes));
        if (hoy.getDayOfMonth() == 1) {
            creadas += seguro("resumen mensual", () -> avisarResumenes(mes.minusMonths(1)));
        }
        return creadas;
    }

    /**
     * Porcentaje del limite a partir del cual se avisa. Hoy es fijo; es el unico
     * sitio que cambia cuando cada presupuesto tenga el suyo.
     */
    int umbralAviso(Presupuesto presupuesto) {
        return UMBRAL_AVISO_POR_DEFECTO;
    }

    /**
     * Gastos recurrentes activos con cargo en los proximos dias, sin contar hoy
     * (el de hoy ya lo genera RecurrenteJob y avisa como CARGO_RECURRENTE).
     * findPendientes devuelve los de proximaFecha hasta el limite, y si el job
     * de recurrentes no ha pasado aun, tambien los atrasados: esos se saltan.
     */
    private int avisarCargosProximos(LocalDate hoy) {
        int creadas = 0;
        for (Recurrente r : recurrenteRepository.findPendientes(hoy.plusDays(DIAS_CARGO_PROXIMO))) {
            if (r.getTipo() != TipoMovimiento.GASTO || !r.getProximaFecha().isAfter(hoy)) {
                continue;
            }
            creadas += crear(Notificacion.builder()
                    .usuarioId(r.getUsuarioId())
                    .tipo(TipoNotificacion.CARGO_PROXIMO)
                    .clave("proximo-" + r.getId() + "-" + r.getProximaFecha())
                    .titulo("Cargo próximo: " + r.getConcepto())
                    .texto(TextoAviso.euros(r.getImporte()) + " el " + TextoAviso.fecha(r.getProximaFecha()) + ".")
                    .enlace(Notificacion.ENLACE_RECURRENTES)
                    .build());
        }
        return creadas;
    }

    private int avisarPresupuestos(YearMonth mes) {
        int creadas = 0;
        for (Presupuesto p : presupuestoRepository.findAllByPeriodo(PeriodoPresupuesto.MENSUAL)) {
            try {
                creadas += avisarPresupuesto(p, mes);
            } catch (RuntimeException e) {
                log.warn("No se pudo comprobar el presupuesto {}: {}", p.getId(), e.toString());
            }
        }
        return creadas;
    }

    /**
     * Pasado el limite avisa como EXCEDIDO; si no, desde el umbral, como AVISO.
     * La clave lleva el mes: cada mes se puede volver a avisar una vez de cada.
     */
    private int avisarPresupuesto(Presupuesto presupuesto, YearMonth mes) {
        BigDecimal limite = presupuesto.getImporteLimite();
        if (limite == null || limite.signum() <= 0) {
            return 0;
        }
        BigDecimal gastado = gastado(presupuesto, mes);
        String categoria = presupuesto.getCategoria() != null ? presupuesto.getCategoria().getNombre() : "una categoría";
        String texto = "Llevas " + TextoAviso.euros(gastado) + " de " + TextoAviso.euros(limite)
                + " en " + TextoAviso.mes(mes) + ".";

        if (gastado.compareTo(limite) > 0) {
            return crear(Notificacion.builder()
                    .usuarioId(presupuesto.getUsuarioId())
                    .tipo(TipoNotificacion.PRESUPUESTO_EXCEDIDO)
                    .clave("presupuesto-excedido-" + presupuesto.getId() + "-" + mes)
                    .titulo("Presupuesto superado: " + categoria)
                    .texto(texto)
                    .enlace(Notificacion.ENLACE_PRESUPUESTOS)
                    .build());
        }

        int umbral = umbralAviso(presupuesto);
        if (gastado.multiply(CIEN).compareTo(limite.multiply(BigDecimal.valueOf(umbral))) >= 0) {
            return crear(Notificacion.builder()
                    .usuarioId(presupuesto.getUsuarioId())
                    .tipo(TipoNotificacion.PRESUPUESTO_AVISO)
                    .clave("presupuesto-aviso-" + presupuesto.getId() + "-" + mes)
                    .titulo("Presupuesto al " + umbral + " %: " + categoria)
                    .texto(texto)
                    .enlace(Notificacion.ENLACE_PRESUPUESTOS)
                    .build());
        }
        return 0;
    }

    private BigDecimal gastado(Presupuesto presupuesto, YearMonth mes) {
        List<Movimiento> movimientos = movimientoRepository.findAll(presupuesto.getUsuarioId(),
                MovimientoFilter.builder()
                        .desde(mes.atDay(1))
                        .hasta(mes.atEndOfMonth())
                        .categoriaId(presupuesto.getCategoriaId())
                        .tipo(TipoMovimiento.GASTO)
                        .build());
        return movimientos.stream().map(Movimiento::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Uno por usuario con movimientos en el mes. Sin movimientos no hay nada que resumir. */
    private int avisarResumenes(YearMonth mes) {
        int creadas = 0;
        for (Long usuarioId : movimientoRepository.findUsuarioIdsConMovimientos(mes.atDay(1), mes.atEndOfMonth())) {
            try {
                ResumenMensual resumen = resumenMensual.get(usuarioId, mes);
                creadas += crear(Notificacion.builder()
                        .usuarioId(usuarioId)
                        .tipo(TipoNotificacion.RESUMEN_MENSUAL)
                        .clave("resumen-" + mes)
                        .titulo("Resumen de " + TextoAviso.mes(mes))
                        .texto("Ingresos " + TextoAviso.euros(resumen.getIngresos())
                                + ", gastos " + TextoAviso.euros(resumen.getGastos())
                                + ", balance " + TextoAviso.euros(resumen.getBalance()) + ".")
                        .enlace(Notificacion.ENLACE_RESUMEN)
                        .build());
            } catch (RuntimeException e) {
                log.warn("No se pudo crear el resumen de {} del usuario {}: {}", mes, usuarioId, e.toString());
            }
        }
        return creadas;
    }

    private int seguro(String que, IntSupplier bloque) {
        try {
            return bloque.getAsInt();
        } catch (RuntimeException e) {
            log.warn("No se pudieron generar los avisos de {}: {}", que, e.toString());
            return 0;
        }
    }

    private int crear(Notificacion notificacion) {
        return crearNotificacion.crear(notificacion).isPresent() ? 1 : 0;
    }
}
