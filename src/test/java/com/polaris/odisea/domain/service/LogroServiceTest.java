package com.polaris.odisea.domain.service;

import com.polaris.odisea.application.out.EstadisticasLogrosPort;
import com.polaris.odisea.domain.model.EstadisticasLogros;
import com.polaris.odisea.domain.model.EstadisticasLogros.Terminado;
import com.polaris.odisea.domain.model.TipoContenido;
import com.polaris.shared.logro.Logro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Logros de Odisea. Ver docs/decisiones/044-logros-globales-con-fecha-calculada.md. */
@ExtendWith(MockitoExtension.class)
class LogroServiceTest {

    private static final Long USUARIO = 1L;

    @Mock
    private EstadisticasLogrosPort estadisticasPort;

    @InjectMocks
    private LogroService service;

    private Map<String, Logro> logros(List<Terminado> terminados, List<LocalDate> valoradas) {
        when(estadisticasPort.find(USUARIO)).thenReturn(EstadisticasLogros.builder()
                .terminados(terminados).fechasValoradas(valoradas).build());
        return service.list(USUARIO).stream().collect(Collectors.toMap(Logro::getCodigo, Function.identity()));
    }

    @Test
    @DisplayName("sin biblioteca: todo el catalogo y nada conseguido")
    void usuarioNuevo() {
        Map<String, Logro> l = logros(List.of(), List.of());

        assertThat(l).hasSize(LogroService.CATALOGO.size());
        assertThat(l.values()).noneMatch(Logro::isConseguido);
    }

    @Test
    @DisplayName("terminados por tipo y Todoterreno fechado con el primer final del ultimo tipo en llegar")
    void porTipo() {
        Map<String, Logro> l = logros(List.of(
                new Terminado(TipoContenido.PELICULA, LocalDate.of(2026, 1, 5)),
                new Terminado(TipoContenido.PELICULA, LocalDate.of(2026, 1, 1)),
                new Terminado(TipoContenido.SERIE, LocalDate.of(2026, 2, 1)),
                new Terminado(TipoContenido.LIBRO, null),
                new Terminado(TipoContenido.JUEGO, LocalDate.of(2026, 3, 1)),
                new Terminado(TipoContenido.LIBRO, LocalDate.of(2026, 4, 1))), List.of());

        assertThat(l.get("PRIMER_TERMINADO").getFechaConseguido()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(l.get("TERMINADOS_10").getProgreso()).isEqualTo(6);
        assertThat(l.get("PELICULAS_10").getProgreso()).isEqualTo(2);
        assertThat(l.get("LIBROS_10").getProgreso()).isEqualTo(2);
        assertThat(l.get("TODOTERRENO").isConseguido()).isTrue();
        assertThat(l.get("TODOTERRENO").getFechaConseguido()).isEqualTo(LocalDate.of(2026, 4, 1));
    }

    @Test
    @DisplayName("valoradas sin fecha de fin cuentan, pero el logro queda sin dia si lo alcanza una de ellas")
    void valoradasSinFecha() {
        LocalDate[] fechas = new LocalDate[10];
        Arrays.fill(fechas, LocalDate.of(2026, 5, 1));
        fechas[9] = null;
        Map<String, Logro> l = logros(List.of(), Arrays.asList(fechas));

        assertThat(l.get("VALORADAS_10").isConseguido()).isTrue();
        assertThat(l.get("VALORADAS_10").getFechaConseguido()).isNull();
    }
}
