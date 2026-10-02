package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.application.in.ActivarPlanComidaInterface;
import com.polaris.fusion.application.in.CreatePlanComidaInterface;
import com.polaris.fusion.application.in.DeletePlanComidaInterface;
import com.polaris.fusion.application.in.GetListaCompraInterface;
import com.polaris.fusion.application.in.GetPlanComidaInterface;
import com.polaris.fusion.application.in.ListPlanComidaInterface;
import com.polaris.fusion.application.in.UpdatePlanComidaInterface;
import com.polaris.fusion.domain.model.PlanComida;
import com.polaris.fusion.infrastructure.persistence.dto.in.PlanComidaFilterListDto;
import com.polaris.fusion.infrastructure.persistence.dto.in.PlanComidaRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.ArticuloCompraDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.PlanComidaFormDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.PlanComidaListDto;
import com.polaris.fusion.infrastructure.persistence.mapper.ArticuloCompraDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.PlanComidaFilterMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.PlanComidaFormDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.PlanComidaListDtoMapper;
import com.polaris.fusion.infrastructure.persistence.mapper.PlanComidaRequestDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
 * comprueba contra usuarioActual. Las lineas viajan anidadas en el plan.
 */
@Tag(name = "Fusion - Plan de comidas",
     description = "Plan semanal: que comer cada dia y en cada momento, con alimentos o recetas. "
             + "Un plan activo por usuario y su lista de la compra calculada al vuelo.")
@RestController
@RequestMapping("/api/fusion/plan")
@RequiredArgsConstructor
public class PlanComidaController {

    private final CreatePlanComidaInterface createPlan;
    private final GetPlanComidaInterface getPlan;
    private final ListPlanComidaInterface listPlan;
    private final UpdatePlanComidaInterface updatePlan;
    private final DeletePlanComidaInterface deletePlan;
    private final ActivarPlanComidaInterface activarPlan;
    private final GetListaCompraInterface getListaCompra;
    private final PlanComidaRequestDtoMapper requestDtoMapper;
    private final PlanComidaFilterMapper filterMapper;
    private final PlanComidaFormDtoMapper formDtoMapper;
    private final PlanComidaListDtoMapper listDtoMapper;
    private final ArticuloCompraDtoMapper articuloCompraDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus planes de comidas",
            description = "El activo primero, luego por nombre. Sin lineas, con la media diaria.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @GetMapping
    public ResponseEntity<List<PlanComidaListDto>> list(@ParameterObject PlanComidaFilterListDto filtro) {
        List<PlanComida> planes = listPlan.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(planes));
    }

    @Parameter(name = "id", description = "Id del plan", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve un plan con sus lineas",
            description = "Lineas por dia y momento, cada una con sus macros, y la media diaria.")
    @ApiResponse(responseCode = "200", description = "El plan")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<PlanComidaFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getPlan.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Crea un plan de comidas",
            description = "Nace sin activar. Cada linea: alimentoId con cantidadG, o recetaId con raciones.")
    @ApiResponse(responseCode = "201", description = "Plan creado")
    @ApiResponse(responseCode = "400", description = "Datos no validos, o una linea con alimento y receta a la vez")
    @ApiResponse(responseCode = "404", description = "Algun alimento no existe, o alguna receta no es tuya")
    @PostMapping
    public ResponseEntity<PlanComidaFormDto> create(@Valid @RequestBody PlanComidaRequestDto dto) {
        PlanComida creado = createPlan.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creado));
    }

    @Parameter(name = "id", description = "Id del plan", in = ParameterIn.PATH)
    @Operation(summary = "Edita un plan",
            description = "Reemplazo completo de nombre y lineas. Si estaba activo, sigue activo.")
    @ApiResponse(responseCode = "200", description = "Plan actualizado")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "404", description = "El plan, algun alimento o alguna receta no existe o no es tuyo")
    @PutMapping("/{id}")
    public ResponseEntity<PlanComidaFormDto> update(@PathVariable Long id,
                                                    @Valid @RequestBody PlanComidaRequestDto dto) {
        PlanComida actualizado = updatePlan.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizado));
    }

    @Parameter(name = "id", description = "Id del plan", in = ParameterIn.PATH)
    @Operation(summary = "Activa un plan", description = "Pasa a ser el unico activo; el que lo era deja de serlo.")
    @ApiResponse(responseCode = "200", description = "Plan activado")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @PostMapping("/{id}/activar")
    public ResponseEntity<PlanComidaFormDto> activar(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(activarPlan.activar(usuarioActual.id(), id)));
    }

    @Parameter(name = "id", description = "Id del plan", in = ParameterIn.PATH)
    @Operation(summary = "Lista de la compra del plan",
            description = "Gramos de cada alimento para toda la semana, con las recetas desplegadas en sus "
                    + "ingredientes segun las raciones. Por nombre.")
    @ApiResponse(responseCode = "200", description = "La lista, vacia si el plan no tiene lineas")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}/lista-compra")
    public ResponseEntity<List<ArticuloCompraDto>> listaCompra(@PathVariable Long id) {
        return ResponseEntity.ok(articuloCompraDtoMapper.toDtoList(getListaCompra.getListaCompra(usuarioActual.id(), id)));
    }

    @Parameter(name = "id", description = "Id del plan", in = ParameterIn.PATH)
    @Operation(summary = "Borra un plan", description = "Se borra con sus lineas. Las recetas no se tocan.")
    @ApiResponse(responseCode = "204", description = "Plan borrado")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deletePlan.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
