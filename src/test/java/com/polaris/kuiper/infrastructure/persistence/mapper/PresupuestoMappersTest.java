package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.domain.model.PresupuestoFilter;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.kuiper.infrastructure.persistence.CategoriaEntity;
import com.polaris.kuiper.infrastructure.persistence.PresupuestoEntity;
import com.polaris.kuiper.infrastructure.persistence.dto.in.PresupuestoFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.PresupuestoRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.PresupuestoFormDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.PresupuestoListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Los mappers de Presupuesto sobre sus implementaciones generadas por
 * MapStruct. PresupuestoEntityMapper usa CategoriaEntityMapper, asi que se
 * prueba con un contexto de Spring minimo que solo contiene esos dos mappers.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PresupuestoEntityMapperImpl.class, CategoriaEntityMapperImpl.class})
class PresupuestoMappersTest {

    @Autowired
    private PresupuestoEntityMapper entityMapper;

    private static Categoria categoria() {
        return Categoria.builder().id(10L).usuarioId(1L).nombre("Comida").color("#FF8800").icono("utensils")
                .tipo(TipoMovimiento.GASTO).build();
    }

    private static Presupuesto presupuesto() {
        return Presupuesto.builder().id(7L).usuarioId(1L).categoriaId(10L).categoria(categoria())
                .periodo(PeriodoPresupuesto.MENSUAL).importeLimite(new BigDecimal("250.00")).porcentajeAlerta(80).build();
    }

    @Test
    @DisplayName("entity -> dominio saca categoriaId de la categoria y anida la Categoria de dominio")
    void entityADominio() {
        CategoriaEntity categoriaEntity = CategoriaEntity.builder().id(10L).usuarioId(1L).nombre("Comida")
                .color("#FF8800").icono("utensils").tipo(TipoMovimiento.GASTO).build();
        PresupuestoEntity entity = PresupuestoEntity.builder().id(7L).usuarioId(1L).categoria(categoriaEntity)
                .periodo(PeriodoPresupuesto.MENSUAL).importeLimite(new BigDecimal("250.00")).porcentajeAlerta(80).build();

        Presupuesto dominio = entityMapper.toDomain(entity);

        assertThat(dominio).usingRecursiveComparison().isEqualTo(presupuesto());
        assertThat(entityMapper.toDomainList(List.of(entity))).hasSize(1);
    }

    @Test
    @DisplayName("dominio -> entity deja la categoria sin rellenar: la pone el adaptador con la Entity completa")
    void dominioAEntity() {
        PresupuestoEntity entity = entityMapper.toEntity(presupuesto());

        assertThat(entity.getCategoria()).isNull();
        assertThat(entity.getUsuarioId()).isEqualTo(1L);
        assertThat(entity.getPeriodo()).isEqualTo(PeriodoPresupuesto.MENSUAL);
        assertThat(entity.getImporteLimite()).isEqualTo(new BigDecimal("250.00"));
        assertThat(entity.getPorcentajeAlerta()).isEqualTo(80);
    }

    @Test
    @DisplayName("request -> dominio no rellena id, usuarioId ni categoria: los pone el servicio")
    void requestNoTrae() {
        Presupuesto dominio = Mappers.getMapper(PresupuestoRequestDtoMapper.class).toDomain(
                new PresupuestoRequestDto(10L, PeriodoPresupuesto.ANUAL, new BigDecimal("3000.00"), 90));

        assertThat(dominio.getId()).isNull();
        assertThat(dominio.getUsuarioId()).isNull();
        assertThat(dominio.getCategoria()).isNull();
        assertThat(dominio.getCategoriaId()).isEqualTo(10L);
        assertThat(dominio.getPeriodo()).isEqualTo(PeriodoPresupuesto.ANUAL);
        assertThat(dominio.getImporteLimite()).isEqualTo(new BigDecimal("3000.00"));
        assertThat(dominio.getPorcentajeAlerta()).isEqualTo(90);
    }

    @Test
    @DisplayName("request sin porcentajeAlerta lo deja nulo: el valor por defecto lo pone el servicio")
    void requestSinAlerta() {
        Presupuesto dominio = Mappers.getMapper(PresupuestoRequestDtoMapper.class).toDomain(
                new PresupuestoRequestDto(10L, PeriodoPresupuesto.MENSUAL, new BigDecimal("300.00"), null));

        assertThat(dominio.getPorcentajeAlerta()).isNull();
    }

    @Test
    @DisplayName("dominio -> form DTO y list DTO aplanan nombre, color e icono de la categoria y llevan el umbral")
    void dtosDeSalida() {
        PresupuestoFormDto form = new PresupuestoFormDto(7L, 10L, "Comida", "#FF8800", "utensils",
                PeriodoPresupuesto.MENSUAL, new BigDecimal("250.00"), 80);
        PresupuestoListDto lista = new PresupuestoListDto(7L, 10L, "Comida", "#FF8800", "utensils",
                PeriodoPresupuesto.MENSUAL, new BigDecimal("250.00"), 80);
        PresupuestoListDtoMapper listMapper = Mappers.getMapper(PresupuestoListDtoMapper.class);

        assertThat(Mappers.getMapper(PresupuestoFormDtoMapper.class).toFormDto(presupuesto())).isEqualTo(form);
        assertThat(listMapper.toListDto(presupuesto())).isEqualTo(lista);
        assertThat(listMapper.toListDtoList(List.of(presupuesto()))).containsExactly(lista);
    }

    @Test
    @DisplayName("filtro: query params -> filtro de dominio")
    void filtro() {
        PresupuestoFilterMapper mapper = Mappers.getMapper(PresupuestoFilterMapper.class);

        PresupuestoFilter filtro = mapper.toFilter(new PresupuestoFilterListDto(PeriodoPresupuesto.MENSUAL, 10L));
        PresupuestoFilter vacio = mapper.toFilter(new PresupuestoFilterListDto(null, null));

        assertThat(filtro.getPeriodo()).isEqualTo(PeriodoPresupuesto.MENSUAL);
        assertThat(filtro.getCategoriaId()).isEqualTo(10L);
        assertThat(vacio.getPeriodo()).isNull();
        assertThat(vacio.getCategoriaId()).isNull();
    }
}
