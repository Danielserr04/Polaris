package com.polaris.kuiper.infrastructure.nucleo;

import com.polaris.kuiper.application.in.GetResumenMensualInterface;
import com.polaris.kuiper.domain.model.EstadoPresupuesto;
import com.polaris.kuiper.domain.model.GastoCategoria;
import com.polaris.nucleo.application.out.ComprobarRecordatorioPort;
import com.polaris.nucleo.domain.model.AvisoRecordatorio;
import com.polaris.nucleo.domain.model.TipoRecordatorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Recordatorio de presupuestos: pendiente mientras alguna categoria del mes
 * este en aviso o pasada del limite. El estado lo calcula el resumen mensual
 * de Kuiper (docs/decisiones/035-presupuesto-umbral-de-alerta.md); aqui solo
 * se escribe. Ver docs/decisiones/045-recordatorios.md.
 */
@Component
@RequiredArgsConstructor
public class PresupuestoRecordatorioAdapter implements ComprobarRecordatorioPort {

    /** Mas de estas categorias y el texto se resume en "y N mas". */
    private static final int MAX_NOMBRES = 3;

    private final GetResumenMensualInterface getResumen;

    @Override
    public TipoRecordatorio tipo() {
        return TipoRecordatorio.PRESUPUESTO;
    }

    @Override
    public Optional<AvisoRecordatorio> pendiente(Long usuarioId, LocalDate fecha) {
        List<GastoCategoria> enRiesgo = getResumen.get(usuarioId, YearMonth.from(fecha)).getGastoPorCategoria().stream()
                .filter(g -> g.getEstado() == EstadoPresupuesto.AVISO || g.getEstado() == EstadoPresupuesto.EXCEDIDO)
                .toList();
        if (enRiesgo.isEmpty()) {
            return Optional.empty();
        }
        boolean alguno = enRiesgo.stream().anyMatch(g -> g.getEstado() == EstadoPresupuesto.EXCEDIDO);
        String titulo = alguno ? "Presupuesto pasado del límite" : "Presupuesto cerca del límite";
        return Optional.of(new AvisoRecordatorio(TipoRecordatorio.PRESUPUESTO, titulo, texto(enRiesgo), "/kuiper"));
    }

    /** "Ocio al 92 %, Restaurantes al 105 %." */
    private static String texto(List<GastoCategoria> enRiesgo) {
        StringBuilder sb = new StringBuilder();
        enRiesgo.stream().limit(MAX_NOMBRES).forEach(g -> {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(g.getCategoria().getNombre());
            if (g.getPorcentaje() != null) {
                sb.append(" al ").append(g.getPorcentaje().setScale(0, RoundingMode.HALF_UP).toPlainString()).append(" %");
            }
        });
        if (enRiesgo.size() > MAX_NOMBRES) {
            sb.append(" y ").append(enRiesgo.size() - MAX_NOMBRES).append(" más");
        }
        return sb.append('.').toString();
    }
}
