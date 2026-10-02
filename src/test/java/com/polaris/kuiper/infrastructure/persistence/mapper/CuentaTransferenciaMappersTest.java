package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.domain.model.FrecuenciaRecurrente;
import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.domain.model.TipoCuenta;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.kuiper.domain.model.Transferencia;
import com.polaris.kuiper.infrastructure.persistence.CategoriaEntity;
import com.polaris.kuiper.infrastructure.persistence.CuentaEntity;
import com.polaris.kuiper.infrastructure.persistence.RecurrenteEntity;
import com.polaris.kuiper.infrastructure.persistence.TransferenciaEntity;
import com.polaris.kuiper.infrastructure.persistence.dto.in.CuentaRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.RecurrenteRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.TransferenciaRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.CuentaListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.TransferenciaFormDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.TransferenciaListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Los mappers de Cuenta y Transferencia, y la cuenta dentro de Recurrente,
 * sobre sus implementaciones generadas por MapStruct. Los que usan otros
 * mappers se prueban con un contexto de Spring minimo.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TransferenciaEntityMapperImpl.class, RecurrenteEntityMapperImpl.class,
        CuentaEntityMapperImpl.class, CategoriaEntityMapperImpl.class})
class CuentaTransferenciaMappersTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 1);

    @Autowired
    private TransferenciaEntityMapper transferenciaEntityMapper;

    @Autowired
    private RecurrenteEntityMapper recurrenteEntityMapper;

    @Autowired
    private CuentaEntityMapper cuentaEntityMapper;

    private static CuentaEntity cuentaEntity(Long id, String nombre) {
        return CuentaEntity.builder().id(id).usuarioId(1L).nombre(nombre).tipo(TipoCuenta.AHORRO)
                .saldoInicial(new BigDecimal("-20.00")).color("#112233").banco("ING").archivada(true).build();
    }

    private static Cuenta cuenta(Long id, String nombre) {
        return Cuenta.builder().id(id).usuarioId(1L).nombre(nombre).tipo(TipoCuenta.AHORRO)
                .saldoInicial(new BigDecimal("-20.00")).color("#112233").banco("ING").archivada(true).build();
    }

    @Test
    @DisplayName("cuenta entity <-> dominio: todo menos saldoActual, que no esta en la tabla")
    void cuentaEntity() {
        Cuenta dominio = cuentaEntityMapper.toDomain(cuentaEntity(3L, "Hucha"));

        assertThat(dominio).usingRecursiveComparison().isEqualTo(cuenta(3L, "Hucha"));
        assertThat(dominio.getSaldoActual()).isNull();
        assertThat(cuentaEntityMapper.toEntity(dominio)).usingRecursiveComparison()
                .isEqualTo(cuentaEntity(3L, "Hucha"));
    }

    @Test
    @DisplayName("cuenta request -> dominio sin id, usuario ni saldoActual; dominio -> list DTO con saldoActual")
    void cuentaDtos() {
        Cuenta dominio = Mappers.getMapper(CuentaRequestDtoMapper.class).toDomain(new CuentaRequestDto(
                "Hucha", TipoCuenta.AHORRO, new BigDecimal("-20.00"), "#112233", null, "ING", true));
        dominio.setId(3L);
        dominio.setSaldoActual(new BigDecimal("80.00"));

        assertThat(dominio.getUsuarioId()).isNull();
        assertThat(Mappers.getMapper(CuentaListDtoMapper.class).toListDto(dominio)).isEqualTo(new CuentaListDto(
                3L, "Hucha", TipoCuenta.AHORRO, new BigDecimal("80.00"), "#112233", null, "ING", true));
    }

    @Test
    @DisplayName("transferencia entity -> dominio saca los ids y anida las dos cuentas; al reves las deja vacias")
    void transferenciaEntity() {
        TransferenciaEntity entity = TransferenciaEntity.builder().id(5L).usuarioId(1L)
                .cuentaOrigen(cuentaEntity(1L, "ING")).cuentaDestino(cuentaEntity(2L, "Hucha"))
                .importe(new BigDecimal("200.00")).fecha(HOY).concepto("Ahorro").build();

        Transferencia dominio = transferenciaEntityMapper.toDomain(entity);
        TransferenciaEntity vuelta = transferenciaEntityMapper.toEntity(dominio);

        assertThat(dominio.getCuentaOrigenId()).isEqualTo(1L);
        assertThat(dominio.getCuentaDestinoId()).isEqualTo(2L);
        assertThat(dominio.getCuentaDestino().getNombre()).isEqualTo("Hucha");
        assertThat(vuelta.getCuentaOrigen()).isNull();
        assertThat(vuelta.getCuentaDestino()).isNull();
        assertThat(vuelta.getImporte()).isEqualTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("transferencia: request sin fichas de cuenta; form y list DTO aplanan los nombres")
    void transferenciaDtos() {
        Transferencia desdeRequest = Mappers.getMapper(TransferenciaRequestDtoMapper.class).toDomain(
                new TransferenciaRequestDto(1L, 2L, new BigDecimal("200.00"), HOY, "Ahorro"));
        assertThat(desdeRequest.getCuentaOrigen()).isNull();
        assertThat(desdeRequest.getCuentaDestinoId()).isEqualTo(2L);

        Transferencia dominio = Transferencia.builder().id(5L).usuarioId(1L).cuentaOrigenId(1L)
                .cuentaOrigen(cuenta(1L, "ING")).cuentaDestinoId(2L).cuentaDestino(cuenta(2L, "Hucha"))
                .importe(new BigDecimal("200.00")).fecha(HOY).concepto("Ahorro").build();

        assertThat(Mappers.getMapper(TransferenciaFormDtoMapper.class).toFormDto(dominio)).isEqualTo(
                new TransferenciaFormDto(5L, HOY, new BigDecimal("200.00"), 1L, "ING", "#112233", 2L, "Hucha",
                        "#112233", "Ahorro"));
        assertThat(Mappers.getMapper(TransferenciaListDtoMapper.class).toListDto(dominio)).isEqualTo(
                new TransferenciaListDto(5L, HOY, new BigDecimal("200.00"), 1L, "ING", 2L, "Hucha", "Ahorro"));
    }

    @Test
    @DisplayName("recurrente: la cuenta viaja de la entity al dominio y a los DTOs, y la request trae solo el id")
    void recurrenteConCuenta() {
        RecurrenteEntity entity = RecurrenteEntity.builder().id(7L).usuarioId(1L).concepto("Netflix")
                .importe(new BigDecimal("12.99")).tipo(TipoMovimiento.GASTO)
                .categoria(CategoriaEntity.builder().id(10L).nombre("Ocio").tipo(TipoMovimiento.GASTO).build())
                .frecuencia(FrecuenciaRecurrente.MENSUAL).fechaInicio(HOY).proximaFecha(HOY).activo(true)
                .cuenta(cuentaEntity(3L, "ING")).build();

        Recurrente dominio = recurrenteEntityMapper.toDomain(entity);

        assertThat(dominio.getCuentaId()).isEqualTo(3L);
        assertThat(dominio.getCategoria()).usingRecursiveComparison()
                .isEqualTo(Categoria.builder().id(10L).nombre("Ocio").tipo(TipoMovimiento.GASTO).build());
        assertThat(recurrenteEntityMapper.toEntity(dominio).getCuenta()).isNull();
        assertThat(Mappers.getMapper(RecurrenteListDtoMapper.class).toListDto(dominio).cuentaNombre()).isEqualTo("ING");
        assertThat(Mappers.getMapper(RecurrenteFormDtoMapper.class).toFormDto(dominio).cuentaId()).isEqualTo(3L);

        Recurrente desdeRequest = Mappers.getMapper(RecurrenteRequestDtoMapper.class).toDomain(new RecurrenteRequestDto(
                "Netflix", new BigDecimal("12.99"), TipoMovimiento.GASTO, 10L, null, FrecuenciaRecurrente.MENSUAL,
                HOY, null, null, 3L));
        assertThat(desdeRequest.getCuentaId()).isEqualTo(3L);
        assertThat(desdeRequest.getCuenta()).isNull();
        assertThat(desdeRequest.isActivo()).isTrue();
    }
}
