package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.GetResumenMensualInterface;
import com.polaris.kuiper.infrastructure.persistence.dto.in.ResumenFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.ResumenMensualDto;
import com.polaris.kuiper.infrastructure.persistence.mapper.ResumenMensualDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

/**
 * Solo lectura: el resumen no es una entidad, se calcula. Sin {@code periodo}
 * usa el mes actual. Inyecta la interfaz de caso de uso, no el Service.
 */
@RestController
@RequestMapping("/api/kuiper/resumen")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class ResumenController {

    private final GetResumenMensualInterface getResumenMensual;
    private final ResumenMensualDtoMapper dtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<ResumenMensualDto> get(ResumenFilterListDto filtro) {
        YearMonth periodo = filtro.periodo() != null ? filtro.periodo() : YearMonth.now();
        return ResponseEntity.ok(dtoMapper.toDto(getResumenMensual.get(usuarioActual.id(), periodo)));
    }
}
