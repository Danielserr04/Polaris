package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.GetResumenAnualInterface;
import com.polaris.kuiper.application.in.GetResumenMensualInterface;
import com.polaris.kuiper.infrastructure.persistence.dto.in.ResumenAnualFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.ResumenFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.ResumenAnualDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.ResumenMensualDto;
import com.polaris.kuiper.infrastructure.persistence.mapper.ResumenAnualDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.ResumenMensualDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;
import java.time.YearMonth;

/**
 * Solo lectura: el resumen no es una entidad, se calcula. Sin {@code periodo}
 * usa el mes actual. Inyecta la interfaz de caso de uso, no el Service.
 */
@Tag(name = "Kuiper - Resumen",
     description = "Resumen mensual calculado sobre tus movimientos y presupuestos. Solo lectura.")
@RestController
@RequestMapping("/api/kuiper/resumen")
@RequiredArgsConstructor
public class ResumenController {

    private final GetResumenMensualInterface getResumenMensual;
    private final GetResumenAnualInterface getResumenAnual;
    private final ResumenMensualDtoMapper dtoMapper;
    private final ResumenAnualDtoMapper anualDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Resumen de un mes",
            description = "Ingresos, gastos y balance del mes, y el gasto por categoria frente a su presupuesto "
                    + "MENSUAL (una fila por categoria con gasto o con presupuesto, de mayor a menor "
                    + "gasto), con su porcentaje consumido y su estado (OK, AVISO, EXCEDIDO) segun el umbral "
                    + "de alerta. Sin periodo usa el mes actual.")
    @ApiResponse(responseCode = "200", description = "Resumen del mes")
    @ApiResponse(responseCode = "400", description = "periodo no tiene el formato yyyy-MM")
    @GetMapping
    public ResponseEntity<ResumenMensualDto> get(@ParameterObject ResumenFilterListDto filtro) {
        YearMonth periodo = filtro.periodo() != null ? filtro.periodo() : YearMonth.now();
        return ResponseEntity.ok(dtoMapper.toDto(getResumenMensual.get(usuarioActual.id(), periodo)));
    }

    @Operation(summary = "Presupuestos anuales de un anio",
            description = "Cada presupuesto ANUAL frente a lo gastado en su categoria del 1 de enero al 31 de "
                    + "diciembre, con porcentaje consumido y estado (OK, AVISO, EXCEDIDO). Del mas consumido "
                    + "al menos; vacio si no tienes presupuestos anuales. Sin anio usa el actual.")
    @ApiResponse(responseCode = "200", description = "Presupuestos anuales del anio")
    @ApiResponse(responseCode = "400", description = "anio no es un numero entre 1900 y 9999")
    @GetMapping("/anual")
    public ResponseEntity<ResumenAnualDto> anual(@Valid @ParameterObject ResumenAnualFilterListDto filtro) {
        int anio = filtro.anio() != null ? filtro.anio() : Year.now().getValue();
        return ResponseEntity.ok(anualDtoMapper.toDto(getResumenAnual.get(usuarioActual.id(), anio)));
    }
}
