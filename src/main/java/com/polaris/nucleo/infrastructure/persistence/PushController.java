package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.application.in.CreateSuscripcionPushInterface;
import com.polaris.nucleo.application.in.DeleteSuscripcionPushInterface;
import com.polaris.nucleo.application.in.EnviarPushPruebaInterface;
import com.polaris.nucleo.application.in.GetClavePushInterface;
import com.polaris.nucleo.domain.model.SuscripcionPush;
import com.polaris.nucleo.infrastructure.persistence.dto.in.SuscripcionPushRequestDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.PushClaveDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.PushPruebaDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.SuscripcionPushFormDto;
import com.polaris.nucleo.infrastructure.persistence.mapper.SuscripcionPushFormDtoMapper;
import com.polaris.nucleo.infrastructure.persistence.mapper.SuscripcionPushRequestDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Avisos al movil (Web Push): la clave para suscribirse, el alta y la baja
 * del dispositivo y un aviso de prueba. Ver docs/decisiones/044-recordatorios.md.
 */
@Tag(name = "Nucleo - Push",
     description = "Dispositivos del usuario autenticado que reciben los recordatorios aunque Polaris este cerrado.")
@RestController
@RequestMapping("/api/nucleo/push")
@RequiredArgsConstructor
public class PushController {

    private final GetClavePushInterface getClave;
    private final CreateSuscripcionPushInterface createSuscripcion;
    private final DeleteSuscripcionPushInterface deleteSuscripcion;
    private final EnviarPushPruebaInterface enviarPrueba;
    private final SuscripcionPushRequestDtoMapper requestDtoMapper;
    private final SuscripcionPushFormDtoMapper formDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Clave publica VAPID", description = "La que pide PushManager.subscribe().")
    @ApiResponse(responseCode = "200", description = "La clave, en base64url")
    @GetMapping("/clave")
    public ResponseEntity<PushClaveDto> clave() {
        return ResponseEntity.ok(new PushClaveDto(getClave.clavePublica()));
    }

    @Operation(summary = "Suscribe este dispositivo",
            description = "Si el endpoint ya estaba, lo actualiza y pasa a ser tuyo.")
    @ApiResponse(responseCode = "200", description = "Dispositivo suscrito")
    @ApiResponse(responseCode = "400", description = "Falta algun campo o el endpoint no es https")
    @PostMapping("/suscripcion")
    public ResponseEntity<SuscripcionPushFormDto> suscribir(@Valid @RequestBody SuscripcionPushRequestDto dto) {
        SuscripcionPush guardada = createSuscripcion.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(guardada));
    }

    @Operation(summary = "Da de baja un dispositivo", description = "Si no existe o no es tuyo, no hace nada.")
    @ApiResponse(responseCode = "204", description = "Ya no recibe avisos")
    @DeleteMapping("/suscripcion")
    public ResponseEntity<Void> darDeBaja(@RequestParam String endpoint) {
        deleteSuscripcion.delete(usuarioActual.id(), endpoint);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Manda un aviso de prueba a tus dispositivos")
    @ApiResponse(responseCode = "200", description = "A cuantos dispositivos ha llegado")
    @PostMapping("/prueba")
    public ResponseEntity<PushPruebaDto> prueba() {
        return ResponseEntity.ok(new PushPruebaDto(enviarPrueba.probar(usuarioActual.id())));
    }
}
