package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.RegistroPeso;
import com.polaris.nucleo.domain.model.RegistroPesoFilter;
import com.polaris.nucleo.infrastructure.persistence.RegistroPesoEntity;
import com.polaris.nucleo.infrastructure.persistence.dto.in.RegistroPesoFilterListDto;
import com.polaris.nucleo.infrastructure.persistence.dto.in.RegistroPesoRequestDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.RegistroPesoFormDto;
import com.polaris.nucleo.infrastructure.persistence.dto.out.RegistroPesoListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Los mappers de RegistroPeso sobre sus implementaciones generadas por MapStruct. */
class RegistroPesoMappersTest {

    private static final LocalDate HOY = LocalDate.of(2026, 9, 30);

    private static RegistroPeso registro() {
        return RegistroPeso.builder().id(5L).usuarioId(1L).fecha(HOY).pesoKg(new BigDecimal("78.50"))
                .grasaPct(new BigDecimal("18.5")).notas("en ayunas").build();
    }

    @Test
    @DisplayName("entity <-> dominio conserva todos los campos y los decimales sin redondear")
    void entityIdaYVuelta() {
        RegistroPesoEntityMapper mapper = Mappers.getMapper(RegistroPesoEntityMapper.class);

        RegistroPesoEntity entity = mapper.toEntity(registro());
        RegistroPeso vuelta = mapper.toDomain(entity);

        assertThat(entity.getUsuarioId()).isEqualTo(1L);
        assertThat(entity.getPesoKg()).isEqualTo(new BigDecimal("78.50"));
        assertThat(vuelta).usingRecursiveComparison().isEqualTo(registro());
    }

    @Test
    @DisplayName("entity -> dominio funciona sobre listas")
    void entityLista() {
        RegistroPesoEntityMapper mapper = Mappers.getMapper(RegistroPesoEntityMapper.class);

        List<RegistroPeso> lista = mapper.toDomainList(List.of(mapper.toEntity(registro())));

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("request -> dominio no rellena id ni usuarioId: los pone el servicio")
    void requestNoTrae() {
        RegistroPesoRequestDtoMapper mapper = Mappers.getMapper(RegistroPesoRequestDtoMapper.class);

        RegistroPeso dominio = mapper.toDomain(
                new RegistroPesoRequestDto(HOY, new BigDecimal("78.50"), null, "notas"));

        assertThat(dominio.getId()).isNull();
        assertThat(dominio.getUsuarioId()).isNull();
        assertThat(dominio.getFecha()).isEqualTo(HOY);
        assertThat(dominio.getPesoKg()).isEqualTo(new BigDecimal("78.50"));
        assertThat(dominio.getGrasaPct()).isNull();
        assertThat(dominio.getNotas()).isEqualTo("notas");
    }

    @Test
    @DisplayName("dominio -> form DTO lleva las notas")
    void formDto() {
        RegistroPesoFormDto dto = Mappers.getMapper(RegistroPesoFormDtoMapper.class).toFormDto(registro());

        assertThat(dto).isEqualTo(new RegistroPesoFormDto(5L, HOY, new BigDecimal("78.50"),
                new BigDecimal("18.5"), "en ayunas"));
    }

    @Test
    @DisplayName("dominio -> list DTO es la version ligera y funciona sobre listas")
    void listDto() {
        RegistroPesoListDtoMapper mapper = Mappers.getMapper(RegistroPesoListDtoMapper.class);

        RegistroPesoListDto dto = mapper.toListDto(registro());

        assertThat(dto).isEqualTo(new RegistroPesoListDto(5L, HOY, new BigDecimal("78.50"), new BigDecimal("18.5")));
        assertThat(mapper.toListDtoList(List.of(registro(), registro()))).hasSize(2);
    }

    @Test
    @DisplayName("filtro: query params -> filtro de dominio, con nulos si no se envian")
    void filtro() {
        RegistroPesoFilterMapper mapper = Mappers.getMapper(RegistroPesoFilterMapper.class);

        RegistroPesoFilter completo = mapper.toFilter(new RegistroPesoFilterListDto(HOY.minusDays(7), HOY));
        RegistroPesoFilter vacio = mapper.toFilter(new RegistroPesoFilterListDto(null, null));

        assertThat(completo.getDesde()).isEqualTo(HOY.minusDays(7));
        assertThat(completo.getHasta()).isEqualTo(HOY);
        assertThat(vacio.getDesde()).isNull();
        assertThat(vacio.getHasta()).isNull();
    }
}
