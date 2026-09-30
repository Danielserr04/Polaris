package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.AlimentoFilter;
import com.polaris.fusion.domain.model.FuenteAlimento;
import com.polaris.fusion.infrastructure.persistence.AlimentoEntity;
import com.polaris.fusion.infrastructure.persistence.dto.in.AlimentoFilterListDto;
import com.polaris.fusion.infrastructure.persistence.dto.in.AlimentoRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.AlimentoFormDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.AlimentoListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Los mappers de Alimento sobre sus implementaciones generadas por MapStruct. */
class AlimentoMappersTest {

    private static Alimento alimento() {
        return Alimento.builder().id(2L).nombre("Arroz").marca("Hacendado").kcal100g(new BigDecimal("130.00"))
                .proteinas100g(new BigDecimal("2.70")).carbohidratos100g(new BigDecimal("28.00"))
                .grasas100g(new BigDecimal("0.30")).fuenteExterna(FuenteAlimento.OPEN_FOOD_FACTS)
                .idExterno("8480000123456").build();
    }

    @Test
    @DisplayName("entity <-> dominio conserva todos los campos, incluidos fuente e idExterno")
    void entityIdaYVuelta() {
        AlimentoEntityMapper mapper = Mappers.getMapper(AlimentoEntityMapper.class);

        AlimentoEntity entity = mapper.toEntity(alimento());
        Alimento vuelta = mapper.toDomain(entity);

        assertThat(entity.getKcal100g()).isEqualTo(new BigDecimal("130.00"));
        assertThat(entity.getFuenteExterna()).isEqualTo(FuenteAlimento.OPEN_FOOD_FACTS);
        assertThat(entity.getIdExterno()).isEqualTo("8480000123456");
        assertThat(vuelta).usingRecursiveComparison().isEqualTo(alimento());
        assertThat(mapper.toDomainList(List.of(entity))).hasSize(1);
    }

    @Test
    @DisplayName("request -> dominio no rellena id, fuenteExterna ni idExterno: son del servicio")
    void requestNoTrae() {
        Alimento dominio = Mappers.getMapper(AlimentoRequestDtoMapper.class).toDomain(new AlimentoRequestDto(
                "Pollo", null, new BigDecimal("165.00"), new BigDecimal("31.00"), new BigDecimal("0.00"),
                new BigDecimal("3.60")));

        assertThat(dominio.getId()).isNull();
        assertThat(dominio.getFuenteExterna()).isNull();
        assertThat(dominio.getIdExterno()).isNull();
        assertThat(dominio.getNombre()).isEqualTo("Pollo");
        assertThat(dominio.getMarca()).isNull();
        assertThat(dominio.getKcal100g()).isEqualTo(new BigDecimal("165.00"));
        assertThat(dominio.getProteinas100g()).isEqualTo(new BigDecimal("31.00"));
        assertThat(dominio.getCarbohidratos100g()).isEqualTo(new BigDecimal("0.00"));
        assertThat(dominio.getGrasas100g()).isEqualTo(new BigDecimal("3.60"));
    }

    @Test
    @DisplayName("dominio -> form DTO lleva la ficha completa, con la fuente")
    void formDto() {
        AlimentoFormDto dto = Mappers.getMapper(AlimentoFormDtoMapper.class).toFormDto(alimento());

        assertThat(dto).isEqualTo(new AlimentoFormDto(2L, "Arroz", "Hacendado", new BigDecimal("130.00"),
                new BigDecimal("2.70"), new BigDecimal("28.00"), new BigDecimal("0.30"),
                FuenteAlimento.OPEN_FOOD_FACTS, "8480000123456"));
    }

    @Test
    @DisplayName("dominio -> list DTO es la version ligera, sin fuente ni idExterno")
    void listDto() {
        AlimentoListDtoMapper mapper = Mappers.getMapper(AlimentoListDtoMapper.class);
        AlimentoListDto esperado = new AlimentoListDto(2L, "Arroz", "Hacendado", new BigDecimal("130.00"),
                new BigDecimal("2.70"), new BigDecimal("28.00"), new BigDecimal("0.30"));

        assertThat(mapper.toListDto(alimento())).isEqualTo(esperado);
        assertThat(mapper.toListDtoList(List.of(alimento()))).containsExactly(esperado);
    }

    @Test
    @DisplayName("filtro: el query param q pasa a ser el texto del filtro de dominio")
    void filtroQPasaATexto() {
        AlimentoFilterMapper mapper = Mappers.getMapper(AlimentoFilterMapper.class);

        AlimentoFilter filtro = mapper.toFilter(new AlimentoFilterListDto("arroz"));
        AlimentoFilter vacio = mapper.toFilter(new AlimentoFilterListDto(null));

        assertThat(filtro.getTexto()).isEqualTo("arroz");
        assertThat(vacio.getTexto()).isNull();
    }
}
