package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.BorrarNotificacionesLeidasInterface;
import com.polaris.kuiper.application.in.ContarNotificacionesNoLeidasInterface;
import com.polaris.kuiper.application.in.DeleteNotificacionInterface;
import com.polaris.kuiper.application.in.GetNotificacionInterface;
import com.polaris.kuiper.application.in.LeerTodasNotificacionesInterface;
import com.polaris.kuiper.application.in.ListNotificacionInterface;
import com.polaris.kuiper.application.in.UpdateNotificacionInterface;
import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.infrastructure.persistence.dto.in.NotificacionFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.NotificacionRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.NotificacionFormDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.NotificacionListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.NotificacionTotalDto;
import com.polaris.kuiper.infrastructure.persistence.mapper.NotificacionFilterMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.NotificacionFormDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.NotificacionListDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.NotificacionRequestDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
 * Inyecta las interfaces de caso de uso, no el Service. No hay POST de
 * creacion: las notificaciones las crean los servicios y el job de avisos.
 */
@Tag(name = "Kuiper - Notificacion",
     description = "Avisos de cargos, presupuestos y resumen del mes. Se leen, se marcan leidas y se borran.")
@RestController
@RequestMapping("/api/kuiper/notificacion")
@RequiredArgsConstructor
public class NotificacionController {

    private final ListNotificacionInterface listNotificacion;
    private final GetNotificacionInterface getNotificacion;
    private final ContarNotificacionesNoLeidasInterface contarNoLeidas;
    private final UpdateNotificacionInterface updateNotificacion;
    private final LeerTodasNotificacionesInterface leerTodas;
    private final DeleteNotificacionInterface deleteNotificacion;
    private final BorrarNotificacionesLeidasInterface borrarLeidas;
    private final NotificacionRequestDtoMapper requestDtoMapper;
    private final NotificacionFilterMapper filterMapper;
    private final NotificacionFormDtoMapper formDtoMapper;
    private final NotificacionListDtoMapper listDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus notificaciones", description = "La mas reciente primero.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si no hay ninguna")
    @GetMapping
    public ResponseEntity<List<NotificacionListDto>> list(@ParameterObject NotificacionFilterListDto filtro) {
        List<Notificacion> notificaciones = listNotificacion.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(notificaciones));
    }

    @Operation(summary = "Cuantas notificaciones tienes sin leer", description = "El numero de la campana.")
    @ApiResponse(responseCode = "200", description = "El total")
    @GetMapping("/no-leidas/total")
    public ResponseEntity<NotificacionTotalDto> totalNoLeidas() {
        return ResponseEntity.ok(new NotificacionTotalDto(contarNoLeidas.contarNoLeidas(usuarioActual.id())));
    }

    @Parameter(name = "id", description = "Id de la notificacion", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve una notificacion")
    @ApiResponse(responseCode = "200", description = "La notificacion")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<NotificacionFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getNotificacion.get(usuarioActual.id(), id)));
    }

    @Parameter(name = "id", description = "Id de la notificacion", in = ParameterIn.PATH)
    @Operation(summary = "Marca una notificacion como leida",
            description = "Sin cuerpo la marca leida. Con {\"leida\": false} la vuelve a dejar sin leer.")
    @ApiResponse(responseCode = "200", description = "Notificacion actualizada")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @PutMapping("/{id}/leida")
    public ResponseEntity<NotificacionFormDto> marcarLeida(@PathVariable Long id,
                                                           @RequestBody(required = false) NotificacionRequestDto dto) {
        NotificacionRequestDto cuerpo = dto != null ? dto : new NotificacionRequestDto(true);
        Notificacion actualizada = updateNotificacion.update(usuarioActual.id(), id, requestDtoMapper.toDomain(cuerpo));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @Operation(summary = "Marca todas tus notificaciones como leidas")
    @ApiResponse(responseCode = "204", description = "Hecho, aunque no hubiera ninguna sin leer")
    @PostMapping("/leer-todas")
    public ResponseEntity<Void> leerTodas() {
        leerTodas.leerTodas(usuarioActual.id());
        return ResponseEntity.noContent().build();
    }

    @Parameter(name = "id", description = "Id de la notificacion", in = ParameterIn.PATH)
    @Operation(summary = "Borra una notificacion")
    @ApiResponse(responseCode = "204", description = "Notificacion borrada")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteNotificacion.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Borra todas tus notificaciones leidas", description = "Las no leidas se quedan.")
    @ApiResponse(responseCode = "204", description = "Hecho, aunque no hubiera ninguna leida")
    @DeleteMapping
    public ResponseEntity<Void> borrarLeidas() {
        borrarLeidas.borrarLeidas(usuarioActual.id());
        return ResponseEntity.noContent().build();
    }
}
