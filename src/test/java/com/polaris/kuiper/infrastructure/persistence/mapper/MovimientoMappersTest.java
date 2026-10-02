package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.TipoCuenta;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.kuiper.infrastructure.persistence.CategoriaEntity;
import com.polaris.kuiper.infrastructure.persistence.CuentaEntity;
import com.polaris.kuiper.infrastructure.persistence.MovimientoEntity;
import com.polaris.kuiper.infrastructure.persistence.dto.in.MovimientoFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.MovimientoRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.MovimientoFormDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.MovimientoListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Los mappers de Movimiento sobre sus implementaciones generadas por MapStruct.
 * MovimientoEntityMapper usa CategoriaEntityMapper y CuentaEntityMapper, asi
 * que se prueba con un contexto de Spring minimo que solo contiene esos tres mappers.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MovimientoEntityMapperImpl.class, CategoriaEntityMapperImpl.class,
        CuentaEntityMapperImpl.class})
class MovimientoMappersTest {

    private static final LocalDate HOY = LocalDate.of(2026, 9, 30);

    @Autowired
    private MovimientoEntityMapper entityMapper;

    private static Categoria categoria() {
        return Categoria.builder().id(10L).usuarioId(1L).nombre("Comida").color("#FF8800").icono("utensils")
                .tipo(TipoMovimiento.GASTO).build();
    }

    private static Cuenta cuenta() {
        return Cuenta.builder().id(3L).usuarioId(1L).nombre("ING").tipo(TipoCuenta.CORRIENTE)
                .saldoInicial(new BigDecimal("100.00")).archivada(false).build();
    }

    private static Movimiento movimiento() {
        return Movimiento.builder().id(5L).usuarioId(1L).fecha(HOY).importe(new BigDecimal("12.50"))
                .tipo(TipoMovimiento.GASTO).categoriaId(10L).categoria(categoria()).concepto("Menu")
                .metodoPago("tarjeta").recurrente(true).cuentaId(3L).cuenta(cuenta()).build();
    }

    @Test
    @DisplayName("entity -> dominio saca categoriaId y cuentaId y anida la Categoria y la Cuenta de dominio")
    void entityADominio() {
        CategoriaEntity categoriaEntity = CategoriaEntity.builder().id(10L).usuarioId(1L).nombre("Comida")
                .color("#FF8800").icono("utensils").tipo(TipoMovimiento.GASTO).build();
        MovimientoEntity entity = MovimientoEntity.builder().id(5L).usuarioId(1L).fecha(HOY)
                .importe(new BigDecimal("12.50")).tipo(TipoMovimiento.GASTO).categoria(categoriaEntity)
                .concepto("Menu").metodoPago("tarjeta").recurrente(true)
                .cuenta(CuentaEntity.builder().id(3L).usuarioId(1L).nombre("ING").tipo(TipoCuenta.CORRIENTE)
                        .saldoInicial(new BigDecimal("100.00")).build())
                .build();

        Movimiento dominio = entityMapper.toDomain(entity);

        assertThat(dominio.getCategoriaId()).isEqualTo(10L);
        assertThat(dominio.getCategoria()).usingRecursiveComparison().isEqualTo(categoria());
        assertThat(dominio.getCuentaId()).isEqualTo(3L);
        assertThat(dominio.getCuenta().getSaldoActual()).isNull();
        assertThat(dominio).usingRecursiveComparison().isEqualTo(movimiento());
        assertThat(entityMapper.toDomainList(List.of(entity))).hasSize(1);
    }

    @Test
    @DisplayName("entity sin cuenta -> dominio con cuentaId y cuenta nulos")
    void entitySinCuenta() {
        MovimientoEntity entity = MovimientoEntity.builder().id(5L).usuarioId(1L).fecha(HOY)
                .importe(new BigDecimal("12.50")).tipo(TipoMovimiento.GASTO)
                .categoria(CategoriaEntity.builder().id(10L).build()).build();

        Movimiento dominio = entityMapper.toDomain(entity);

        assertThat(dominio.getCuentaId()).isNull();
        assertThat(dominio.getCuenta()).isNull();
    }

