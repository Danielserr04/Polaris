package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.NivelActividad;
import com.polaris.nucleo.domain.model.Perfil;
import com.polaris.nucleo.domain.model.PerfilFilter;
import com.polaris.nucleo.domain.model.Sexo;
import com.polaris.nucleo.infrastructure.persistence.PerfilEntity;
import com.polaris.nucleo.infrastructure.persistence.dto.in.PerfilFilterListDto;
import com.polaris.nucleo.infrastructure.persistence.dto.in.PerfilRequestDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.PerfilFormDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.PerfilListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Los mappers de Perfil sobre sus implementaciones generadas por MapStruct. */
class PerfilMappersTest {

    private static final LocalDate NACIMIENTO = LocalDate.of(1990, 5, 17);

    private static Perfil perfil() {
        return Perfil.builder().id(3L).usuarioId(1L).alturaCm(180).fechaNacimiento(NACIMIENTO)
                .sexo(Sexo.HOMBRE).nivelActividad(NivelActividad.MODERADO).build();
    }

    @Test
    @DisplayName("entity <-> dominio conserva todos los campos, incluido el usuarioId")
    void entityIdaYVuelta() {
        PerfilEntityMapper mapper = Mappers.getMapper(PerfilEntityMapper.class);

        PerfilEntity entity = mapper.toEntity(perfil());
        Perfil vuelta = mapper.toDomain(entity);

        assertThat(entity.getUsuarioId()).isEqualTo(1L);
        assertThat(entity.getAlturaCm()).isEqualTo(180);
        assertThat(vuelta).usingRecursiveComparison().isEqualTo(perfil());
    }

    @Test
    @DisplayName("un perfil a medias (todo nulo salvo usuarioId) se mapea sin fallar")
    void perfilAMedias() {
        PerfilEntityMapper mapper = Mappers.getMapper(PerfilEntityMapper.class);

        Perfil dominio = mapper.toDomain(PerfilEntity.builder().usuarioId(1L).build());

        assertThat(dominio.getUsuarioId()).isEqualTo(1L);
        assertThat(dominio.getAlturaCm()).isNull();
        assertThat(dominio.getSexo()).isNull();
    }

    @Test
    @DisplayName("request -> dominio no rellena id ni usuarioId: los pone el servicio")
    void requestNoTrae() {
        PerfilRequestDtoMapper mapper = Mappers.getMapper(PerfilRequestDtoMapper.class);

        Perfil dominio = mapper.toDomain(new PerfilRequestDto(180, NACIMIENTO, Sexo.MUJER, NivelActividad.ALTO));

        assertThat(dominio.getId()).isNull();
        assertThat(dominio.getUsuarioId()).isNull();
        assertThat(dominio.getAlturaCm()).isEqualTo(180);
        assertThat(dominio.getFechaNacimiento()).isEqualTo(NACIMIENTO);
        assertThat(dominio.getSexo()).isEqualTo(Sexo.MUJER);
        assertThat(dominio.getNivelActividad()).isEqualTo(NivelActividad.ALTO);
    }

    @Test
    @DisplayName("dominio -> form DTO lleva la ficha completa y no expone el usuarioId")
    void formDto() {
        PerfilFormDto dto = Mappers.getMapper(PerfilFormDtoMapper.class).toFormDto(perfil());

        assertThat(dto).isEqualTo(new PerfilFormDto(3L, 180, NACIMIENTO, Sexo.HOMBRE, NivelActividad.MODERADO));
    }

    @Test
    @DisplayName("dominio -> list DTO solo lleva el id (Perfil no tiene listado)")
    void listDto() {
        assertThat(Mappers.getMapper(PerfilListDtoMapper.class).toListDto(perfil())).isEqualTo(new PerfilListDto(3L));
    }

    @Test
    @DisplayName("el filtro de Perfil esta vacio y se mapea a un filtro vacio")
    void filtroVacio() {
        PerfilFilter filtro = Mappers.getMapper(PerfilFilterMapper.class).toFilter(new PerfilFilterListDto());

        assertThat(filtro).isNotNull();
    }
}
