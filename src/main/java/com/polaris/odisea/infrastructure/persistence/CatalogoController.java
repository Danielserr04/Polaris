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

import java.util.List;

/**
 * Busqueda en las fuentes externas e importacion a tu lista.
 *
 * <p>La busqueda no toca datos personales y no filtra por usuario. La
 * importacion si: crea una Entrada tuya, y el usuarioId sale del JWT, nunca
 * del cuerpo de la peticion.
 */
@RestController
@RequestMapping("/api/odisea/catalogo")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class CatalogoController {

    private final BuscarCatalogoInterface buscarCatalogo;
    private final ImportarEntradaInterface importarEntrada;
    private final ResultadoCatalogoDtoMapper resultadoDtoMapper;
    private final EntradaFormDtoMapper entradaFormDtoMapper;
    private final UsuarioActual usuarioActual;

    @GetMapping("/buscar")
    public ResponseEntity<List<ResultadoCatalogoDto>> buscar(@Valid CatalogoBuscarDto filtro) {
        return ResponseEntity.ok(resultadoDtoMapper.toDtoList(
                buscarCatalogo.buscar(filtro.q(), filtro.tipo())));
    }

    @PostMapping("/importar")
    public ResponseEntity<EntradaFormDto> importar(@Valid @RequestBody ImportarEntradaRequestDto dto) {
        Entrada entrada = importarEntrada.importar(usuarioActual.id(), dto.idExterno(), dto.tipo());
        return ResponseEntity.status(HttpStatus.CREATED).body(entradaFormDtoMapper.toFormDto(entrada));
    }
}
