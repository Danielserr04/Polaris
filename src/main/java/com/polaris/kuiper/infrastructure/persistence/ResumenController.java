package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.GetResumenMensualInterface;
import com.polaris.kuiper.infrastructure.persistence.dto.in.ResumenFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.ResumenMensualDto;
import com.polaris.kuiper.infrastructure.persistence.mapper.ResumenMensualDtoMapper;
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
    private final ResumenMensualDtoMapper dtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Resumen de un mes",
            description = "Ingresos, gastos y balance del mes, y el gasto por categoria frente a su presupuesto "
                    + "MENSUAL (una fila por categoria con gasto o con presupuesto, de mayor a menor "
                    + "gasto). Sin periodo usa el mes actual.")
    @ApiResponse(responseCode = "200", description = "Resumen del mes")
    @ApiResponse(responseCode = "400", description = "periodo no tiene el formato yyyy-MM")
    @GetMapping
    public ResponseEntity<ResumenMensualDto> get(@ParameterObject ResumenFilterListDto filtro) {
        YearMonth periodo = filtro.periodo() != null ? filtro.periodo() : YearMonth.now();
        return ResponseEntity.ok(dtoMapper.toDto(getResumenMensual.get(usuarioActual.id(), periodo)));
    }
}
