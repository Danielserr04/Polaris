package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.in.CalcularObjetivoNutricionalInterface;
import com.polaris.fusion.infrastructure.persistence.dto.in.CalculoObjetivoFilterListDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.CalculoObjetivoDto;
import com.polaris.fusion.infrastructure.persistence.mapper.CalculoObjetivoDtoMapper;
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

import java.time.LocalDate;

/**
 * Solo lectura: propone un objetivo y no guarda nada. Para guardarlo, el
 * cliente hace POST /api/fusion/objetivo con esos numeros.
 */
@Tag(name = "Fusion - Objetivo nutricional")
@RestController
@RequestMapping("/api/fusion/objetivo/calculo")
@RequiredArgsConstructor
public class CalculoObjetivoController {

    private final CalcularObjetivoNutricionalInterface calcularObjetivo;
    private final CalculoObjetivoDtoMapper dtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Calcula un objetivo a partir de tu perfil",
            description = "Gasto basal (Mifflin-St Jeor) por el factor de actividad, mas el ajuste del tipo. "
                    + "Proteinas 2 g/kg, grasas 25 % de las kcal, carbohidratos el resto. No guarda nada.")
    @ApiResponse(responseCode = "200", description = "El objetivo propuesto y de donde sale")
    @ApiResponse(responseCode = "400",
            description = "Falta algun dato del perfil (altura, fecha de nacimiento, sexo, actividad) o un peso")
    @GetMapping
    public ResponseEntity<CalculoObjetivoDto> calcular(@Valid @ParameterObject CalculoObjetivoFilterListDto filtro) {
        return ResponseEntity.ok(dtoMapper.toDto(calcularObjetivo.calcular(usuarioActual.id(), LocalDate.now(),
                filtro.tipo(), filtro.nivelActividad(), filtro.pesoKg())));
    }
}
