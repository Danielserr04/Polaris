package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.ObjetivoNutricional;
import com.polaris.fusion.infrastructure.persistence.ObjetivoNutricionalEntity;
import com.polaris.fusion.infrastructure.persistence.dto.in.ObjetivoNutricionalRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.ObjetivoNutricionalFormDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Los mappers de ObjetivoNutricional sobre sus implementaciones generadas por MapStruct. */
class ObjetivoNutricionalMappersTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 9, 1);

    private static ObjetivoNutricional objetivo() {
        return ObjetivoNutricional.builder().id(3L).usuarioId(1L).kcalDiarias(2400).proteinasObj(160)
                .carbosObj(250).grasasObj(70).vigenteDesde(DESDE).build();
    }

    @Test
    @DisplayName("entity <-> dominio conserva todos los campos, incluido el usuarioId")
    void entityIdaYVuelta() {
        ObjetivoNutricionalEntityMapper mapper = Mappers.getMapper(ObjetivoNutricionalEntityMapper.class);

        ObjetivoNutricionalEntity entity = mapper.toEntity(objetivo());
        ObjetivoNutricional vuelta = mapper.toDomain(entity);

        assertThat(entity.getUsuarioId()).isEqualTo(1L);
        assertThat(entity.getKcalDiarias()).isEqualTo(2400);
        assertThat(entity.getVigenteDesde()).isEqualTo(DESDE);
        assertThat(vuelta).usingRecursiveComparison().isEqualTo(objetivo());
        assertThat(mapper.toDomainList(List.of(entity))).hasSize(1);
    }

    @Test
    @DisplayName("request -> dominio no rellena id ni usuarioId: los pone el servicio")
    void requestNoTrae() {
        ObjetivoNutricional dominio = Mappers.getMapper(ObjetivoNutricionalRequestDtoMapper.class)
                .toDomain(new ObjetivoNutricionalRequestDto(2400, 160, 250, 70, DESDE));

        assertThat(dominio.getId()).isNull();
        assertThat(dominio.getUsuarioId()).isNull();
        assertThat(dominio.getKcalDiarias()).isEqualTo(2400);
        assertThat(dominio.getProteinasObj()).isEqualTo(160);
        assertThat(dominio.getCarbosObj()).isEqualTo(250);
        assertThat(dominio.getGrasasObj()).isEqualTo(70);
        assertThat(dominio.getVigenteDesde()).isEqualTo(DESDE);
    }

    @Test
    @DisplayName("dominio -> form DTO lleva los objetivos y la fecha de vigencia, y funciona sobre listas")
    void formDto() {
        ObjetivoNutricionalFormDtoMapper mapper = Mappers.getMapper(ObjetivoNutricionalFormDtoMapper.class);
        ObjetivoNutricionalFormDto esperado = new ObjetivoNutricionalFormDto(3L, 2400, 160, 250, 70, DESDE);

        assertThat(mapper.toFormDto(objetivo())).isEqualTo(esperado);
        assertThat(mapper.toFormDtoList(List.of(objetivo()))).containsExactly(esperado);
    }
}
