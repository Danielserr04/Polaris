package com.polaris.atlas.infrastructure.nucleo;

import com.polaris.atlas.domain.model.PesoCorporal;
import com.polaris.atlas.domain.model.PesoCorporalFilter;
import com.polaris.nucleo.application.in.CreateRegistroPesoInterface;
import com.polaris.nucleo.application.in.ListRegistroPesoInterface;
import com.polaris.nucleo.domain.model.RegistroPeso;
import com.polaris.nucleo.domain.model.RegistroPesoFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Los casos de uso de Nucleo van con mock; el mapper es el real (MapStruct),
 * porque lo que se prueba es justo la traduccion entre los dos modelos.
 */
@ExtendWith(MockitoExtension.class)
class NucleoPesoCorporalAdapterTest {

    private static final Long USUARIO = 7L;
    private static final LocalDate DIA = LocalDate.of(2026, 9, 30);

    @Mock
    private ListRegistroPesoInterface listRegistroPeso;

    @Mock
    private CreateRegistroPesoInterface createRegistroPeso;

    private NucleoPesoCorporalAdapter adapter() {
        return new NucleoPesoCorporalAdapter(listRegistroPeso, createRegistroPeso,
                new AtlasPesoCorporalNucleoMapperImpl());
    }

    @Test
    @DisplayName("findAll traduce el filtro, pide al caso de uso de Nucleo con el usuario y devuelve el modelo de Atlas")
    void findAllTraduceFiltroYResultado() {
        when(listRegistroPeso.list(any(), any())).thenReturn(List.of(
                RegistroPeso.builder().id(1L).usuarioId(USUARIO).fecha(DIA)
                        .pesoKg(new BigDecimal("78.50")).grasaPct(new BigDecimal("15.2")).notas("tras entrenar").build(),
                RegistroPeso.builder().id(2L).usuarioId(USUARIO).fecha(DIA.minusDays(1))
                        .pesoKg(new BigDecimal("78.90")).build()));

        List<PesoCorporal> pesos = adapter().findAll(USUARIO,
                PesoCorporalFilter.builder().desde(DIA.minusDays(7)).hasta(DIA).build());

        ArgumentCaptor<RegistroPesoFilter> filtro = ArgumentCaptor.forClass(RegistroPesoFilter.class);
        verify(listRegistroPeso).list(org.mockito.ArgumentMatchers.eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getDesde()).isEqualTo(DIA.minusDays(7));
        assertThat(filtro.getValue().getHasta()).isEqualTo(DIA);

        // Se conserva el orden que decide Nucleo (mas reciente primero).
        assertThat(pesos).hasSize(2);
        assertThat(pesos.get(0).getFecha()).isEqualTo(DIA);
        assertThat(pesos.get(0).getPesoKg()).isEqualByComparingTo("78.50");
        assertThat(pesos.get(0).getGrasaPct()).isEqualByComparingTo("15.2");
        assertThat(pesos.get(0).getNotas()).isEqualTo("tras entrenar");
        assertThat(pesos.get(1).getGrasaPct()).isNull();
    }

    @Test
    @DisplayName("findAll con filtro vacio pide sin rango")
    void findAllSinRango() {
        when(listRegistroPeso.list(any(), any())).thenReturn(List.of());

        assertThat(adapter().findAll(USUARIO, PesoCorporalFilter.builder().build())).isEmpty();

        ArgumentCaptor<RegistroPesoFilter> filtro = ArgumentCaptor.forClass(RegistroPesoFilter.class);
        verify(listRegistroPeso).list(org.mockito.ArgumentMatchers.eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getDesde()).isNull();
        assertThat(filtro.getValue().getHasta()).isNull();
    }

    @Test
    @DisplayName("registrar crea en Nucleo sin id ni usuario en el modelo (los pone Nucleo) y devuelve el resultado traducido")
    void registrarLlamaAlCasoDeUsoDeNucleo() {
        when(createRegistroPeso.create(any(), any())).thenAnswer(invocacion -> {
            RegistroPeso enviado = invocacion.getArgument(1);
            enviado.setId(42L);
            enviado.setUsuarioId(invocacion.getArgument(0));
            return enviado;
        });

        PesoCorporal apuntado = adapter().registrar(USUARIO, PesoCorporal.builder().fecha(DIA)
                .pesoKg(new BigDecimal("78.50")).grasaPct(new BigDecimal("15.2")).notas("ayuno").build());

        ArgumentCaptor<RegistroPeso> registro = ArgumentCaptor.forClass(RegistroPeso.class);
        verify(createRegistroPeso).create(org.mockito.ArgumentMatchers.eq(USUARIO), registro.capture());
        assertThat(registro.getValue().getFecha()).isEqualTo(DIA);
        assertThat(registro.getValue().getPesoKg()).isEqualByComparingTo("78.50");
        assertThat(registro.getValue().getGrasaPct()).isEqualByComparingTo("15.2");
        assertThat(registro.getValue().getNotas()).isEqualTo("ayuno");

        assertThat(apuntado.getFecha()).isEqualTo(DIA);
        assertThat(apuntado.getPesoKg()).isEqualByComparingTo("78.50");
        assertThat(apuntado.getGrasaPct()).isEqualByComparingTo("15.2");
        assertThat(apuntado.getNotas()).isEqualTo("ayuno");
    }

    @Test
    @DisplayName("las excepciones de Nucleo (409, 404...) se propagan tal cual")
    void propagaExcepciones() {
        when(createRegistroPeso.create(any(), any())).thenThrow(new IllegalStateException("fallo de nucleo"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> adapter().registrar(USUARIO,
                        PesoCorporal.builder().fecha(DIA).pesoKg(BigDecimal.ONE).build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("fallo de nucleo");
    }
}
