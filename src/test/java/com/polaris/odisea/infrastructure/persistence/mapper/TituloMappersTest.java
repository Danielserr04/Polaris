package com.polaris.odisea.infrastructure.persistence.mapper;

import com.polaris.odisea.domain.model.FuenteExterna;
import com.polaris.odisea.domain.model.TipoContenido;
import com.polaris.odisea.domain.model.Titulo;
import com.polaris.odisea.domain.model.TituloFilter;
import com.polaris.odisea.infrastructure.persistence.TituloEntity;
import com.polaris.odisea.infrastructure.persistence.dto.in.TituloFilterListDto;
import com.polaris.odisea.infrastructure.persistence.dto.in.TituloRequestDto;
import com.polaris.odisea.infrastructure.persistence.dto.out.TituloFormDto;
import com.polaris.odisea.infrastructure.persistence.dto.out.TituloListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Los mappers de Titulo sobre sus implementaciones generadas por MapStruct. */
class TituloMappersTest {

    private static Titulo titulo() {
        return Titulo.builder().id(4L).tipo(TipoContenido.PELICULA).titulo("Dune").tituloOriginal("Dune")
                .anio(2021).sinopsis("Arrakis").imagenUrl("http://img/dune.jpg").generos("Ciencia ficcion")
                .duracionMin(155).fuenteExterna(FuenteExterna.TMDB).idExterno("438631").build();
    }

    @Test
    @DisplayName("entity <-> dominio conserva todos los campos")
    void entityIdaYVuelta() {
        TituloEntityMapper mapper = Mappers.getMapper(TituloEntityMapper.class);

        TituloEntity entity = mapper.toEntity(titulo());
        Titulo vuelta = mapper.toDomain(entity);

        assertThat(entity.getFuenteExterna()).isEqualTo(FuenteExterna.TMDB);
        assertThat(entity.getIdExterno()).isEqualTo("438631");
        assertThat(vuelta).usingRecursiveComparison().isEqualTo(titulo());
        assertThat(mapper.toDomainList(List.of(entity))).hasSize(1);
    }

    @Test
    @DisplayName("request -> dominio no rellena el id: lo pone la base de datos")
    void requestNoTraeId() {
        Titulo dominio = Mappers.getMapper(TituloRequestDtoMapper.class).toDomain(new TituloRequestDto(
                TipoContenido.LIBRO, "Dune", null, 1965, null, null, "Ciencia ficcion", null,
                FuenteExterna.MANUAL, null));

        assertThat(dominio.getId()).isNull();
        assertThat(dominio.getTipo()).isEqualTo(TipoContenido.LIBRO);
        assertThat(dominio.getTitulo()).isEqualTo("Dune");
        assertThat(dominio.getAnio()).isEqualTo(1965);
        assertThat(dominio.getFuenteExterna()).isEqualTo(FuenteExterna.MANUAL);
        assertThat(dominio.getIdExterno()).isNull();
    }

    @Test
    @DisplayName("dominio -> form DTO lleva la ficha completa")
    void formDto() {
        TituloFormDto dto = Mappers.getMapper(TituloFormDtoMapper.class).toFormDto(titulo());

        assertThat(dto).isEqualTo(new TituloFormDto(4L, TipoContenido.PELICULA, "Dune", "Dune", 2021, "Arrakis",
                "http://img/dune.jpg", "Ciencia ficcion", 155, FuenteExterna.TMDB, "438631"));
    }

    @Test
    @DisplayName("dominio -> list DTO es la version ligera y funciona sobre listas")
    void listDto() {
        TituloListDtoMapper mapper = Mappers.getMapper(TituloListDtoMapper.class);
        TituloListDto esperado = new TituloListDto(4L, TipoContenido.PELICULA, "Dune", 2021, "http://img/dune.jpg");

        assertThat(mapper.toListDto(titulo())).isEqualTo(esperado);
        assertThat(mapper.toListDtoList(List.of(titulo()))).containsExactly(esperado);
    }

    @Test
    @DisplayName("filtro: query params -> filtro de dominio")
    void filtro() {
        TituloFilterMapper mapper = Mappers.getMapper(TituloFilterMapper.class);

        TituloFilter filtro = mapper.toFilter(new TituloFilterListDto(TipoContenido.SERIE, "dun"));
        TituloFilter vacio = mapper.toFilter(new TituloFilterListDto(null, null));

        assertThat(filtro.getTipo()).isEqualTo(TipoContenido.SERIE);
        assertThat(filtro.getTexto()).isEqualTo("dun");
        assertThat(vacio.getTipo()).isNull();
        assertThat(vacio.getTexto()).isNull();
    }
}
