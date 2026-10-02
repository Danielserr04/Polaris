package com.polaris.atlas.infrastructure.persistence;

import com.polaris.atlas.domain.model.MejorPesoEjercicio;
import com.polaris.atlas.domain.model.ProgresionFilter;
import com.polaris.atlas.domain.model.ProgresionSesion;
import com.polaris.atlas.domain.model.VolumenSesionEjercicio;
import com.polaris.atlas.infrastructure.persistence.SerieRegistroRepository.MejorPesoFila;
import com.polaris.atlas.infrastructure.persistence.SerieRegistroRepository.ProgresionFila;
import com.polaris.atlas.infrastructure.persistence.SerieRegistroRepository.VolumenSesionFila;
import com.polaris.atlas.infrastructure.persistence.mapper.ProgresionProyeccionMapperImpl;
import com.polaris.atlas.infrastructure.persistence.mapper.RecordProyeccionMapperImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Los adaptadores pasan el usuario y el filtro a la consulta agregada y
 * convierten sus filas a dominio. Las consultas en si (JPQL con SUM, MAX y
 * GROUP BY) se ejecutan en MySQL; aqui se comprueba que todas filtran por
 * usuario_id, que es el aislamiento entre usuarios.
 */
class ProgresionRecordsAdaptersTest {

    private final SerieRegistroRepository repository = mock(SerieRegistroRepository.class);

    private static ProgresionFila progresionFila(Long sesionId, LocalDate fecha, String volumen, long series,
                                                 String peso, long reps) {
        ProgresionFila fila = mock(ProgresionFila.class);
        when(fila.getSesionId()).thenReturn(sesionId);
        when(fila.getFecha()).thenReturn(fecha);
        when(fila.getVolumen()).thenReturn(new BigDecimal(volumen));
        when(fila.getNumeroSeries()).thenReturn(series);
        when(fila.getPesoMaximo()).thenReturn(new BigDecimal(peso));
        when(fila.getRepsTotales()).thenReturn(reps);
        return fila;
    }

    @Test
    @DisplayName("progresion: pasa usuario, ejercicio y rango a la consulta y convierte cada fila a dominio")
    void progresion() {
        ProgresionFilter filtro = ProgresionFilter.builder().ejercicioId(5L)
                .desde(LocalDate.of(2026, 9, 1)).hasta(LocalDate.of(2026, 9, 30)).build();
        ProgresionFila fila = progresionFila(10L, LocalDate.of(2026, 9, 2), "1775.00", 3, "82.50", 22);
        when(repository.findProgresion(7L, 5L, filtro.getDesde(), filtro.getHasta())).thenReturn(List.of(fila));
        ProgresionJpaAdapter adapter = new ProgresionJpaAdapter(repository, new ProgresionProyeccionMapperImpl());

        List<ProgresionSesion> resultado = adapter.findProgresion(7L, filtro);

        assertThat(resultado).hasSize(1);
        ProgresionSesion sesion = resultado.get(0);
        assertThat(sesion.getSesionId()).isEqualTo(10L);
        assertThat(sesion.getFecha()).isEqualTo(LocalDate.of(2026, 9, 2));
        assertThat(sesion.getVolumen()).isEqualByComparingTo("1775.00");
        assertThat(sesion.getNumeroSeries()).isEqualTo(3);
        assertThat(sesion.getPesoMaximo()).isEqualByComparingTo("82.50");
        assertThat(sesion.getRepsTotales()).isEqualTo(22L);
    }

    @Test
    @DisplayName("progresion: el rango opcional llega como nulos")
    void progresionSinRango() {
        ProgresionFilter filtro = ProgresionFilter.builder().ejercicioId(5L).build();
        when(repository.findProgresion(7L, 5L, null, null)).thenReturn(List.of());
        ProgresionJpaAdapter adapter = new ProgresionJpaAdapter(repository, new ProgresionProyeccionMapperImpl());

        assertThat(adapter.findProgresion(7L, filtro)).isEmpty();
        verify(repository).findProgresion(7L, 5L, null, null);
    }

    @Test
    @DisplayName("records: convierte las filas de peso maximo y de volumen por sesion a dominio, para el usuario dado")
    void records() {
        MejorPesoFila peso = mock(MejorPesoFila.class);
        when(peso.getEjercicioId()).thenReturn(1L);
        when(peso.getEjercicioNombre()).thenReturn("Press banca");
        when(peso.getEjercicioGrupoMuscular()).thenReturn("Pecho");
        when(peso.getPesoKg()).thenReturn(new BigDecimal("100.00"));
        when(peso.getReps()).thenReturn(5);
        when(peso.getFecha()).thenReturn(LocalDate.of(2026, 9, 8));
        VolumenSesionFila volumen = mock(VolumenSesionFila.class);
        when(volumen.getEjercicioId()).thenReturn(1L);
        when(volumen.getSesionId()).thenReturn(10L);
        when(volumen.getFecha()).thenReturn(LocalDate.of(2026, 9, 1));
        when(volumen.getVolumen()).thenReturn(new BigDecimal("1775.00"));
        when(repository.findMejorPesoPorEjercicio(7L)).thenReturn(List.of(peso));
        when(repository.findVolumenPorEjercicioYSesion(7L)).thenReturn(List.of(volumen));
        RecordJpaAdapter adapter = new RecordJpaAdapter(repository, new RecordProyeccionMapperImpl());

        List<MejorPesoEjercicio> pesos = adapter.findMejorPesoPorEjercicio(7L);
        List<VolumenSesionEjercicio> volumenes = adapter.findVolumenPorEjercicioYSesion(7L);

        assertThat(pesos).hasSize(1);
        assertThat(pesos.get(0).getEjercicioNombre()).isEqualTo("Press banca");
        assertThat(pesos.get(0).getEjercicioGrupoMuscular()).isEqualTo("Pecho");
        assertThat(pesos.get(0).getPesoKg()).isEqualByComparingTo("100");
        assertThat(pesos.get(0).getReps()).isEqualTo(5);
        assertThat(pesos.get(0).getFecha()).isEqualTo(LocalDate.of(2026, 9, 8));
        assertThat(volumenes).hasSize(1);
        assertThat(volumenes.get(0).getEjercicioId()).isEqualTo(1L);
        assertThat(volumenes.get(0).getSesionId()).isEqualTo(10L);
        assertThat(volumenes.get(0).getVolumen()).isEqualByComparingTo("1775");
    }

    @Test
    @DisplayName("todas las agregaciones filtran por usuario_id, agregan en la base (group by) y no piden series enteras")
    void lasConsultasFiltranPorUsuario() {
        List<Method> consultas = Arrays.stream(SerieRegistroRepository.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Query.class))
                .toList();

        assertThat(consultas).extracting(Method::getName)
                .containsExactlyInAnyOrder("findProgresion", "findMejorPesoPorEjercicio",
                        "findVolumenPorEjercicioYSesion", "findTrabajoMuscular", "findPrimerUsoPorEjercicio",
                        "findVolumenPorSesion");
        for (Method consulta : consultas) {
            String jpql = consulta.getAnnotation(Query.class).value();
            assertThat(jpql).as(consulta.getName()).contains("s.usuarioId = :usuarioId");
            // Una proyeccion, nunca la entidad: no se cargan series en memoria.
            assertThat(jpql).as(consulta.getName()).doesNotStartWith("select s from");
            assertThat(jpql).as(consulta.getName()).contains("group by");
            assertThat(consulta.getReturnType()).isEqualTo(List.class);
        }
    }
}
