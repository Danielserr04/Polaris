package com.polaris.odisea.infrastructure.persistence.mapper;

import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.domain.model.EntradaFilter;
import com.polaris.odisea.domain.model.EstadoEntrada;
import com.polaris.odisea.domain.model.FuenteExterna;
import com.polaris.odisea.domain.model.TipoContenido;
import com.polaris.odisea.domain.model.Titulo;
import com.polaris.odisea.infrastructure.persistence.EntradaEntity;
import com.polaris.odisea.infrastructure.persistence.TituloEntity;
import com.polaris.odisea.infrastructure.persistence.dto.in.EntradaFilterListDto;
import com.polaris.odisea.infrastructure.persistence.dto.in.EntradaRequestDto;
import com.polaris.odisea.infrastructure.persistence.dto.out.EntradaFormDto;
import com.polaris.odisea.infrastructure.persistence.dto.out.EntradaListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Los mappers de Entrada sobre sus implementaciones generadas por MapStruct.
 * EntradaEntityMapper usa TituloEntityMapper, asi que se prueba con un
 * contexto de Spring minimo que solo contiene esos dos mappers.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EntradaEntityMapperImpl.class, TituloEntityMapperImpl.class})
class EntradaMappersTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 9, 1);
    private static final LocalDate FIN = LocalDate.of(2026, 9, 20);

    @Autowired
    private EntradaEntityMapper entityMapper;

    private static Titulo titulo() {
        return Titulo.builder().id(4L).tipo(TipoContenido.JUEGO).titulo("Hades").anio(2020)
                .imagenUrl("http://img/hades.jpg").fuenteExterna(FuenteExterna.IGDB).idExterno("113112").build();
    }

    private static Entrada entrada() {
        return Entrada.builder().id(9L).usuarioId(1L).tituloId(4L).titulo(titulo()).estado(EstadoEntrada.TERMINADO)
                .valoracion(9).notas("genial").fechaInicio(INICIO).fechaFin(FIN).favorito(true).progreso(100).build();
    }

    @Test
    @DisplayName("entity -> dominio saca tituloId del titulo y anida el Titulo de dominio")
    void entityADominio() {
        TituloEntity tituloEntity = TituloEntity.builder().id(4L).tipo(TipoContenido.JUEGO).titulo("Hades")
                .anio(2020).imagenUrl("http://img/hades.jpg").fuenteExterna(FuenteExterna.IGDB)
                .idExterno("113112").build();
        EntradaEntity entity = EntradaEntity.builder().id(9L).usuarioId(1L).titulo(tituloEntity)
                .estado(EstadoEntrada.TERMINADO).valoracion(9).notas("genial").fechaInicio(INICIO).fechaFin(FIN)
                .favorito(true).progreso(100).build();

        Entrada dominio = entityMapper.toDomain(entity);

        assertThat(dominio.getTituloId()).isEqualTo(4L);
        assertThat(dominio).usingRecursiveComparison().isEqualTo(entrada());
        assertThat(entityMapper.toDomainList(List.of(entity))).hasSize(1);
    }

    @Test
    @DisplayName("dominio -> entity deja el titulo sin rellenar: lo pone el adaptador con la Entity completa")
    void dominioAEntity() {
        EntradaEntity entity = entityMapper.toEntity(entrada());

        assertThat(entity.getTitulo()).isNull();
        assertThat(entity.getUsuarioId()).isEqualTo(1L);
        assertThat(entity.getEstado()).isEqualTo(EstadoEntrada.TERMINADO);
        assertThat(entity.getValoracion()).isEqualTo(9);
        assertThat(entity.isFavorito()).isTrue();
        assertThat(entity.getProgreso()).isEqualTo(100);
    }

    @Test
    @DisplayName("request -> dominio no rellena id, usuarioId ni titulo: los pone el servicio")
    void requestNoTrae() {
        Entrada dominio = Mappers.getMapper(EntradaRequestDtoMapper.class).toDomain(new EntradaRequestDto(
                4L, EstadoEntrada.EN_CURSO, 7, "va bien", INICIO, null, true, 40));

        assertThat(dominio.getId()).isNull();
        assertThat(dominio.getUsuarioId()).isNull();
        assertThat(dominio.getTitulo()).isNull();
        assertThat(dominio.getTituloId()).isEqualTo(4L);
        assertThat(dominio.getEstado()).isEqualTo(EstadoEntrada.EN_CURSO);
        assertThat(dominio.getValoracion()).isEqualTo(7);
        assertThat(dominio.getFechaInicio()).isEqualTo(INICIO);
        assertThat(dominio.getFechaFin()).isNull();
        assertThat(dominio.isFavorito()).isTrue();
        assertThat(dominio.getProgreso()).isEqualTo(40);
    }

    @Test
    @DisplayName("dominio -> form DTO aplana titulo, imagen y tipo del titulo")
    void formDto() {
        EntradaFormDto dto = Mappers.getMapper(EntradaFormDtoMapper.class).toFormDto(entrada());

        assertThat(dto).isEqualTo(new EntradaFormDto(9L, 4L, "Hades", "http://img/hades.jpg", TipoContenido.JUEGO,
                EstadoEntrada.TERMINADO, 9, "genial", INICIO, FIN, true, 100));
    }

    @Test
    @DisplayName("dominio -> list DTO es la version ligera: sin notas ni fechas, con anio y duracion del titulo")
    void listDto() {
        EntradaListDtoMapper mapper = Mappers.getMapper(EntradaListDtoMapper.class);
        EntradaListDto esperado = new EntradaListDto(9L, 4L, "Hades", null, 2020, null, "http://img/hades.jpg", TipoContenido.JUEGO,
                EstadoEntrada.TERMINADO, 9, true, 100);

        assertThat(mapper.toListDto(entrada())).isEqualTo(esperado);
        assertThat(mapper.toListDtoList(List.of(entrada()))).containsExactly(esperado);
    }

    @Test
    @DisplayName("filtro: query params -> filtro de dominio")
    void filtro() {
        EntradaFilterMapper mapper = Mappers.getMapper(EntradaFilterMapper.class);

        EntradaFilter filtro = mapper.toFilter(new EntradaFilterListDto(TipoContenido.JUEGO, EstadoEntrada.PENDIENTE));
        EntradaFilter vacio = mapper.toFilter(new EntradaFilterListDto(null, null));

        assertThat(filtro.getTipo()).isEqualTo(TipoContenido.JUEGO);
        assertThat(filtro.getEstado()).isEqualTo(EstadoEntrada.PENDIENTE);
        assertThat(vacio.getTipo()).isNull();
        assertThat(vacio.getEstado()).isNull();
    }
}
