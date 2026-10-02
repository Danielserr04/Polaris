package com.polaris.kuiper.infrastructure.nucleo;

import com.polaris.kuiper.application.in.ListMovimientoInterface;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.nucleo.application.out.ComprobarRecordatorioPort;
import com.polaris.nucleo.domain.model.AvisoRecordatorio;
import com.polaris.nucleo.domain.model.TipoRecordatorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Recordatorio de gastos: pendiente mientras no haya ningun gasto apuntado
 * ese dia (los de la papelera no cuentan). Kuiper responde a Nucleo con sus
 * propios casos de uso. Ver docs/decisiones/044-recordatorios.md.
 */
@Component
@RequiredArgsConstructor
public class GastosRecordatorioAdapter implements ComprobarRecordatorioPort {

    private final ListMovimientoInterface listMovimiento;

    @Override
    public TipoRecordatorio tipo() {
        return TipoRecordatorio.GASTOS;
    }

    @Override
    public Optional<AvisoRecordatorio> pendiente(Long usuarioId, LocalDate fecha) {
        MovimientoFilter filtro = MovimientoFilter.builder().desde(fecha).hasta(fecha).tipo(TipoMovimiento.GASTO).build();
        if (!listMovimiento.list(usuarioId, filtro).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new AvisoRecordatorio(TipoRecordatorio.GASTOS, "¿Has gastado algo hoy?",
                "No tienes ningún gasto apuntado hoy en Kuiper.", "/kuiper"));
    }
}
