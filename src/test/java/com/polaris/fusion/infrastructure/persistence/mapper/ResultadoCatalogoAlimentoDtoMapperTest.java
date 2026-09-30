package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.FuenteAlimento;
import com.polaris.fusion.domain.model.ResultadoCatalogoAlimento;
import com.polaris.fusion.infrastructure.persistence.dto.out.ResultadoCatalogoAlimentoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultadoCatalogoAlimentoDtoMapperTest {

    private final ResultadoCatalogoAlimentoDtoMapper mapper =
            Mappers.getMapper(ResultadoCatalogoAlimentoDtoMapper.class);

    private static ResultadoCatalogoAlimento resultado(Long alimentoId) {
        return ResultadoCatalogoAlimento.builder().fuenteExterna(FuenteAlimento.OPEN_FOOD_FACTS)
                .idExterno("8480000123456").nombre("Arroz").marca("Hacendado")
                .kcal100g(new BigDecimal("130.00")).proteinas100g(new BigDecimal("2.70"))
                .carbohidratos100g(new BigDecimal("28.00")).grasas100g(new BigDecimal("0.30"))
                .alimentoId(alimentoId).build();
    }

    @Test
    @DisplayName("lleva todos los campos, y alimentoId indica que ya esta importado")
    void todosLosCampos() {
        ResultadoCatalogoAlimentoDto dto = mapper.toDto(resultado(2L));

        assertThat(dto).isEqualTo(new ResultadoCatalogoAlimentoDto(FuenteAlimento.OPEN_FOOD_FACTS, "8480000123456",
                "Arroz", "Hacendado", new BigDecimal("130.00"), new BigDecimal("2.70"), new BigDecimal("28.00"),
                new BigDecimal("0.30"), 2L));
    }

    @Test
    @DisplayName("alimentoId nulo se conserva nulo (aun no importado) y funciona sobre listas")
    void sinImportarYListas() {
        assertThat(mapper.toDto(resultado(null)).alimentoId()).isNull();
        assertThat(mapper.toDtoList(List.of(resultado(1L), resultado(null)))).hasSize(2);
    }
}