    @Test
    @DisplayName("dominio -> entity deja categoria y cuenta sin rellenar: las pone el adaptador con las Entity completas")
    void dominioAEntity() {
        MovimientoEntity entity = entityMapper.toEntity(movimiento());

        assertThat(entity.getCategoria()).isNull();
        assertThat(entity.getCuenta()).isNull();
        assertThat(entity.getUsuarioId()).isEqualTo(1L);
        assertThat(entity.getImporte()).isEqualTo(new BigDecimal("12.50"));
        assertThat(entity.getTipo()).isEqualTo(TipoMovimiento.GASTO);
        assertThat(entity.getConcepto()).isEqualTo("Menu");
        assertThat(entity.getMetodoPago()).isEqualTo("tarjeta");
        assertThat(entity.isRecurrente()).isTrue();
    }

    @Test
    @DisplayName("request -> dominio no rellena id, usuarioId ni categoria: los pone el servicio")
    void requestNoTrae() {
        Movimiento dominio = Mappers.getMapper(MovimientoRequestDtoMapper.class).toDomain(new MovimientoRequestDto(
                HOY, new BigDecimal("12.50"), TipoMovimiento.GASTO, 10L, "Menu", "tarjeta", true, 3L));

        assertThat(dominio.getId()).isNull();
        assertThat(dominio.getUsuarioId()).isNull();
        assertThat(dominio.getCategoria()).isNull();
        assertThat(dominio.getCuenta()).isNull();
        assertThat(dominio.getCuentaId()).isEqualTo(3L);
        assertThat(dominio.getCategoriaId()).isEqualTo(10L);
        assertThat(dominio.getImporte()).isEqualTo(new BigDecimal("12.50"));
        assertThat(dominio.getConcepto()).isEqualTo("Menu");
        assertThat(dominio.isRecurrente()).isTrue();
    }

    @Test
    @DisplayName("dominio -> form DTO aplana nombre, color e icono de la categoria y el nombre de la cuenta")
    void formDto() {
        MovimientoFormDto dto = Mappers.getMapper(MovimientoFormDtoMapper.class).toFormDto(movimiento());

        assertThat(dto).isEqualTo(new MovimientoFormDto(5L, HOY, new BigDecimal("12.50"), TipoMovimiento.GASTO, 10L,
                "Comida", "#FF8800", "utensils", "Menu", "tarjeta", true, 3L, "ING"));
    }

    @Test
    @DisplayName("dominio -> list DTO es la version ligera, sin metodo de pago ni recurrente")
    void listDto() {
        MovimientoListDtoMapper mapper = Mappers.getMapper(MovimientoListDtoMapper.class);
        MovimientoListDto esperado = new MovimientoListDto(5L, HOY, new BigDecimal("12.50"), TipoMovimiento.GASTO,
                10L, "Comida", "#FF8800", "utensils", "Menu", 3L, "ING");

        assertThat(mapper.toListDto(movimiento())).isEqualTo(esperado);
        assertThat(mapper.toListDtoList(List.of(movimiento()))).containsExactly(esperado);
    }

    @Test
    @DisplayName("filtro: query params -> filtro de dominio")
    void filtro() {
        MovimientoFilterMapper mapper = Mappers.getMapper(MovimientoFilterMapper.class);

        MovimientoFilter filtro = mapper.toFilter(new MovimientoFilterListDto(HOY.minusDays(30), HOY, 10L,
                TipoMovimiento.GASTO, 3L));
        MovimientoFilter vacio = mapper.toFilter(new MovimientoFilterListDto(null, null, null, null, null));

        assertThat(filtro.getDesde()).isEqualTo(HOY.minusDays(30));
        assertThat(filtro.getHasta()).isEqualTo(HOY);
        assertThat(filtro.getCategoriaId()).isEqualTo(10L);
        assertThat(filtro.getTipo()).isEqualTo(TipoMovimiento.GASTO);
        assertThat(filtro.getCuentaId()).isEqualTo(3L);
        assertThat(vacio).usingRecursiveComparison().isEqualTo(MovimientoFilter.builder().build());
    }
}
