package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.out.EstadisticasEntrenoPort;
import com.polaris.atlas.domain.model.EstadisticasEntreno;
import com.polaris.shared.logro.CalculoLogros.FechaImporte;
import com.polaris.shared.logro.Logro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Los logros de Atlas se calculan al vuelo para el usuario del JWT, con la
 * fecha en que se consiguieron. Ver docs/decisiones/043-logros-calculados-y-metas.md
 * y docs/decisiones/044-logros-globales-con-fecha-calculada.md.
 */
@ExtendWith(MockitoExtension.class)
class LogroServiceTest {

    private static final Long USUARIO = 1L;

    @Mock
    private EstadisticasEntrenoPort estadisticasPort;

    @InjectMocks
    private LogroService service;

    private Map<String, Logro> logros(List<LocalDate> sesiones, List<LocalDate> ejercicios, List<FechaImporte> volumen) {
        when(estadisticasPort.find(USUARIO)).thenReturn(EstadisticasEntreno.builder().fechasSesion(sesiones)
                .primerUsoEjercicios(ejercicios).volumenPorSesion(volumen).build());
        return service.list(USUARIO).stream().collect(Collectors.toMap(Logro::getCodigo, Function.identity()));
    }

    @Test
    @DisplayName("sin entrenos: todo el catalogo, nada conseguido, progreso 0 y sin fechas")
    void usuarioNuevo() {
        Map<String, Logro> l = logros(List.of(), List.of(), List.of());

        assertThat(l).hasSize(LogroService.CATALOGO.size());
        assertThat(l.values()).noneMatch(Logro::isConseguido);
        assertThat(l.values()).allMatch(x -> x.getProgreso() == 0 && x.getFechaConseguido() == null);
    }

    @Test
    @DisplayName("cada metrica alimenta sus logros y la fecha es la del paso que llega al objetivo")
    void progresoYFechaPorMetrica() {
        LocalDate inicio = LocalDate.of(2026, 9, 1);
        List<LocalDate> doceSesiones = IntStream.range(0, 12).mapToObj(inicio::plusDays).toList();
        Map<String, Logro> l = logros(doceSesiones,
                Collections.nCopies(7, inicio),
                List.of(new FechaImporte(inicio, new BigDecimal("6000")),
                        new FechaImporte(inicio.plusDays(3), new BigDecimal("4999.99")),
                        new FechaImporte(inicio.plusDays(5), new BigDecimal("0.01"))));

        assertThat(l.get("PRIMER_ENTRENO").getFechaConseguido()).isEqualTo(inicio);
        assertThat(l.get("ENTRENOS_10").getFechaConseguido()).isEqualTo(inicio.plusDays(9));
        assertThat(l.get("ENTRENOS_50").getProgreso()).isEqualTo(12);
        assertThat(l.get("ENTRENOS_50").isConseguido()).isFalse();
        assertThat(l.get("EJERCICIOS_10").getProgreso()).isEqualTo(7);
        // 6.000 + 4.999,99 kg son 10 t enteras el dia 3; el ultimo centimo completa la 11.
        assertThat(l.get("TONELADAS_10").getProgreso()).isEqualTo(11);
        assertThat(l.get("TONELADAS_10").getFechaConseguido()).isEqualTo(inicio.plusDays(3));
    }

    @Test
    @DisplayName("la racha es la mas larga de semanas seguidas, aunque ya se haya cortado, y se fecha al llegar")
    void rachaMasLarga() {
        List<LocalDate> fechas = List.of(
                // Semanas del 7, 14, 21 y 28 de septiembre de 2026 (lunes): 4 seguidas, dos dias en la misma.
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 13), LocalDate.of(2026, 9, 15),
                LocalDate.of(2026, 9, 21), LocalDate.of(2026, 10, 4),
                // Hueco y luego 2 seguidas.
                LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 26));
        Map<String, Logro> l = logros(fechas, List.of(), List.of());

        assertThat(l.get("RACHA_4").getProgreso()).isEqualTo(4);
        // La cuarta semana es la del 28 de septiembre; se entreno el domingo 4 de octubre.
        assertThat(l.get("RACHA_4").getFechaConseguido()).isEqualTo(LocalDate.of(2026, 10, 4));
        assertThat(l.get("RACHA_12").isConseguido()).isFalse();
    }
}
