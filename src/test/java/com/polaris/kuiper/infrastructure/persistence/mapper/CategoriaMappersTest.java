package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaFilter;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.kuiper.infrastructure.persistence.CategoriaEntity;
import com.polaris.kuiper.infrastructure.persistence.dto.in.CategoriaFilterListDto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.CategoriaRequestDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.CategoriaFormDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.CategoriaListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Los mappers de Categoria sobre sus implementaciones generadas por MapStruct. */
class CategoriaMappersTest {

    private static Categoria categoria() {
        return Categoria.builder().id(10L).usuarioId(1L).nombre("Comida").color("#FF8800").icono("utensils")
                .tipo(TipoMovimiento.GASTO).build();
    }

    @Test
    @DisplayName("entity <-> dominio conserva todos los campos, incluido el usuarioId")
    void entityIdaYVuelta() {
        CategoriaEntityMapper mapper = Mappers.getMapper(CategoriaEntityMapper.class);

        CategoriaEntity entity = mapper.toEntity(categoria());
        Categoria vuelta = mapper.toDomain(entity);

        assertThat(entity.getUsuarioId()).isEqualTo(1L);
        assertThat(entity.getTipo()).isEqualTo(TipoMovimiento.GASTO);
        assertThat(vuelta).usingRecursiveComparison().isEqualTo(categoria());
        assertThat(mapper.toDomainList(List.of(entity))).hasSize(1);
    }

    @Test
    @DisplayName("request -> dominio no rellena id ni usuarioId: los pone el servicio")
    void requestNoTrae() {
        Categoria dominio = Mappers.getMapper(CategoriaRequestDtoMapper.class)
                .toDomain(new CategoriaRequestDto("Nomina", "#00FF00", "wallet", TipoMovimiento.INGRESO));

        assertThat(dominio.getId()).isNull();
        assertThat(dominio.getUsuarioId()).isNull();
        assertThat(dominio.getNombre()).isEqualTo("Nomina");
        assertThat(dominio.getColor()).isEqualTo("#00FF00");
        assertThat(dominio.getIcono()).isEqualTo("wallet");
        assertThat(dominio.getTipo()).isEqualTo(TipoMovimiento.INGRESO);
    }

    @Test
    @DisplayName("dominio -> form DTO y list DTO llevan los mismos campos y ninguno es el usuarioId")
    void dtosDeSalida() {
        assertThat(Mappers.getMapper(CategoriaFormDtoMapper.class).toFormDto(categoria()))
                .isEqualTo(new CategoriaFormDto(10L, "Comida", "#FF8800", "utensils", TipoMovimiento.GASTO));

        CategoriaListDtoMapper listMapper = Mappers.getMapper(CategoriaListDtoMapper.class);
        CategoriaListDto esperado = new CategoriaListDto(10L, "Comida", "#FF8800", "utensils", TipoMovimiento.GASTO);
        assertThat(listMapper.toListDto(categoria())).isEqualTo(esperado);
        assertThat(listMapper.toListDtoList(List.of(categoria()))).containsExactly(esperado);
    }

    @Test
    @DisplayName("filtro: query params -> filtro de dominio, con tipo nulo si no se envia")
    void filtro() {
        CategoriaFilterMapper mapper = Mappers.getMapper(CategoriaFilterMapper.class);

        CategoriaFilter conTipo = mapper.toFilter(new CategoriaFilterListDto(TipoMovimiento.INGRESO));
        CategoriaFilter sinTipo = mapper.toFilter(new CategoriaFilterListDto(null));

        assertThat(conTipo.getTipo()).isEqualTo(TipoMovimiento.INGRESO);
        assertThat(sinTipo.getTipo()).isNull();
    }
}
