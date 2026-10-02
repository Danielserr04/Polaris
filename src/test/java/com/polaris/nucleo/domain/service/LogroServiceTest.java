package com.polaris.nucleo.domain.service;

import com.polaris.nucleo.application.out.EstadisticasLogrosPort;
import com.polaris.nucleo.domain.model.EstadisticasLogros;
import com.polaris.shared.logro.Logro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Logros del cuerpo y el perfil. Ver docs/decisiones/044-logros-globales-con-fecha-calculada.md. */
@ExtendWith(MockitoExtension.class)
class LogroServiceTest {

    private static final Long USUARIO = 1L;
    private static final LocalDate D1 = LocalDate.of(2026, 9, 1);

    @Mock
    private EstadisticasLogrosPort estadisticasPort;

    @InjectMocks
    private LogroService service;

    private Map<String, Logro> logros(EstadisticasLogros e) {
        when(estadisticasPort.find(USUARIO)).thenReturn(e);
        return service.list(USUARIO).stream().collect(Collectors.toMap(Logro::getCodigo, Function.identity()));
    }

    @Test
    @DisplayName("sin datos: todo el catalogo y nada conseguido")
    void usuarioNuevo() {
        Map<String, Logro> l = logros(EstadisticasLogros.builder().fechasPeso(List.of()).fechasMedida(List.of()).build());

        assertThat(l).hasSize(LogroService.CATALOGO.size());
        assertThat(l.values()).noneMatch(Logro::isConseguido);
    }

    @Test
    @DisplayName("pesajes, racha de dias, medidas y perfil completo sin fecha")
    void metricas() {
        List<LocalDate> pesos = List.of(D1, D1.plusDays(1), D1.plusDays(2), D1.plusDays(3), D1.plusDays(4),
                D1.plusDays(5), D1.plusDays(6), D1.plusDays(20));
        Map<String, Logro> l = logros(EstadisticasLogros.builder().fechasPeso(pesos)
                .fechasMedida(List.of(D1.plusDays(3))).camposPerfil(4).build());

        assertThat(l.get("PRIMER_PESAJE").getFechaConseguido()).isEqualTo(D1);
        assertThat(l.get("PESAJES_30").getProgreso()).isEqualTo(8);
        assertThat(l.get("RACHA_PESAJE_7").getFechaConseguido()).isEqualTo(D1.plusDays(6));
        assertThat(l.get("PRIMERA_MEDIDA").getFechaConseguido()).isEqualTo(D1.plusDays(3));
        assertThat(l.get("PERFIL_COMPLETO").isConseguido()).isTrue();
        assertThat(l.get("PERFIL_COMPLETO").getFechaConseguido()).isNull();
    }
}
