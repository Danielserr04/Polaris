package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.in.GetResumenDiarioInterface;
import com.polaris.fusion.infrastructure.persistence.dto.in.ResumenDiarioFilterListDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.ResumenDiarioDto;
import com.polaris.fusion.infrastructure.persistence.mapper.ResumenDiarioDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Solo lectura: el resumen no es una entidad, se calcula. Sin {@code fecha}
 * usa hoy. Inyecta la interfaz de caso de uso, no el Service.
 */
@RestController
@RequestMapping("/api/fusion/resumen")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class ResumenDiarioController {

    private final GetResumenDiarioInterface getResumenDiario;
    private final ResumenDiarioDtoMapper dtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<ResumenDiarioDto> get(ResumenDiarioFilterListDto filtro) {
        LocalDate fecha = filtro.fecha() != null ? filtro.fecha() : LocalDate.now();
        return ResponseEntity.ok(dtoMapper.toDto(getResumenDiario.get(usuarioActual.id(), fecha)));
    }
}
