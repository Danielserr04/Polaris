package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.GetComparativaCategoriasInterface;
import com.polaris.kuiper.application.in.GetEvolucionInterface;
import com.polaris.kuiper.application.in.GetProyeccionInterface;
import com.polaris.kuiper.application.in.ListComerciosFrecuentesInterface;
import com.polaris.kuiper.application.in.ListInsightsInterface;
import com.polaris.kuiper.infrastructure.persistence.dto.in.EvolucionFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.PeriodoAnalisisFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.RangoAnalisisFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.ComercioFrecuenteDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.ComparativaCategoriasDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.EvolucionMensualDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.InsightDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.ProyeccionMensualDto;
import com.polaris.kuiper.infrastructure.persistence.mapper.ComercioFrecuenteDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.ComparativaCategoriasDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.EvolucionMensualDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.InsightDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.ProyeccionMensualDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Solo lectura: el analisis no es una entidad, se calcula sobre movimientos,
 * presupuestos y recurrentes. Hoy y el mes actual los decide el servidor,
 * como en el resumen. Inyecta las interfaces de caso de uso, no el Service.
 * Ver docs/decisiones/037-analisis-calculado-en-servicio.md.
 */
@Tag(name = "Kuiper - Analisis",
     description = "Evolucion, comparativas, comercios frecuentes, insights y proyeccion. Solo lectura.")
@RestController
@RequestMapping("/api/kuiper/analisis")
@RequiredArgsConstructor
public class AnalisisController {

    static final int MESES_POR_DEFECTO = 6;
    static final int LIMITE_POR_DEFECTO = 10;

    private final GetEvolucionInterface getEvolucion;
    private final GetComparativaCategoriasInterface getComparativaCategorias;
    private final ListComerciosFrecuentesInterface listComerciosFrecuentes;
    private final ListInsightsInterface listInsights;
    private final GetProyeccionInterface getProyeccion;
    private final EvolucionMensualDtoMapper evolucionMapper;
    private final ComparativaCategoriasDtoMapper comparativaMapper;
    private final ComercioFrecuenteDtoMapper comercioMapper;
    private final InsightDtoMapper insightMapper;
    private final ProyeccionMensualDtoMapper proyeccionMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Evolucion mensual",
            description = "Ingresos, gastos, balance y tasa de ahorro de los ultimos meses, del mas antiguo al "
                    + "mas reciente. Los meses sin movimientos salen a cero.")
    @ApiResponse(responseCode = "200", description = "Una fila por mes")
    @ApiResponse(responseCode = "400", description = "meses fuera de 1..24, o hasta no tiene el formato yyyy-MM")
    @GetMapping("/evolucion")
    public ResponseEntity<List<EvolucionMensualDto>> evolucion(@ParameterObject EvolucionFilterListDto filtro) {
        int meses = filtro.meses() != null ? filtro.meses() : MESES_POR_DEFECTO;
        YearMonth hasta = filtro.hasta() != null ? filtro.hasta() : YearMonth.now();
        return ResponseEntity.ok(evolucionMapper.toDtoList(getEvolucion.get(usuarioActual.id(), hasta, meses)));
    }

    @Operation(summary = "Gasto por categoria frente al periodo anterior",
            description = "Peso de cada categoria en el gasto del rango y diferencia con el rango anterior de "
                    + "la misma duracion (meses enteros contra los meses anteriores). Sin fechas, el mes actual.")
    @ApiResponse(responseCode = "200", description = "Comparativa")
    @ApiResponse(responseCode = "400", description = "Fechas con formato no valido, o desde posterior a hasta")
    @GetMapping("/categorias")
    public ResponseEntity<ComparativaCategoriasDto> categorias(@ParameterObject RangoAnalisisFilterListDto filtro) {
        return ResponseEntity.ok(comparativaMapper.toDto(
                getComparativaCategorias.get(usuarioActual.id(), desde(filtro), hasta(filtro))));
    }

    @Operation(summary = "Comercios mas frecuentes",
            description = "Gastos agrupados por concepto (sin distinguir espacios, mayusculas ni tildes), de "
                    + "mayor a menor total. Sin fechas, el mes actual.")
    @ApiResponse(responseCode = "200", description = "Top de comercios, vacio si no hay gastos con concepto")
    @ApiResponse(responseCode = "400", description = "Fechas no validas, desde posterior a hasta, o limite fuera de 1..50")
    @GetMapping("/comercios")
    public ResponseEntity<List<ComercioFrecuenteDto>> comercios(@ParameterObject RangoAnalisisFilterListDto filtro) {
        int limite = filtro.limite() != null ? filtro.limite() : LIMITE_POR_DEFECTO;
        return ResponseEntity.ok(comercioMapper.toDtoList(
                listComerciosFrecuentes.list(usuarioActual.id(), desde(filtro), hasta(filtro), limite)));
    }

    @Operation(summary = "Insights del mes",
            description = "Observaciones redactadas sobre el mes: gasto frente a la media, categoria que mas "
                    + "sube, presupuestos, ahorro, mayor gasto, cargos de los proximos 7 dias (solo el mes "
                    + "actual) y dia de la semana con mas gasto. Primero los AVISO. Sin periodo, el mes actual.")
    @ApiResponse(responseCode = "200", description = "Insights, vacio si no hay datos")
    @ApiResponse(responseCode = "400", description = "periodo no tiene el formato yyyy-MM")
    @GetMapping("/insights")
    public ResponseEntity<List<InsightDto>> insights(@ParameterObject PeriodoAnalisisFilterListDto filtro) {
        LocalDate hoy = LocalDate.now();
        YearMonth periodo = filtro.periodo() != null ? filtro.periodo() : YearMonth.from(hoy);
        return ResponseEntity.ok(insightMapper.toDtoList(listInsights.list(usuarioActual.id(), periodo, hoy)));
    }

    @Operation(summary = "Proyeccion de gasto a fin de mes",
            description = "Gasto actual, mas el ritmo diario del gasto no recurrente por los dias que faltan, "
                    + "mas los recurrentes pendientes. Un mes pasado devuelve el gasto real. Sin periodo, el "
                    + "mes actual.")
    @ApiResponse(responseCode = "200", description = "Proyeccion")
    @ApiResponse(responseCode = "400", description = "periodo no tiene el formato yyyy-MM")
    @GetMapping("/proyeccion")
    public ResponseEntity<ProyeccionMensualDto> proyeccion(@ParameterObject PeriodoAnalisisFilterListDto filtro) {
        LocalDate hoy = LocalDate.now();
        YearMonth periodo = filtro.periodo() != null ? filtro.periodo() : YearMonth.from(hoy);
        return ResponseEntity.ok(proyeccionMapper.toDto(getProyeccion.get(usuarioActual.id(), periodo, hoy)));
    }

    private static LocalDate desde(RangoAnalisisFilterListDto filtro) {
        return filtro.desde() != null ? filtro.desde() : YearMonth.now().atDay(1);
    }

    private static LocalDate hasta(RangoAnalisisFilterListDto filtro) {
        return filtro.hasta() != null ? filtro.hasta() : YearMonth.now().atEndOfMonth();
    }
}
