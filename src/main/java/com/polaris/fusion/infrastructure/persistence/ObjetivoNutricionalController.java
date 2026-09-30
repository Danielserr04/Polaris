package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.in.CreateObjetivoNutricionalInterface;
import com.polaris.fusion.application.in.GetObjetivoNutricionalInterface;
import com.polaris.fusion.application.in.ListObjetivoNutricionalInterface;
import com.polaris.fusion.domain.model.ObjetivoNutricional;
import com.polaris.fusion.infrastructure.persistence.dto.in.ObjetivoNutricionalFilterListDto;
import com.polaris.fusion.infrastructure.persistence.dto.in.ObjetivoNutricionalRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.ObjetivoNutricionalFormDto;
import com.polaris.fusion.infrastructure.persistence.mapper.ObjetivoNutricionalFormDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.ObjetivoNutricionalRequestDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Inyecta las interfaces de caso de uso, no el Service. Son datos personales:
 * todo va contra usuarioActual. Sin PUT ni DELETE: el historico es inmutable.
 *
 * <p>{@code GET /api/fusion/objetivo} devuelve el vigente (con {@code ?fecha=}
 * opcional, por defecto hoy) y {@code GET /api/fusion/objetivo/historico} la
 * lista completa.
 */
@RestController
@RequestMapping("/api/fusion/objetivo")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class ObjetivoNutricionalController {

    private final CreateObjetivoNutricionalInterface createObjetivo;
    private final GetObjetivoNutricionalInterface getObjetivo;
    private final ListObjetivoNutricionalInterface listObjetivo;
    private final ObjetivoNutricionalRequestDtoMapper requestDtoMapper;
    private final ObjetivoNutricionalFormDtoMapper formDtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<ObjetivoNutricionalFormDto> getVigente(ObjetivoNutricionalFilterListDto filtro) {
        LocalDate fecha = filtro.fecha() != null ? filtro.fecha() : LocalDate.now();
        return ResponseEntity.ok(formDtoMapper.toFormDto(getObjetivo.getVigente(usuarioActual.id(), fecha)));
    }

    @GetMapping("/historico")
    public ResponseEntity<List<ObjetivoNutricionalFormDto>> list() {
        List<ObjetivoNutricional> objetivos = listObjetivo.list(usuarioActual.id());
        return ResponseEntity.ok(formDtoMapper.toFormDtoList(objetivos));
    }

    @PostMapping
    public ResponseEntity<ObjetivoNutricionalFormDto> create(@Valid @RequestBody ObjetivoNutricionalRequestDto dto) {
        ObjetivoNutricional creado = createObjetivo.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creado));
    }
}
