package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.Rutina;
import com.polaris.atlas.domain.model.RutinaEjercicio;
import com.polaris.atlas.domain.model.RutinaFilter;
import com.polaris.atlas.infrastructure.persistence.EjercicioEntity;
import com.polaris.atlas.infrastructure.persistence.RutinaEjercicioEntity;
import com.polaris.atlas.infrastructure.persistence.RutinaEntity;
import com.polaris.atlas.infrastructure.persistence.dto.in.RutinaEjercicioRequestDto;
import com.polaris.atlas.infrastructure.persistence.dto.in.RutinaFilterListDto;
import com.polaris.atlas.infrastructure.persistence.dto.in.RutinaRequestDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.RutinaEjercicioFormDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.RutinaFormDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.RutinaListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Los mappers de Rutina y RutinaEjercicio sobre sus implementaciones generadas
 * por MapStruct. Los de entity y form DTO se apoyan en otros mappers (uses), asi
 * que se prueban con un contexto de Spring minimo que solo contiene esos mappers.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RutinaEntityMapperImpl.class, RutinaEjercicioEntityMapperImpl.class,
        EjercicioEntityMapperImpl.class, RutinaFormDtoMapperImpl.class, RutinaEjercicioFormDtoMapperImpl.class})
class RutinaMappersTest {

    @Autowired
    private RutinaEntityMapper entityMapper;

    @Autowired
    private RutinaEjercicioEntityMapper lineaEntityMapper;

    @Autowired
    private RutinaFormDtoMapper formMapper;

    private static Ejercicio pressBanca() {
        return Ejercicio.builder().id(2L).nombre("Press banca").grupoMuscular("Pecho").equipamiento("Barra").build();
    }

    private static Ejercicio dominadas() {
        return Ejercicio.builder().id(3L).usuarioId(1L).nombre("Dominadas").grupoMuscular("Espalda").build();
    }

    private static RutinaEjercicio linea(Long id, Ejercicio ejercicio, int orden, String reps) {
        return RutinaEjercicio.builder().id(id).usuarioId(1L).ejercicioId(ejercicio.getId()).ejercicio(ejercicio)
                .orden(orden).seriesObjetivo(4).repsObjetivo(reps).build();
    }

    private static Rutina rutina() {
        return Rutina.builder().id(8L).usuarioId(1L).nombre("Push").descripcion("Empuje").activa(true)
                .lineas(List.of(linea(20L, pressBanca(), 1, "8-12"), linea(21L, dominadas(), 2, "AMRAP"))).build();
    }

    @Test
    @DisplayName("entity -> dominio de la rutina trae las lineas con su ejercicio y ejercicioId, y usuarioId por linea")
    void entityADominio() {
        EjercicioEntity ejercicio = EjercicioEntity.builder().id(2L).nombre("Press banca").grupoMuscular("Pecho")
                .equipamiento("Barra").build();
        RutinaEjercicioEntity lineaEntity = RutinaEjercicioEntity.builder().id(20L).usuarioId(1L).ejercicio(ejercicio)
                .orden(1).seriesObjetivo(4).repsObjetivo("8-12").build();
        RutinaEntity entity = RutinaEntity.builder().id(8L).usuarioId(1L).nombre("Push").descripcion("Empuje")
                .activa(true).lineas(List.of(lineaEntity)).build();

        Rutina dominio = entityMapper.toDomain(entity);

        assertThat(dominio.getUsuarioId()).isEqualTo(1L);
        assertThat(dominio.getNombre()).isEqualTo("Push");
        assertThat(dominio.getDescripcion()).isEqualTo("Empuje");
        assertThat(dominio.isActiva()).isTrue();
        assertThat(dominio.getLineas()).hasSize(1);
        RutinaEjercicio l = dominio.getLineas().get(0);
        assertThat(l.getUsuarioId()).isEqualTo(1L);
        assertThat(l.getEjercicioId()).isEqualTo(2L);
        assertThat(l.getEjercicio()).usingRecursiveComparison().isEqualTo(pressBanca());
        assertThat(l.getOrden()).isEqualTo(1);
        assertThat(l.getSeriesObjetivo()).isEqualTo(4);
        assertThat(l.getRepsObjetivo()).isEqualTo("8-12");
        assertThat(entityMapper.toDomainList(List.of(entity))).hasSize(1);
    }

    @Test
    @DisplayName("dominio -> entity de la linea apunta al ejercicio solo por su id y deja la rutina sin rellenar")
    void lineaDominioAEntity() {
        RutinaEjercicioEntity entity = lineaEntityMapper.toEntity(linea(20L, pressBanca(), 3, "5"));

        assertThat(entity.getEjercicio()).isNotNull();
        assertThat(entity.getEjercicio().getId()).isEqualTo(2L);
        assertThat(entity.getRutina()).isNull();
        assertThat(entity.getUsuarioId()).isEqualTo(1L);
        assertThat(entity.getOrden()).isEqualTo(3);
        assertThat(entity.getSeriesObjetivo()).isEqualTo(4);
        assertThat(entity.getRepsObjetivo()).isEqualTo("5");
        assertThat(lineaEntityMapper.toEntityList(List.of(linea(20L, pressBanca(), 1, "5")))).hasSize(1);
    }

