package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.GastoCategoria;
import com.polaris.kuiper.domain.model.ResumenMensual;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.kuiper.infrastructure.persistence.dto.out.GastoCategoriaDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.ResumenMensualDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResumenMensualDtoMapperTest {

    private final ResumenMensualDtoMapper mapper = Mappers.getMapper(ResumenMensualDtoMapper.class);

    private static Categoria comida() {
        return Categoria.builder().id(10L).usuarioId(1L).nombre("Comida").color("#FF8800").icono("utensils")
                .tipo(TipoMovimiento.GASTO).build();
    }

    @Test
    @DisplayName("el periodo sale como texto yyyy-MM y las filas aplanan la categoria")
    void resumenCompleto() {
        ResumenMensual resumen = ResumenMensual.builder()
                .periodo(YearMonth.of(2026, 9))
                .ingresos(new BigDecimal("1500.00")).gastos(new BigDecimal("80.00"))
                .balance(new BigDecimal("1420.00"))
                .gastoPorCategoria(List.of(GastoCategoria.builder().categoria(comida())
                        .gastado(new BigDecimal("80.00")).limiteMensual(new BigDecimal("250.00"))
                        .restante(new BigDecimal("170.00")).build()))
                .build();

        ResumenMensualDto dto = mapper.toDto(resumen);

        assertThat(dto.periodo()).isEqualTo("2026-09");
        assertThat(dto.ingresos()).isEqualTo(new BigDecimal("1500.00"));
        assertThat(dto.gastos()).isEqualTo(new BigDecimal("80.00"));
        assertThat(dto.balance()).isEqualTo(new BigDecimal("1420.00"));
        assertThat(dto.gastoPorCategoria()).containsExactly(new GastoCategoriaDto(10L, "Comida", "#FF8800",
                "utensils", new BigDecimal("80.00"), new BigDecimal("250.00"), new BigDecimal("170.00")));
    }

    @Test
    @DisplayName("una fila sin presupuesto conserva limite y restante nulos")
    void filaSinPresupuesto() {
        GastoCategoriaDto dto = mapper.toDto(GastoCategoria.builder().categoria(comida())
                .gastado(new BigDecimal("12.50")).build());

        assertThat(dto.limiteMensual()).isNull();
        assertThat(dto.restante()).isNull();
        assertThat(dto.gastado()).isEqualTo(new BigDecimal("12.50"));
    }

    @Test
    @DisplayName("un periodo nulo sale como nulo")
    void periodoNulo() {
        assertThat(mapper.toDto(ResumenMensual.builder().build()).periodo()).isNull();
    }
}
