package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.out.EstadisticasLogrosPort;
import com.polaris.kuiper.domain.model.EstadisticasLogros;
import com.polaris.kuiper.domain.model.EstadisticasLogros.MetaLogro;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Logros de Kuiper. Ver docs/decisiones/044-logros-globales-con-fecha-calculada.md. */
@ExtendWith(MockitoExtension.class)
class LogroServiceTest {

    private static final Long USUARIO = 1L;
    private static final LocalDate HOY = LocalDate.of(2026, 10, 2);

    @Mock
    private EstadisticasLogrosPort estadisticasPort;

    @InjectMocks
    private LogroService service;

    private Map<String, Logro> logros(EstadisticasLogros e) {
        when(estadisticasPort.find(USUARIO)).thenReturn(e);
        return service.list(USUARIO, HOY).stream().collect(Collectors.toMap(Logro::getCodigo, Function.identity()));
    }

    private static FechaImporte fi(String fecha, String importe) {
        return new FechaImporte(LocalDate.parse(fecha), new BigDecimal(importe));
    }

    @Test
    @DisplayName("sin datos: todo el catalogo y nada conseguido")
    void usuarioNuevo() {
        Map<String, Logro> l = logros(EstadisticasLogros.builder().fechasMovimiento(List.of())
                .balancePorDia(List.of()).metas(List.of()).build());

        assertThat(l).hasSize(LogroService.CATALOGO.size());
        assertThat(l.values()).noneMatch(Logro::isConseguido);
    }

    @Test
    @DisplayName("mes en verde: solo meses terminados con balance positivo, fechados en su ultimo dia")
    void mesesEnVerde() {
        Map<String, Logro> l = logros(EstadisticasLogros.builder()
                .fechasMovimiento(List.of(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 1)))
                .balancePorDia(List.of(
                        // Julio: +100 -150, en rojo.
                        fi("2026-07-01", "100"), fi("2026-07-20", "-150"),
                        // Agosto: +500 -200, en verde.
                        fi("2026-08-01", "500"), fi("2026-08-15", "-200"),
                        // Octubre aun no ha terminado: no cuenta aunque vaya en verde.
                        fi("2026-10-01", "900")))
                .metas(List.of()).build());

        assertThat(l.get("MES_EN_VERDE").getFechaConseguido()).isEqualTo(LocalDate.of(2026, 8, 31));
        assertThat(l.get("MESES_EN_VERDE_6").getProgreso()).isEqualTo(1);
        assertThat(l.get("RACHA_MESES_3").getFechaConseguido()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(l.get("MOVIMIENTOS_100").getProgreso()).isEqualTo(3);
    }

    @Test
    @DisplayName("una meta se consigue el dia de la aportacion que llega al objetivo; presupuestos sin fecha")
    void metasYPresupuestos() {
        Map<String, Logro> l = logros(EstadisticasLogros.builder().fechasMovimiento(List.of()).balancePorDia(List.of())
                .metas(List.of(
                        new MetaLogro(new BigDecimal("300"), List.of(fi("2026-05-10", "200"), fi("2026-04-01", "100"))),
                        new MetaLogro(new BigDecimal("1000"), List.of(fi("2026-03-01", "999.99")))))
                .numeroPresupuestos(2)
                .build());

        assertThat(l.get("META_CONSEGUIDA").getFechaConseguido()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(l.get("METAS_5").getProgreso()).isEqualTo(1);
        assertThat(l.get("PRIMERA_APORTACION").getFechaConseguido()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(l.get("PRIMER_PRESUPUESTO").isConseguido()).isTrue();
        assertThat(l.get("PRIMER_PRESUPUESTO").getFechaConseguido()).isNull();
    }
}
