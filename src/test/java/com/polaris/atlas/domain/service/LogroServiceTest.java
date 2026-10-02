package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.out.EstadisticasEntrenoPort;
import com.polaris.atlas.application.out.PesoCorporalPort;
import com.polaris.atlas.domain.model.EstadisticasEntreno;
import com.polaris.atlas.domain.model.Logro;
import com.polaris.atlas.domain.model.PesoCorporal;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Los logros se calculan al vuelo para el usuario del JWT. Ver
 * docs/decisiones/043-logros-calculados-y-metas.md.
 */
@ExtendWith(MockitoExtension.class)
class LogroServiceTest {

    private static final Long USUARIO = 1L;

    @Mock
    private EstadisticasEntrenoPort estadisticasPort;

    @Mock
    private PesoCorporalPort pesoCorporalPort;

    @InjectMocks
    private LogroService service;

    private Map<String, Logro> logros(long sesiones, long ejercicios, String volumen, List<LocalDate> fechas, int pesajes) {
        when(estadisticasPort.find(USUARIO)).thenReturn(EstadisticasEntreno.builder().numeroSesiones(sesiones)
                .ejerciciosDistintos(ejercicios).volumenTotal(new BigDecimal(volumen)).fechasSesion(fechas).build());
        when(pesoCorporalPort.findAll(eq(USUARIO), any()))
                .thenReturn(Collections.nCopies(pesajes, PesoCorporal.builder().build()));
        return service.list(USUARIO).stream().collect(Collectors.toMap(Logro::getCodigo, Function.identity()));
    }

    @Test
    @DisplayName("sin entrenos: todo el catalogo, nada conseguido y progreso 0")
    void usuarioNuevo() {
        Map<String, Logro> l = logros(0, 0, "0", List.of(), 0);

        assertThat(l).hasSize(LogroService.CATALOGO.size());
        assertThat(l.values()).noneMatch(Logro::isConseguido);
        assertThat(l.values()).allMatch(x -> x.getProgreso() == 0);
    }

    @Test
    @DisplayName("cada metrica alimenta sus logros: sesiones, ejercicios, toneladas enteras y pesajes")
    void progresoPorMetrica() {
        Map<String, Logro> l = logros(12, 7, "10999.99", List.of(LocalDate.of(2026, 9, 1)), 30);

        assertThat(l.get("PRIMER_ENTRENO").isConseguido()).isTrue();
        assertThat(l.get("ENTRENOS_10").isConseguido()).isTrue();
        assertThat(l.get("ENTRENOS_50").getProgreso()).isEqualTo(12);
        assertThat(l.get("ENTRENOS_50").isConseguido()).isFalse();
        assertThat(l.get("EJERCICIOS_10").getProgreso()).isEqualTo(7);
        // 10.999,99 kg son 10 t enteras: justo el logro de 10 t.
        assertThat(l.get("TONELADAS_10").getProgreso()).isEqualTo(10);
        assertThat(l.get("TONELADAS_10").isConseguido()).isTrue();
        assertThat(l.get("PESAJES_30").isConseguido()).isTrue();
    }

    @Test
    @DisplayName("la racha es la mas larga de semanas seguidas, de lunes a domingo, aunque ya se haya cortado")
    void rachaMasLarga() {
        List<LocalDate> fechas = List.of(
                // Semanas del 7, 14, 21 y 28 de septiembre de 2026 (lunes): 4 seguidas, dos dias en la misma.
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 13), LocalDate.of(2026, 9, 15),
                LocalDate.of(2026, 9, 21), LocalDate.of(2026, 10, 4),
                // Hueco y luego 2 seguidas.
                LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 26));

        assertThat(LogroService.rachaMasLarga(fechas)).isEqualTo(4);
        assertThat(LogroService.rachaMasLarga(List.of())).isZero();
    }
}
