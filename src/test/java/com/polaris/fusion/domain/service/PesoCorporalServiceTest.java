package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.out.PesoCorporalPort;
import com.polaris.fusion.domain.model.PesoCorporal;
import com.polaris.fusion.domain.model.PesoCorporalFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PesoCorporalServiceTest {

    private static final Long USUARIO = 7L;

    @Mock
    private PesoCorporalPort port;

    @InjectMocks
    private PesoCorporalService service;

    @Test
    @DisplayName("list delega en el puerto con el usuario y el filtro")
    void listDelega() {
        PesoCorporalFilter filtro = PesoCorporalFilter.builder().desde(LocalDate.of(2026, 9, 1)).build();
        List<PesoCorporal> pesos = List.of(PesoCorporal.builder().fecha(LocalDate.of(2026, 9, 30))
                .pesoKg(new BigDecimal("78.50")).build());
        when(port.findAll(USUARIO, filtro)).thenReturn(pesos);

        assertThat(service.list(USUARIO, filtro)).isSameAs(pesos);
    }

    @Test
    @DisplayName("create delega en el puerto y devuelve lo que este devuelva")
    void createDelega() {
        PesoCorporal peso = PesoCorporal.builder().fecha(LocalDate.of(2026, 9, 30))
                .pesoKg(new BigDecimal("78.50")).build();
        PesoCorporal guardado = PesoCorporal.builder().fecha(peso.getFecha()).pesoKg(peso.getPesoKg()).build();
        when(port.registrar(USUARIO, peso)).thenReturn(guardado);

        assertThat(service.create(USUARIO, peso)).isSameAs(guardado);
        verify(port).registrar(USUARIO, peso);
    }
}
