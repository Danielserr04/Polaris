package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.in.CreateCuentaInterface;
import com.polaris.kuiper.application.in.DeleteCuentaInterface;
import com.polaris.kuiper.application.in.GetCuentaInterface;
import com.polaris.kuiper.application.in.GetPatrimonioInterface;
import com.polaris.kuiper.application.in.ListCuentaInterface;
import com.polaris.kuiper.application.in.UpdateCuentaInterface;
import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.infrastructure.persistence.dto.in.CuentaFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.CuentaRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.CuentaFormDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.CuentaListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.PatrimonioDto;
import com.polaris.kuiper.infrastructure.persistence.mapper.CuentaFilterMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.CuentaFormDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.CuentaListDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.CuentaRequestDtoMapper;
import com.polaris.kuiper.infrastructure.persistence.mapper.PatrimonioDtoMapper;
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
 * comprueba contra usuarioActual: son datos personales.
 */
@Tag(name = "Kuiper - Cuenta",
     description = "Cuentas bancarias, tarjetas y efectivo. El saldo actual se calcula a partir de los "
             + "movimientos y transferencias.")
@RestController
@RequestMapping("/api/kuiper/cuenta")
@RequiredArgsConstructor
public class CuentaController {

    private final CreateCuentaInterface createCuenta;
    private final GetCuentaInterface getCuenta;
    private final ListCuentaInterface listCuenta;
    private final UpdateCuentaInterface updateCuenta;
    private final DeleteCuentaInterface deleteCuenta;
    private final GetPatrimonioInterface getPatrimonio;
    private final CuentaRequestDtoMapper requestDtoMapper;
    private final CuentaFilterMapper filterMapper;
    private final CuentaFormDtoMapper formDtoMapper;
    private final CuentaListDtoMapper listDtoMapper;
    private final PatrimonioDtoMapper patrimonioDtoMapper;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Lista tus cuentas con su saldo actual",
            description = "Activas primero y por nombre. Saldo actual = saldo inicial + ingresos - gastos + "
                    + "transferencias entrantes - salientes.")
    @ApiResponse(responseCode = "200", description = "Listado, vacio si nada coincide")
    @ApiResponse(responseCode = "400", description = "Algun filtro no tiene un formato valido")
    @GetMapping
    public ResponseEntity<List<CuentaListDto>> list(@ParameterObject CuentaFilterListDto filtro) {
        List<Cuenta> cuentas = listCuenta.list(usuarioActual.id(), filterMapper.toFilter(filtro));
        return ResponseEntity.ok(listDtoMapper.toListDtoList(cuentas));
    }

    @Operation(summary = "Tu patrimonio",
            description = "Suma del saldo actual de todas tus cuentas, archivadas incluidas.")
    @ApiResponse(responseCode = "200", description = "El total, 0.00 si no tienes cuentas")
    @GetMapping("/patrimonio")
    public ResponseEntity<PatrimonioDto> patrimonio() {
        return ResponseEntity.ok(patrimonioDtoMapper.toDto(getPatrimonio.get(usuarioActual.id())));
    }

    @Parameter(name = "id", description = "Id de la cuenta", in = ParameterIn.PATH)
    @Operation(summary = "Devuelve una cuenta con su saldo actual")
    @ApiResponse(responseCode = "200", description = "La cuenta")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @GetMapping("/{id}")
    public ResponseEntity<CuentaFormDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(formDtoMapper.toFormDto(getCuenta.get(usuarioActual.id(), id)));
    }

    @Operation(summary = "Crea una cuenta", description = "El nombre es unico entre tus cuentas.")
    @ApiResponse(responseCode = "201", description = "Cuenta creada")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "409", description = "Ya tienes una cuenta con ese nombre")
    @PostMapping
    public ResponseEntity<CuentaFormDto> create(@Valid @RequestBody CuentaRequestDto dto) {
        Cuenta creada = createCuenta.create(usuarioActual.id(), requestDtoMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(formDtoMapper.toFormDto(creada));
    }

    @Parameter(name = "id", description = "Id de la cuenta", in = ParameterIn.PATH)
    @Operation(summary = "Edita o archiva una cuenta",
            description = "Reemplaza todos los campos. Archivar es archivada = true.")
    @ApiResponse(responseCode = "200", description = "Cuenta actualizada")
    @ApiResponse(responseCode = "400", description = "Datos no validos")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @ApiResponse(responseCode = "409", description = "Ya tienes otra cuenta con ese nombre")
    @PutMapping("/{id}")
    public ResponseEntity<CuentaFormDto> update(@PathVariable Long id, @Valid @RequestBody CuentaRequestDto dto) {
        Cuenta actualizada = updateCuenta.update(usuarioActual.id(), id, requestDtoMapper.toDomain(dto));
        return ResponseEntity.ok(formDtoMapper.toFormDto(actualizada));
    }

    @Parameter(name = "id", description = "Id de la cuenta", in = ParameterIn.PATH)
    @Operation(summary = "Borra una cuenta",
            description = "Solo si no tiene movimientos, recurrentes ni transferencias; si no, archivala.")
    @ApiResponse(responseCode = "204", description = "Cuenta borrada")
    @ApiResponse(responseCode = "400", description = "Tiene movimientos, recurrentes o transferencias")
    @ApiResponse(responseCode = "404", description = "No existe, o pertenece a otro usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteCuenta.delete(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
