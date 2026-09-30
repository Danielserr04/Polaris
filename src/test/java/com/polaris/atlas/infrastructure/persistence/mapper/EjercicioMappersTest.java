package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.EjercicioFilter;
import com.polaris.atlas.infrastructure.persistence.EjercicioEntity;
import com.polaris.atlas.infrastructure.persistence.dto.in.EjercicioFilterListDto;
import com.polaris.atlas.infrastructure.persistence.dto.in.EjercicioRequestDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.EjercicioFormDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.EjercicioListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Los mappers de Ejercicio sobre sus implementaciones generadas por MapStruct. */
class EjercicioMappersTest {

    private static Ejercicio propio() {
        return Ejercicio.builder().id(10L).usuarioId(1L).nombre("Flexiones").grupoMuscular("Pecho")
                .equipamiento("Peso corporal").build();
    }

    private static Ejercicio catalogo() {
        return Ejercicio.builder().id(11L).usuarioId(null).nombre("Sentadilla").grupoMuscular("Pierna").build();
    }

    @Test
    @DisplayName("entity <-> dominio conserva todos los campos, y el usuarioId nulo del catalogo")
    void entityIdaYVuelta() {
        EjercicioEntityMapper mapper = Mappers.getMapper(EjercicioEntityMapper.class);

        EjercicioEntity entity = mapper.toEntity(propio());
        EjercicioEntity entityCatalogo = mapper.toEntity(catalogo());

        assertThat(entity.getUsuarioId()).isEqualTo(1L);
        assertThat(entity.getEquipamiento()).isEqualTo("Peso corporal");
        assertThat(entityCatalogo.getUsuarioId()).isNull();
        assertThat(mapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(propio());
        assertThat(mapper.toDomain(entityCatalogo)).usingRecursiveComparison().isEqualTo(catalogo());
        assertThat(mapper.toDomainList(List.of(entity, entityCatalogo))).hasSize(2);
    }

    @Test
    @DisplayName("request -> dominio no rellena id ni usuarioId: los pone el servicio")
    void requestNoTrae() {
        Ejercicio dominio = Mappers.getMapper(EjercicioRequestDtoMapper.class)
                .toDomain(new EjercicioRequestDto("Dominadas", "Espalda", "Barra"));

        assertThat(dominio.getId()).isNull();
        assertThat(dominio.getUsuarioId()).isNull();
        assertThat(dominio.getNombre()).isEqualTo("Dominadas");
        assertThat(dominio.getGrupoMuscular()).isEqualTo("Espalda");
        assertThat(dominio.getEquipamiento()).isEqualTo("Barra");
    }

    @Test
    @DisplayName("dominio -> form DTO y list DTO: esPropio sale del usuarioId, que no llega al DTO")
    void dtosDeSalida() {
        EjercicioFormDtoMapper formMapper = Mappers.getMapper(EjercicioFormDtoMapper.class);
        assertThat(formMapper.toFormDto(propio()))
                .isEqualTo(new EjercicioFormDto(10L, "Flexiones", "Pecho", "Peso corporal", true));
        assertThat(formMapper.toFormDto(catalogo()))
                .isEqualTo(new EjercicioFormDto(11L, "Sentadilla", "Pierna", null, false));

        EjercicioListDtoMapper listMapper = Mappers.getMapper(EjercicioListDtoMapper.class);
        EjercicioListDto esperadoPropio = new EjercicioListDto(10L, "Flexiones", "Pecho", "Peso corporal", true);
        EjercicioListDto esperadoCatalogo = new EjercicioListDto(11L, "Sentadilla", "Pierna", null, false);
        assertThat(listMapper.toListDto(propio())).isEqualTo(esperadoPropio);
        assertThat(listMapper.toListDtoList(List.of(propio(), catalogo())))
                .containsExactly(esperadoPropio, esperadoCatalogo);
    }

    @Test
    @DisplayName("filtro: ?q= pasa a texto y ?grupoMuscular= se conserva; sin params quedan nulos")
    void filtro() {
        EjercicioFilterMapper mapper = Mappers.getMapper(EjercicioFilterMapper.class);

        EjercicioFilter completo = mapper.toFilter(new EjercicioFilterListDto("Pecho", "press"));
        EjercicioFilter vacio = mapper.toFilter(new EjercicioFilterListDto(null, null));

        assertThat(completo.getGrupoMuscular()).isEqualTo("Pecho");
        assertThat(completo.getTexto()).isEqualTo("press");
        assertThat(vacio.getGrupoMuscular()).isNull();
        assertThat(vacio.getTexto()).isNull();
    }
}
