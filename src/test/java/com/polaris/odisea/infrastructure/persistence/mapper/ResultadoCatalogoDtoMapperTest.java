package com.polaris.odisea.infrastructure.persistence.mapper;

import com.polaris.odisea.domain.model.FuenteExterna;
import com.polaris.odisea.domain.model.ResultadoCatalogo;
import com.polaris.odisea.domain.model.TipoContenido;
import com.polaris.odisea.infrastructure.persistence.dto.out.ResultadoCatalogoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultadoCatalogoDtoMapperTest {

    private final ResultadoCatalogoDtoMapper mapper = Mappers.getMapper(ResultadoCatalogoDtoMapper.class);

    private static ResultadoCatalogo resultado(Long tituloId) {
        return ResultadoCatalogo.builder().fuenteExterna(FuenteExterna.OPEN_LIBRARY).idExterno("OL45804W")
                .tipo(TipoContenido.LIBRO).titulo("Dune").tituloOriginal("Dune").anio(1965).sinopsis("Arrakis")
                .imagenUrl("http://img/dune.jpg").tituloId(tituloId).build();
    }

    @Test
    @DisplayName("lleva todos los campos, y tituloId indica que ya esta importado")
    void todosLosCampos() {
        ResultadoCatalogoDto dto = mapper.toDto(resultado(4L));

        assertThat(dto).isEqualTo(new ResultadoCatalogoDto(FuenteExterna.OPEN_LIBRARY, "OL45804W",
                TipoContenido.LIBRO, "Dune", "Dune", 1965, "Arrakis", "http://img/dune.jpg", 4L));
    }

    @Test
    @DisplayName("tituloId nulo se conserva nulo (aun no importado) y funciona sobre listas")
    void sinImportarYListas() {
        assertThat(mapper.toDto(resultado(null)).tituloId()).isNull();
        assertThat(mapper.toDtoList(List.of(resultado(1L), resultado(null)))).hasSize(2);
    }
}
