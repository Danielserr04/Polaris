package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.out.EstadisticasLogrosPort;
import com.polaris.fusion.domain.model.EstadisticasLogros;
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
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Logros de Fusion. Ver docs/decisiones/044-logros-globales-con-fecha-calculada.md. */
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
        Map<String, Logro> l = logros(EstadisticasLogros.builder().fechasComida(List.of()).build());

        assertThat(l).hasSize(LogroService.CATALOGO.size());
        assertThat(l.values()).noneMatch(Logro::isConseguido);
    }

    @Test
    @DisplayName("comidas, dias y racha salen de las fechas; recetas y planes cuentan sin fecha")
    void metricas() {
        // Tres comidas al dia durante 8 dias seguidos.
        List<LocalDate> fechas = IntStream.range(0, 24).mapToObj(i -> D1.plusDays(i / 3)).toList();
        Map<String, Logro> l = logros(EstadisticasLogros.builder().fechasComida(fechas)
                .numeroRecetas(3).numeroPlanes(0).primerObjetivo(D1.plusDays(2)).build());

        assertThat(l.get("COMIDAS_100").getProgreso()).isEqualTo(24);
        assertThat(l.get("DIAS_30").getProgreso()).isEqualTo(8);
        assertThat(l.get("RACHA_7").getFechaConseguido()).isEqualTo(D1.plusDays(6));
        assertThat(l.get("OBJETIVO").getFechaConseguido()).isEqualTo(D1.plusDays(2));
        assertThat(l.get("PRIMERA_RECETA").isConseguido()).isTrue();
        assertThat(l.get("PRIMERA_RECETA").getFechaConseguido()).isNull();
        assertThat(l.get("PRIMER_PLAN").isConseguido()).isFalse();
    }
}
