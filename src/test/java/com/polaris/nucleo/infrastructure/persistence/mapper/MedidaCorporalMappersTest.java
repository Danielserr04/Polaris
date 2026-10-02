package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.MedidaCorporal;
import com.polaris.nucleo.domain.model.MedidaCorporalFilter;
import com.polaris.nucleo.infrastructure.persistence.MedidaCorporalEntity;
import com.polaris.nucleo.infrastructure.persistence.dto.in.MedidaCorporalFilterListDto;
import com.polaris.nucleo.infrastructure.persistence.dto.in.MedidaCorporalRequestDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.MedidaCorporalFormDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.MedidaCorporalListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Los mappers de MedidaCorporal sobre sus implementaciones generadas por MapStruct. */
class MedidaCorporalMappersTest {

    private static final LocalDate HOY = LocalDate.of(2026, 9, 30);

    private static MedidaCorporal registro() {
        return MedidaCorporal.builder().id(5L).usuarioId(1L).fecha(HOY).cinturaCm(new BigDecimal("82.5"))
                .pechoCm(new BigDecimal("101.5")).notas("antes de entrenar").build();
    }

    @Test
    @DisplayName("entity <-> dominio conserva todos los campos y los decimales sin redondear")
    void entityIdaYVuelta() {
        MedidaCorporalEntityMapper mapper = Mappers.getMapper(MedidaCorporalEntityMapper.class);

        MedidaCorporalEntity entity = mapper.toEntity(registro());
        MedidaCorporal vuelta = mapper.toDomain(entity);

        assertThat(entity.getUsuarioId()).isEqualTo(1L);
        assertThat(entity.getCinturaCm()).isEqualTo(new BigDecimal("82.5"));
        assertThat(vuelta).usingRecursiveComparison().isEqualTo(registro());
    }

    @Test
    @DisplayName("entity -> dominio funciona sobre listas")
    void entityLista() {
        MedidaCorporalEntityMapper mapper = Mappers.getMapper(MedidaCorporalEntityMapper.class);

        List<MedidaCorporal> lista = mapper.toDomainList(List.of(mapper.toEntity(registro())));

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("request -> dominio no rellena id ni usuarioId: los pone el servicio")
    void requestNoTrae() {
        MedidaCorporalRequestDtoMapper mapper = Mappers.getMapper(MedidaCorporalRequestDtoMapper.class);

        MedidaCorporal dominio = mapper.toDomain(
                new MedidaCorporalRequestDto(HOY, null, null, new BigDecimal("82.5"), null, null, null, null, null, "notas"));

        assertThat(dominio.getId()).isNull();
        assertThat(dominio.getUsuarioId()).isNull();
        assertThat(dominio.getFecha()).isEqualTo(HOY);
        assertThat(dominio.getCinturaCm()).isEqualTo(new BigDecimal("82.5"));
        assertThat(dominio.getPechoCm()).isNull();
        assertThat(dominio.getNotas()).isEqualTo("notas");
    }

    @Test
    @DisplayName("dominio -> form DTO lleva las notas")
    void formDto() {
        MedidaCorporalFormDto dto = Mappers.getMapper(MedidaCorporalFormDtoMapper.class).toFormDto(registro());

        assertThat(dto).isEqualTo(new MedidaCorporalFormDto(5L, HOY, null, new BigDecimal("101.5"),
                new BigDecimal("82.5"), null, null, null, null, null, "antes de entrenar"));
    }

    @Test
    @DisplayName("dominio -> list DTO es la version ligera y funciona sobre listas")
    void listDto() {
        MedidaCorporalListDtoMapper mapper = Mappers.getMapper(MedidaCorporalListDtoMapper.class);

        MedidaCorporalListDto dto = mapper.toListDto(registro());

        assertThat(dto).isEqualTo(new MedidaCorporalListDto(5L, HOY, null, new BigDecimal("101.5"),
                new BigDecimal("82.5"), null, null, null, null, null));
        assertThat(mapper.toListDtoList(List.of(registro(), registro()))).hasSize(2);
    }

    @Test
    @DisplayName("filtro: query params -> filtro de dominio, con nulos si no se envian")
    void filtro() {
        MedidaCorporalFilterMapper mapper = Mappers.getMapper(MedidaCorporalFilterMapper.class);

        MedidaCorporalFilter completo = mapper.toFilter(new MedidaCorporalFilterListDto(HOY.minusDays(7), HOY));
        MedidaCorporalFilter vacio = mapper.toFilter(new MedidaCorporalFilterListDto(null, null));

        assertThat(completo.getDesde()).isEqualTo(HOY.minusDays(7));
        assertThat(completo.getHasta()).isEqualTo(HOY);
        assertThat(vacio.getDesde()).isNull();
        assertThat(vacio.getHasta()).isNull();
    }
}
