package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.out.EjercicioRepositoryPort;
import com.polaris.atlas.application.out.MetaEntrenoRepositoryPort;
import com.polaris.atlas.application.out.PesoCorporalPort;
import com.polaris.atlas.application.out.RecordRepositoryPort;
import com.polaris.atlas.application.out.SesionRepositoryPort;
import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.MejorPesoEjercicio;
import com.polaris.atlas.domain.model.MetaEntreno;
import com.polaris.atlas.domain.model.MetaEntrenoNotFoundException;
import com.polaris.atlas.domain.model.PesoCorporal;
import com.polaris.atlas.domain.model.Sesion;
import com.polaris.atlas.domain.model.TipoMetaEntreno;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo que importa de las metas: el punto de partida se fija al crear, el
 * progreso se calcula al leer en el sentido correcto (bajar o subir peso) y un
 * usuario nunca ve las de otro. Ver docs/decisiones/043-logros-calculados-y-metas.md.
 */
@ExtendWith(MockitoExtension.class)
class MetaEntrenoServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO = 2L;
    private static final Long PRESS = 10L;

    @Mock
    private MetaEntrenoRepositoryPort repository;
    @Mock
    private EjercicioRepositoryPort ejercicioRepository;
    @Mock
    private PesoCorporalPort pesoCorporalPort;
    @Mock
    private RecordRepositoryPort recordRepository;
    @Mock
    private SesionRepositoryPort sesionRepository;

    @InjectMocks
    private MetaEntrenoService service;

    /** Ultimo peso 80 kg, record de press de 90 kg y dos sesiones esta semana. */
    private void datos(String peso, String record, int sesiones) {
        lenient().when(pesoCorporalPort.findAll(eq(USUARIO), any())).thenReturn(peso == null ? List.of()
                : List.of(PesoCorporal.builder().fecha(LocalDate.now()).pesoKg(new BigDecimal(peso)).build()));
        lenient().when(recordRepository.findMejorPesoPorEjercicio(USUARIO)).thenReturn(List.of(
                MejorPesoEjercicio.builder().ejercicioId(PRESS).pesoKg(new BigDecimal(record)).reps(3).build()));
        lenient().when(sesionRepository.findAll(eq(USUARIO), any()))
                .thenReturn(java.util.Collections.nCopies(sesiones, Sesion.builder().build()));
        lenient().when(ejercicioRepository.findById(PRESS))
                .thenReturn(Optional.of(Ejercicio.builder().id(PRESS).nombre("Press banca").build()));
        lenient().when(repository.save(any(MetaEntreno.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static MetaEntreno meta(TipoMetaEntreno tipo, Long ejercicioId, String objetivo) {
        return MetaEntreno.builder().tipo(tipo).ejercicioId(ejercicioId).valorObjetivo(new BigDecimal(objetivo)).build();
    }

    @Test
    @DisplayName("create fija usuario, fecha de hoy y el punto de partida con el ultimo peso")
    void createFijaPuntoDePartida() {
        datos("80.00", "90", 0);

        MetaEntreno creada = service.create(USUARIO, meta(TipoMetaEntreno.PESO_CORPORAL, PRESS, "75"));

        assertThat(creada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(creada.getCreadaEn()).isEqualTo(LocalDate.now());
        assertThat(creada.getValorInicial()).isEqualByComparingTo("80");
        // El ejercicio solo cuenta en las marcas.
        assertThat(creada.getEjercicioId()).isNull();
        assertThat(creada.getProgresoPct()).isZero();
    }

    @Test
    @DisplayName("peso para bajar: de 80 a 75 llevando 77 es un 60 %, y en 75 o menos esta conseguida")
    void pesoBajando() {
        datos("77.00", "90", 0);
        MetaEntreno m = meta(TipoMetaEntreno.PESO_CORPORAL, null, "75");
        m.setId(5L);
        m.setUsuarioId(USUARIO);
        m.setValorInicial(new BigDecimal("80.00"));
        when(repository.findById(5L)).thenReturn(Optional.of(m));

        MetaEntreno leida = service.get(USUARIO, 5L);

        assertThat(leida.getValorActual()).isEqualByComparingTo("77");
        assertThat(leida.getProgresoPct()).isEqualTo(60);
        assertThat(leida.isConseguida()).isFalse();
    }

    @Test
    @DisplayName("peso para subir: pasar del objetivo cuenta como conseguida")
    void pesoSubiendo() {
        datos("71.00", "90", 0);
        MetaEntreno m = meta(TipoMetaEntreno.PESO_CORPORAL, null, "70");
        m.setId(5L);
        m.setUsuarioId(USUARIO);
        m.setValorInicial(new BigDecimal("65.00"));
        when(repository.findById(5L)).thenReturn(Optional.of(m));

        MetaEntreno leida = service.get(USUARIO, 5L);

        assertThat(leida.isConseguida()).isTrue();
        assertThat(leida.getProgresoPct()).isEqualTo(100);
    }

    @Test
    @DisplayName("marca: progreso desde el record al crear y nombre del ejercicio")
    void marca() {
        datos("80", "95", 0);
        MetaEntreno m = meta(TipoMetaEntreno.MARCA_EJERCICIO, PRESS, "100");
        m.setId(6L);
        m.setUsuarioId(USUARIO);
        m.setValorInicial(new BigDecimal("90"));
        when(repository.findById(6L)).thenReturn(Optional.of(m));

        MetaEntreno leida = service.get(USUARIO, 6L);

        assertThat(leida.getEjercicioNombre()).isEqualTo("Press banca");
        assertThat(leida.getValorActual()).isEqualByComparingTo("95");
        assertThat(leida.getProgresoPct()).isEqualTo(50);
    }

    @Test
    @DisplayName("sesiones por semana: cuenta las de esta semana y rechaza objetivos fuera de 1-7 o con decimales")
    void sesionesSemana() {
        datos("80", "90", 3);

        MetaEntreno creada = service.create(USUARIO, meta(TipoMetaEntreno.SESIONES_SEMANA, null, "4"));
        assertThat(creada.getValorActual()).isEqualByComparingTo("3");
        assertThat(creada.getProgresoPct()).isEqualTo(75);

        for (String malo : new String[] {"0.5", "8", "2.5"}) {
            assertThatThrownBy(() -> service.create(USUARIO, meta(TipoMetaEntreno.SESIONES_SEMANA, null, malo)))
                    .as(malo).isInstanceOf(ValidationException.class);
        }
    }

    @Test
    @DisplayName("una marca sin ejercicio, o con uno propio de otro usuario, es un 400 y no se guarda")
    void marcaSinEjercicioValido() {
        assertThatThrownBy(() -> service.create(USUARIO, meta(TipoMetaEntreno.MARCA_EJERCICIO, null, "100")))
                .isInstanceOf(ValidationException.class);
        when(ejercicioRepository.findById(99L))
                .thenReturn(Optional.of(Ejercicio.builder().id(99L).usuarioId(OTRO).nombre("Ajeno").build()));
        assertThatThrownBy(() -> service.create(USUARIO, meta(TipoMetaEntreno.MARCA_EJERCICIO, 99L, "100")))
                .isInstanceOf(ValidationException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("un plazo pasado es un 400")
    void plazoPasado() {
        MetaEntreno m = meta(TipoMetaEntreno.PESO_CORPORAL, null, "75");
        m.setFechaLimite(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> service.create(USUARIO, m)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("update conserva el punto de partida si mide lo mismo y lo rehace si cambia de ejercicio")
    void updatePuntoDePartida() {
        datos("80", "95", 0);
        MetaEntreno existente = meta(TipoMetaEntreno.MARCA_EJERCICIO, PRESS, "100");
        existente.setId(6L);
        existente.setUsuarioId(USUARIO);
        existente.setValorInicial(new BigDecimal("90"));
        existente.setCreadaEn(LocalDate.of(2026, 9, 1));
        when(repository.findById(6L)).thenReturn(Optional.of(existente));

        MetaEntreno igual = service.update(USUARIO, 6L, meta(TipoMetaEntreno.MARCA_EJERCICIO, PRESS, "105"));
        assertThat(igual.getValorInicial()).isEqualByComparingTo("90");
        assertThat(igual.getCreadaEn()).isEqualTo(LocalDate.of(2026, 9, 1));

        MetaEntreno otraMedida = service.update(USUARIO, 6L, meta(TipoMetaEntreno.PESO_CORPORAL, null, "75"));
        assertThat(otraMedida.getValorInicial()).isEqualByComparingTo("80");
    }

    @Test
    @DisplayName("get, update y delete de una meta de otro usuario son 404 y no tocan nada")
    void ajena() {
        MetaEntreno ajena = meta(TipoMetaEntreno.PESO_CORPORAL, null, "75");
        ajena.setId(7L);
        ajena.setUsuarioId(OTRO);
        when(repository.findById(7L)).thenReturn(Optional.of(ajena));

        assertThatThrownBy(() -> service.get(USUARIO, 7L)).isInstanceOf(MetaEntrenoNotFoundException.class);
        assertThatThrownBy(() -> service.update(USUARIO, 7L, meta(TipoMetaEntreno.PESO_CORPORAL, null, "70")))
                .isInstanceOf(MetaEntrenoNotFoundException.class);
        assertThatThrownBy(() -> service.delete(USUARIO, 7L)).isInstanceOf(MetaEntrenoNotFoundException.class);
        verify(repository, never()).save(any());
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("sin peso apuntado, una meta de peso no tiene valor actual ni progreso")
    void sinPeso() {
        datos(null, "90", 0);

        MetaEntreno creada = service.create(USUARIO, meta(TipoMetaEntreno.PESO_CORPORAL, null, "75"));

        assertThat(creada.getValorInicial()).isNull();
        assertThat(creada.getValorActual()).isNull();
        assertThat(creada.getProgresoPct()).isZero();
        assertThat(creada.isConseguida()).isFalse();
    }
}
