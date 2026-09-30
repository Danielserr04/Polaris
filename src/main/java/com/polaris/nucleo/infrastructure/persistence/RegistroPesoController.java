package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.application.in.CreateRegistroPesoInterface;
import com.polaris.nucleo.application.in.DeleteRegistroPesoInterface;
import com.polaris.nucleo.application.in.GetRegistroPesoInterface;
import com.polaris.nucleo.application.in.ListRegistroPesoInterface;
import com.polaris.nucleo.application.in.UpdateRegistroPesoInterface;
import com.polaris.nucleo.domain.model.RegistroPeso;
import com.polaris.nucleo.infrastructure.persistence.dto.in.RegistroPesoFilterListDto;
import com.polaris.nucleo.infrastructure.persistence.dto.in.RegistroPesoRequestDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.RegistroPesoFormDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.RegistroPesoListDto;
import com.polaris.nucleo.infrastructure.persistence.mapper.RegistroPesoFilterMapper;
import com.polaris.nucleo.infrastructure.persistence.mapper.RegistroPesoFormDtoMapper;
import com.polaris.nucleo.infrastructure.persistence.mapper.RegistroPesoListDtoMapper;
import com.polaris.nucleo.infrastructure.persistence.mapper.RegistroPesoRequestDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Inyecta las interfaces de caso de uso, no el Service. Todo se filtra y se
 * comprueba contra usuarioActual: son datos personales.
 *
 * <p>El POST responde 201 incluso cuando actualiza el registro de ese dia
 * (un peso por dia): al cliente le da igual, quiere "este dia queda con este peso".
 */
@RestController
@RequestMapping("/api/nucleo/registro-peso")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class RegistroPesoController {

    private final CreateRegistroPesoInterface createRegistroPeso;
    private final GetRegistroPesoInterface getRegistroPeso;
    private final ListRegistroPesoInterface listRegistroPeso;
    private final UpdateRegistroPesoInterface updateRegistroPeso;
    private final DeleteRegistroPesoInterface deleteRegistroPeso;
    private final RegistroPesoRequestDtoMapper requestDtoMapper;
    private final RegistroPesoFilterMapper filterMapper;
    private final RegistroPesoFormDtoMapper formDtoMapper;
    private final RegistroPesoListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping
    public ResponseEntity<List<RegistroPesoListDto>> list(RegistroPesoFilterListDto filtro) {
        List<RegistroPeso> registros = listRegistroPeso.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(registros));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistroPesoFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getRegistroPeso.get(usuarioActual.id(), id)));
    }

    @PostMapping
    public ResponseEntity<RegistroPesoFormDto> create(@Valid @RequestBody RegistroPesoRequestDto dto) {
        RegistroPeso creado = createRegistroPeso.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegistroPesoFormDto> update(@PathVariable Long id,
                                                       @Valid @RequestBody RegistroPesoRequestDto dto) {
        RegistroPeso actualizado = updateRegistroPeso.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteRegistroPeso.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
