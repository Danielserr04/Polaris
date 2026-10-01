package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.application.in.BuscarCatalogoInterface;
import com.polaris.odisea.application.in.ImportarEntradaInterface;
import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.infrastructure.persistence.dto.in.CatalogoBuscarDto;
import com.polaris.odisea.infrastructure.persistence.dto.in.ImportarEntradaRequestDto;
import com.polaris.odisea.infrastructure.persistence.dto.out.EntradaFormDto;
import com.polaris.odisea.infrastructure.persistence.dto.out.ResultadoCatalogoDto;
import com.polaris.odisea.infrastructure.persistence.mapper.EntradaFormDtoMapper;
import com.polaris.odisea.infrastructure.persistence.mapper.ResultadoCatalogoDtoMapper;
import com.polaris.shared.security.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Busqueda en las fuentes externas e importacion a tu lista.
 *
 * <p>La busqueda no toca datos personales y no filtra por usuario. La
 * importacion si: crea una Entrada tuya, y el usuarioId sale del JWT, nunca
 * del cuerpo de la peticion.
 */
@Tag(name = "Odisea - Catalogo externo",
     description = "Busqueda en las fuentes externas (TMDB para peliculas y series, IGDB para juegos, Open "
             + "Library para libros) e importacion a tu lista.")
@RestController
@RequestMapping("/api/odisea/catalogo")
@RequiredArgsConstructor
public class CatalogoController {

    private final BuscarCatalogoInterface buscarCatalogo;
    private final ImportarEntradaInterface importarEntrada;
    private final ResultadoCatalogoDtoMapper resultadoDtoMapper;
    private final EntradaFormDtoMapper entradaFormDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Busca titulos en la fuente externa",
            description = "El tipo decide la fuente. Cada resultado trae tituloId si esa ficha ya esta en el "
                    + "catalogo de Polaris y null si no. No guarda nada.")
    @ApiResponse(responseCode = "200", description = "Resultados, vacio si nada coincide")
    @ApiResponse(responseCode = "400",
            description = "Falta q o tipo, la consulta no la admite la fuente, o la fuente no esta configurada "
                    + "(falta su token o credenciales)")
    @ApiResponse(responseCode = "502", description = "La fuente externa no ha respondido correctamente")
    @GetMapping("/buscar")
    public ResponseEntity<List<ResultadoCatalogoDto>> buscar(@Valid @ParameterObject CatalogoBuscarDto filtro) {
        return ResponseEntity.ok(resultadoDtoMapper.toDtoList(
                buscarCatalogo.buscar(filtro.q(), filtro.tipo())));
    }

    @Operation(summary = "Importa un titulo externo a tu lista",
            description = "Crea la ficha en el catalogo compartido (o reutiliza la que otro usuario ya importo) "
                    + "y una entrada tuya en estado PENDIENTE.")
    @ApiResponse(responseCode = "201", description = "Entrada creada")
    @ApiResponse(responseCode = "400",
            description = "Cuerpo no valido, idExterno no valido para la fuente, o la fuente no esta "
                    + "configurada")
    @ApiResponse(responseCode = "409", description = "Ya tienes ese titulo en tu lista")
    @ApiResponse(responseCode = "502", description = "La fuente externa no ha respondido correctamente")
    @PostMapping("/importar")
    public ResponseEntity<EntradaFormDto> importar(@Valid @RequestBody ImportarEntradaRequestDto dto) {
        Entrada entrada = importarEntrada.importar(usuarioActual.id(), dto.idExterno(), dto.tipo());
        return ResponseEntity.status(HttpStatus.CREATED).body(entradaFormDtoMapper.toFormDto(entrada));
    }
}