    @Test
    @DisplayName("dominio -> entity de la rutina lleva sus lineas")
    void rutinaDominioAEntity() {
        RutinaEntity entity = entityMapper.toEntity(rutina());

        assertThat(entity.getId()).isEqualTo(8L);
        assertThat(entity.isActiva()).isTrue();
        assertThat(entity.getLineas()).hasSize(2);
        assertThat(entity.getLineas()).extracting(l -> l.getEjercicio().getId()).containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("request -> dominio no rellena id ni usuarioId (ni en las lineas) y lleva ejercicioId, orden, series y reps")
    void requestNoTrae() {
        RutinaRequestDtoMapper mapper = Mappers.getMapper(RutinaRequestDtoMapper.class);

        Rutina dominio = mapper.toDomain(new RutinaRequestDto("Push", "Empuje", false, List.of(
                new RutinaEjercicioRequestDto(2L, 1, 4, "8-12"),
                new RutinaEjercicioRequestDto(3L, 2, 3, "AMRAP"))));

        assertThat(dominio.getId()).isNull();
        assertThat(dominio.getUsuarioId()).isNull();
        assertThat(dominio.getNombre()).isEqualTo("Push");
        assertThat(dominio.getDescripcion()).isEqualTo("Empuje");
        assertThat(dominio.isActiva()).isFalse();
        assertThat(dominio.getLineas()).hasSize(2);
        assertThat(dominio.getLineas()).allSatisfy(l -> {
            assertThat(l.getId()).isNull();
            assertThat(l.getUsuarioId()).isNull();
            assertThat(l.getEjercicio()).isNull();
        });
        assertThat(dominio.getLineas()).extracting(RutinaEjercicio::getEjercicioId).containsExactly(2L, 3L);
        assertThat(dominio.getLineas()).extracting(RutinaEjercicio::getOrden).containsExactly(1, 2);
        assertThat(dominio.getLineas()).extracting(RutinaEjercicio::getSeriesObjetivo).containsExactly(4, 3);
        assertThat(dominio.getLineas()).extracting(RutinaEjercicio::getRepsObjetivo).containsExactly("8-12", "AMRAP");
    }

    @Test
    @DisplayName("dominio -> form DTO lleva las lineas con nombre y grupo del ejercicio, y nunca el usuarioId")
    void formDto() {
        RutinaFormDto dto = formMapper.toFormDto(rutina());

        assertThat(dto.id()).isEqualTo(8L);
        assertThat(dto.nombre()).isEqualTo("Push");
        assertThat(dto.descripcion()).isEqualTo("Empuje");
        assertThat(dto.activa()).isTrue();
        assertThat(dto.lineas()).hasSize(2);
        RutinaEjercicioFormDto primera = dto.lineas().get(0);
        assertThat(primera.id()).isEqualTo(20L);
        assertThat(primera.ejercicioId()).isEqualTo(2L);
        assertThat(primera.ejercicioNombre()).isEqualTo("Press banca");
        assertThat(primera.ejercicioGrupoMuscular()).isEqualTo("Pecho");
        assertThat(primera.orden()).isEqualTo(1);
        assertThat(primera.seriesObjetivo()).isEqualTo(4);
        assertThat(primera.repsObjetivo()).isEqualTo("8-12");
    }

    @Test
    @DisplayName("dominio -> list DTO lleva el numero de ejercicios y no las lineas, y funciona sobre listas")
    void listDto() {
        RutinaListDtoMapper mapper = Mappers.getMapper(RutinaListDtoMapper.class);

        RutinaListDto dto = mapper.toListDto(rutina());

        assertThat(dto.id()).isEqualTo(8L);
        assertThat(dto.nombre()).isEqualTo("Push");
        assertThat(dto.descripcion()).isEqualTo("Empuje");
        assertThat(dto.activa()).isTrue();
        assertThat(dto.numeroEjercicios()).isEqualTo(2);
        assertThat(mapper.toListDtoList(List.of(rutina(), rutina()))).hasSize(2);
    }

    @Test
    @DisplayName("filtro: query params -> filtro de dominio")
    void filtro() {
        RutinaFilterMapper mapper = Mappers.getMapper(RutinaFilterMapper.class);

        RutinaFilter filtro = mapper.toFilter(new RutinaFilterListDto(true));
        RutinaFilter vacio = mapper.toFilter(new RutinaFilterListDto(null));

        assertThat(filtro.getActiva()).isTrue();
        assertThat(vacio.getActiva()).isNull();
    }
}
