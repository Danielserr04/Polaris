package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.ProgresionFilter;
import com.polaris.atlas.domain.model.ProgresionSesion;
import com.polaris.atlas.domain.model.RecordEjercicio;
import com.polaris.atlas.infrastructure.persistence.dto.in.ProgresionFilterListDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.ProgresionSesionDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.RecordEjercicioDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Los mappers de progresion y records sobre sus implementaciones generadas por MapStruct. */
class ProgresionMappersTest {

    @Test
    @DisplayName("el filtro del DTO pasa entero al filtro de dominio")
    void filtro() {
        ProgresionFilter filtro = new ProgresionFilterMapperImpl().toFilter(
                new ProgresionFilterListDto(5L, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)));

        assertThat(filtro.getEjercicioId()).isEqualTo(5L);
        assertThat(filtro.getDesde()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(filtro.getHasta()).isEqualTo(LocalDate.of(2026, 9, 30));
    }

    @Test
    @DisplayName("una sesion de progresion pasa a DTO conservando los BigDecimal y su escala")
    void progresionDto() {
        ProgresionSesion sesion = ProgresionSesion.builder().sesionId(10L).fecha(LocalDate.of(2026, 9, 2))
                .volumen(new BigDecimal("1775.00")).numeroSeries(3).pesoMaximo(new BigDecimal("82.50"))
                .repsTotales(22).build();

        ProgresionSesionDto dto = new ProgresionSesionDtoMapperImpl().toDto(sesion);

        assertThat(dto.sesionId()).isEqualTo(10L);
        assertThat(dto.fecha()).isEqualTo(LocalDate.of(2026, 9, 2));
        assertThat(dto.volumen()).isEqualTo(new BigDecimal("1775.00"));
        assertThat(dto.numeroSeries()).isEqualTo(3);
        assertThat(dto.pesoMaximo()).isEqualTo(new BigDecimal("82.50"));
        assertThat(dto.repsTotales()).isEqualTo(22L);
        assertThat(new ProgresionSesionDtoMapperImpl().toDtoList(List.of(sesion))).hasSize(1);
    }

    @Test
    @DisplayName("un record pasa a DTO; sin volumen los campos de volumen quedan nulos")
    void recordDto() {
        RecordEjercicio completo = RecordEjercicio.builder().ejercicioId(1L).ejercicioNombre("Press banca")
                .ejercicioGrupoMuscular("Pecho").pesoMaximo(new BigDecimal("100.00")).repsPesoMaximo(5)
                .fechaPesoMaximo(LocalDate.of(2026, 9, 8)).volumenMaximoSesion(new BigDecimal("1775.00"))
                .fechaVolumenMaximo(LocalDate.of(2026, 9, 1)).build();
        RecordEjercicio sinVolumen = RecordEjercicio.builder().ejercicioId(2L).ejercicioNombre("Dominadas")
                .ejercicioGrupoMuscular("Espalda").pesoMaximo(new BigDecimal("0.00")).repsPesoMaximo(12)
                .fechaPesoMaximo(LocalDate.of(2026, 9, 15)).build();
        RecordEjercicioDtoMapperImpl mapper = new RecordEjercicioDtoMapperImpl();

        RecordEjercicioDto dto = mapper.toDto(completo);
        assertThat(dto.ejercicioNombre()).isEqualTo("Press banca");
        assertThat(dto.pesoMaximo()).isEqualTo(new BigDecimal("100.00"));
        assertThat(dto.repsPesoMaximo()).isEqualTo(5);
        assertThat(dto.volumenMaximoSesion()).isEqualTo(new BigDecimal("1775.00"));
        assertThat(dto.fechaVolumenMaximo()).isEqualTo(LocalDate.of(2026, 9, 1));

        List<RecordEjercicioDto> lista = mapper.toDtoList(List.of(completo, sinVolumen));
        assertThat(lista).hasSize(2);
        assertThat(lista.get(1).volumenMaximoSesion()).isNull();
        assertThat(lista.get(1).fechaVolumenMaximo()).isNull();
    }
}
