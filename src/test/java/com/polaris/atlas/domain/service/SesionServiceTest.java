package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.out.EjercicioRepositoryPort;
import com.polaris.atlas.application.out.RutinaRepositoryPort;
import com.polaris.atlas.application.out.SesionRepositoryPort;
import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.Rutina;
import com.polaris.atlas.domain.model.SerieRegistro;
import com.polaris.atlas.domain.model.Sesion;
import com.polaris.atlas.domain.model.SesionFilter;
import com.polaris.atlas.domain.model.SesionNotFoundException;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo que mas importa: sesion y series se guardan con el usuario del JWT, cada
 * ejercicio tiene que ser visible para el usuario (catalogo o suyo, nunca propio
 * de otro), la rutina si viene tiene que ser suya, las series cumplen sus
 * rangos (reps, peso BigDecimal, RPE en pasos de 0.5) y no repiten numero por
 * ejercicio, hay de 1 a 200, la fecha no es futura y nadie ve ni toca la sesion
 * de otro. Ver docs/decisiones/025-sesion-agregado-con-series.md.
 */
@ExtendWith(MockitoExtension.class)
class SesionServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;
    private static final LocalDate HOY = LocalDate.now();

    @Mock
    private SesionRepositoryPort repository;

    @Mock
    private EjercicioRepositoryPort ejercicioRepository;

    @Mock
    private RutinaRepositoryPort rutinaRepository;

    @InjectMocks
    private SesionService service;

    private static Ejercicio ejercicio(Long id, Long usuarioId) {
        return Ejercicio.builder().id(id).usuarioId(usuarioId).nombre("Ejercicio " + id).grupoMuscular("Pecho").build();
    }

    private static SerieRegistro serie(Long id, Long usuarioId, Long ejercicioId, int numero, int reps, String peso,
                                       String rpe) {
        return SerieRegistro.builder().id(id).usuarioId(usuarioId).ejercicioId(ejercicioId).numeroSerie(numero)
                .reps(reps).pesoKg(new BigDecimal(peso)).rpe(rpe == null ? null : new BigDecimal(rpe)).build();
    }

    private static SerieRegistro serie(Long ejercicioId, int numero) {
        return serie(null, null, ejercicioId, numero, 8, "80", null);
    }

    private static Sesion sesion(Long id, Long usuarioId, Long rutinaId, LocalDate fecha, SerieRegistro... series) {
        return Sesion.builder().id(id).usuarioId(usuarioId).rutinaId(rutinaId).fecha(fecha)
                .series(new ArrayList<>(List.of(series))).build();
    }

    private static Sesion libre(SerieRegistro... series) {
        return sesion(null, null, null, HOY, series);
    }

    private static Rutina rutina(Long id, Long usuarioId) {
        return Rutina.builder().id(id).usuarioId(usuarioId).nombre("Push").activa(true).build();
    }

    private void ejercicioVisible(Long id, Long usuarioId) {
        when(ejercicioRepository.findById(id)).thenReturn(Optional.of(ejercicio(id, usuarioId)));
    }

    private void guardarDevuelveLoRecibido() {
        when(repository.save(any(Sesion.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ---- create ----

    @Test
    @DisplayName("create fija usuarioId en la sesion y en cada serie, y anula los ids que traiga")
    void createFijaUsuarioYAnulaIds() {
        ejercicioVisible(10L, null);
        ejercicioVisible(11L, USUARIO);
        guardarDevuelveLoRecibido();

        Sesion creada = service.create(USUARIO, sesion(999L, 999L, null, HOY,
                serie(777L, 999L, 10L, 1, 8, "80", null), serie(778L, 999L, 11L, 1, 8, "40", null)));

        assertThat(creada.getId()).isNull();
        assertThat(creada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(creada.getSeries()).hasSize(2).allSatisfy(s -> {
            assertThat(s.getId()).isNull();
            assertThat(s.getUsuarioId()).isEqualTo(USUARIO);
        });
    }

    @Test
    @DisplayName("create admite un entreno libre (sin rutina) y no consulta rutinas")
    void createAdmiteEntrenoLibre() {
        ejercicioVisible(10L, null);
        guardarDevuelveLoRecibido();

        Sesion creada = service.create(USUARIO, libre(serie(10L, 1)));

        assertThat(creada.getRutinaId()).isNull();
        verify(rutinaRepository, never()).findById(any());
    }

    @Test
    @DisplayName("create admite una rutina propia, activa o no")
    void createAdmiteRutinaPropia() {
        ejercicioVisible(10L, null);
        when(rutinaRepository.findById(5L)).thenReturn(Optional.of(rutina(5L, USUARIO)));
        guardarDevuelveLoRecibido();

        Sesion creada = service.create(USUARIO, sesion(null, null, 5L, HOY, serie(10L, 1)));

        assertThat(creada.getRutinaId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("create lanza ValidationException legible y no guarda si la rutina es de otro usuario")
    void createLanzaSiLaRutinaEsDeOtroUsuario() {
        ejercicioVisible(10L, null);
        when(rutinaRepository.findById(5L)).thenReturn(Optional.of(rutina(5L, OTRO_USUARIO)));

        assertThatThrownBy(() -> service.create(USUARIO, sesion(null, null, 5L, HOY, serie(10L, 1))))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("La rutina 5 no existe o no esta disponible");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create da el mismo 400 si la rutina no existe: no se confirma que el id sea de otro")
    void createLanzaSiLaRutinaNoExiste() {
        ejercicioVisible(10L, null);
        when(rutinaRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(USUARIO, sesion(null, null, 5L, HOY, serie(10L, 1))))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("La rutina 5 no existe o no esta disponible");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create acepta un ejercicio del catalogo y uno propio del usuario")
    void createAceptaCatalogoYPropio() {
        ejercicioVisible(10L, null);
        ejercicioVisible(11L, USUARIO);
        guardarDevuelveLoRecibido();

        Sesion creada = service.create(USUARIO, libre(serie(10L, 1), serie(11L, 1)));

        assertThat(creada.getSeries()).hasSize(2);
    }

    @Test
    @DisplayName("create lanza ValidationException legible y no guarda si el ejercicio es propio de otro usuario")
    void createLanzaSiElEjercicioEsDeOtroUsuario() {
        ejercicioVisible(10L, OTRO_USUARIO);

        assertThatThrownBy(() -> service.create(USUARIO, libre(serie(10L, 1))))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("El ejercicio 10 no existe o no esta disponible");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create da el mismo 400 si el ejercicio no existe")
    void createLanzaSiElEjercicioNoExiste() {
        when(ejercicioRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(USUARIO, libre(serie(10L, 1))))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("El ejercicio 10 no existe o no esta disponible");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create consulta cada ejercicio una sola vez aunque tenga muchas series")
    void createConsultaCadaEjercicioUnaVez() {
        ejercicioVisible(10L, null);
        guardarDevuelveLoRecibido();

        service.create(USUARIO, libre(serie(10L, 1), serie(10L, 2), serie(10L, 3)));

        verify(ejercicioRepository, org.mockito.Mockito.times(1)).findById(10L);
    }

    // ---- orden ----

    @Test
    @DisplayName("create agrupa las series por ejercicio en su orden de aparicion y por numeroSerie dentro de cada uno")
    void createAgrupaYOrdenaLasSeries() {
        ejercicioVisible(10L, null);
        ejercicioVisible(11L, null);
        guardarDevuelveLoRecibido();

        Sesion creada = service.create(USUARIO, libre(
                serie(11L, 2), serie(10L, 3), serie(11L, 1), serie(10L, 1), serie(10L, 2)));

        assertThat(creada.getSeries()).extracting(SerieRegistro::getEjercicioId)
                .containsExactly(11L, 11L, 10L, 10L, 10L);
        assertThat(creada.getSeries()).extracting(SerieRegistro::getNumeroSerie)
                .containsExactly(1, 2, 1, 2, 3);
    }

    @Test
    @DisplayName("create admite numeros de serie no consecutivos (1, 2, 5)")
    void createAdmiteNumerosNoConsecutivos() {
        ejercicioVisible(10L, null);
        guardarDevuelveLoRecibido();

        Sesion creada = service.create(USUARIO, libre(serie(10L, 5), serie(10L, 1), serie(10L, 2)));

        assertThat(creada.getSeries()).extracting(SerieRegistro::getNumeroSerie).containsExactly(1, 2, 5);
    }

    // ---- numero de serie ----

    @Test
    @DisplayName("create lanza ValidationException y no guarda con el mismo numeroSerie repetido en un ejercicio")
    void createLanzaConNumeroDeSerieRepetido() {
        assertThatThrownBy(() -> service.create(USUARIO, libre(serie(10L, 1), serie(10L, 1))))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("numero de serie 1")
                .hasMessageContaining("ejercicio 10");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create admite el mismo numeroSerie en ejercicios distintos")
    void createAdmiteMismoNumeroEnEjerciciosDistintos() {
        ejercicioVisible(10L, null);
        ejercicioVisible(11L, null);
        guardarDevuelveLoRecibido();

        Sesion creada = service.create(USUARIO, libre(serie(10L, 1), serie(11L, 1)));

        assertThat(creada.getSeries()).hasSize(2);
    }

    @Test
    @DisplayName("create lanza ValidationException con numeroSerie menor que 1, mayor que 999 o nulo")
    void createLanzaConNumeroDeSerieFueraDeRango() {
        for (Integer numero : new Integer[]{0, -1, 1000, null}) {
            SerieRegistro s = serie(10L, 1);
            s.setNumeroSerie(numero);
            assertThatThrownBy(() -> service.create(USUARIO, libre(s)))
                    .isInstanceOf(ValidationException.class).hasMessageContaining("numero de serie");
        }

        verify(repository, never()).save(any());
    }

    // ---- reps ----

    @Test
    @DisplayName("create lanza ValidationException con reps menores que 1, mayores que 999 o nulas")
    void createLanzaConRepsFueraDeRango() {
        for (Integer reps : new Integer[]{0, -3, 1000, null}) {
            SerieRegistro s = serie(10L, 1);
            s.setReps(reps);
            assertThatThrownBy(() -> service.create(USUARIO, libre(s)))
                    .isInstanceOf(ValidationException.class).hasMessageContaining("repeticiones");
        }

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create admite reps entre 1 y 999")
    void createAdmiteRepsLimite() {
        ejercicioVisible(10L, null);
        guardarDevuelveLoRecibido();

        assertThat(service.create(USUARIO, libre(serie(null, null, 10L, 1, 1, "80", null),
                serie(null, null, 10L, 2, 999, "80", null))).getSeries()).hasSize(2);
    }

    // ---- peso ----

    @Test
    @DisplayName("create lanza ValidationException con peso negativo, por encima de 1000 kg o nulo")
    void createLanzaConPesoFueraDeRango() {
        for (String peso : new String[]{"-0.01", "1000.01", "5000"}) {
            SerieRegistro s = serie(10L, 1);
            s.setPesoKg(new BigDecimal(peso));
            assertThatThrownBy(() -> service.create(USUARIO, libre(s)))
                    .isInstanceOf(ValidationException.class).hasMessageContaining("peso entre 0 y 1000");
        }
        SerieRegistro sinPeso = serie(10L, 1);
        sinPeso.setPesoKg(null);
        assertThatThrownBy(() -> service.create(USUARIO, libre(sinPeso)))
                .isInstanceOf(ValidationException.class).hasMessageContaining("necesita un peso");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create admite peso 0 (peso corporal) y 1000 kg, y normaliza la escala a 2 decimales")
    void createAdmitePesoLimiteYNormalizaEscala() {
        ejercicioVisible(10L, null);
        guardarDevuelveLoRecibido();

        Sesion creada = service.create(USUARIO, libre(
                serie(null, null, 10L, 1, 10, "0", null),
                serie(null, null, 10L, 2, 10, "1000", null),
                serie(null, null, 10L, 3, 10, "82.5", null),
                serie(null, null, 10L, 4, 10, "82.50", null)));

        assertThat(creada.getSeries()).extracting(s -> s.getPesoKg().toPlainString())
                .containsExactly("0.00", "1000.00", "82.50", "82.50");
    }

    @Test
    @DisplayName("create lanza ValidationException con mas de 2 decimales en el peso, pero admite ceros de relleno")
    void createLanzaConMasDeDosDecimalesEnElPeso() {
        SerieRegistro s = serie(10L, 1);
        s.setPesoKg(new BigDecimal("82.505"));

        assertThatThrownBy(() -> service.create(USUARIO, libre(s)))
                .isInstanceOf(ValidationException.class).hasMessageContaining("2 decimales");

        ejercicioVisible(10L, null);
        guardarDevuelveLoRecibido();
        SerieRegistro conCeros = serie(10L, 1);
        conCeros.setPesoKg(new BigDecimal("82.500"));
        assertThat(service.create(USUARIO, libre(conCeros)).getSeries().get(0).getPesoKg())
                .isEqualByComparingTo("82.5");
    }

    // ---- rpe ----

    @Test
    @DisplayName("create admite rpe nulo y rpe de 1 a 10 en pasos de 0.5, con escala 1")
    void createAdmiteRpeValido() {
        ejercicioVisible(10L, null);
        guardarDevuelveLoRecibido();

        Sesion creada = service.create(USUARIO, libre(
                serie(null, null, 10L, 1, 8, "80", null),
                serie(null, null, 10L, 2, 8, "80", "1"),
                serie(null, null, 10L, 3, 8, "80", "7.5"),
                serie(null, null, 10L, 4, 8, "80", "8.0"),
                serie(null, null, 10L, 5, 8, "80", "10")));

        assertThat(creada.getSeries()).extracting(s -> s.getRpe() == null ? null : s.getRpe().toPlainString())
                .containsExactly(null, "1.0", "7.5", "8.0", "10.0");
    }

    @Test
    @DisplayName("create lanza ValidationException con rpe menor que 1 o mayor que 10")
    void createLanzaConRpeFueraDeRango() {
        for (String rpe : new String[]{"0", "0.5", "10.5", "11", "-1"}) {
            SerieRegistro s = serie(10L, 1);
            s.setRpe(new BigDecimal(rpe));
            assertThatThrownBy(() -> service.create(USUARIO, libre(s)))
                    .isInstanceOf(ValidationException.class).hasMessageContaining("RPE entre 1 y 10");
        }

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create lanza ValidationException con un rpe que no va en pasos de 0.5")
    void createLanzaConRpeFueraDePaso() {
        for (String rpe : new String[]{"7.3", "8.25", "9.1", "7.75"}) {
            SerieRegistro s = serie(10L, 1);
            s.setRpe(new BigDecimal(rpe));
            assertThatThrownBy(() -> service.create(USUARIO, libre(s)))
                    .isInstanceOf(ValidationException.class).hasMessageContaining("pasos de 0.5");
        }

        verify(repository, never()).save(any());
    }

    // ---- fecha y duracion ----

    @Test
    @DisplayName("create admite la fecha de hoy y una pasada, y rechaza una futura")
    void createFecha() {
        ejercicioVisible(10L, null);
        guardarDevuelveLoRecibido();

        assertThat(service.create(USUARIO, sesion(null, null, null, HOY, serie(10L, 1))).getFecha()).isEqualTo(HOY);
        assertThat(service.create(USUARIO, sesion(null, null, null, HOY.minusYears(1), serie(10L, 1))).getFecha())
                .isEqualTo(HOY.minusYears(1));

        assertThatThrownBy(() -> service.create(USUARIO, sesion(null, null, null, HOY.plusDays(1), serie(10L, 1))))
                .isInstanceOf(ValidationException.class).hasMessageContaining("no puede ser futura");
    }

    @Test
    @DisplayName("create lanza ValidationException sin fecha")
    void createSinFecha() {
        assertThatThrownBy(() -> service.create(USUARIO, sesion(null, null, null, null, serie(10L, 1))))
                .isInstanceOf(ValidationException.class).hasMessageContaining("fecha");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create admite duracion nula o de 1 a 1440 minutos y rechaza el resto")
    void createDuracion() {
        ejercicioVisible(10L, null);
        guardarDevuelveLoRecibido();

        Sesion sinDuracion = libre(serie(10L, 1));
        Sesion limite = libre(serie(10L, 1));
        limite.setDuracionMin(1440);
        assertThat(service.create(USUARIO, sinDuracion).getDuracionMin()).isNull();
        assertThat(service.create(USUARIO, limite).getDuracionMin()).isEqualTo(1440);

        for (int duracion : new int[]{0, -5, 1441}) {
            Sesion s = libre(serie(10L, 1));
            s.setDuracionMin(duracion);
            assertThatThrownBy(() -> service.create(USUARIO, s))
                    .isInstanceOf(ValidationException.class).hasMessageContaining("duracion");
        }
    }

    // ---- numero de series ----

    @Test
    @DisplayName("create lanza ValidationException con cero series o series null")
    void createLanzaSinSeries() {
        Sesion sinSeries = libre();
        Sesion seriesNull = libre();
        seriesNull.setSeries(null);

        assertThatThrownBy(() -> service.create(USUARIO, sinSeries))
                .isInstanceOf(ValidationException.class).hasMessageContaining("al menos una serie");
        assertThatThrownBy(() -> service.create(USUARIO, seriesNull))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create admite exactamente 200 series y rechaza 201")
    void createLimiteDeSeries() {
        ejercicioVisible(10L, null);
        guardarDevuelveLoRecibido();

        assertThat(service.create(USUARIO, sesionConSeries(200)).getSeries()).hasSize(200);

        assertThatThrownBy(() -> service.create(USUARIO, sesionConSeries(201)))
                .isInstanceOf(ValidationException.class).hasMessageContaining("200");
    }

    private static Sesion sesionConSeries(int n) {
        Sesion s = libre();
        IntStream.rangeClosed(1, n).forEach(i -> s.getSeries().add(serie(10L, i)));
        return s;
    }

    // ---- get / list ----

    @Test
    @DisplayName("get devuelve la sesion propia")
    void getDevuelveSesionPropia() {
        when(repository.findById(5L)).thenReturn(Optional.of(sesion(5L, USUARIO, null, HOY, serie(1L, USUARIO, 10L, 1, 8, "80", null))));

        assertThat(service.get(USUARIO, 5L).getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("get lanza SesionNotFoundException si el id no existe")
    void getLanzaSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(USUARIO, 42L)).isInstanceOf(SesionNotFoundException.class);
    }

    @Test
    @DisplayName("get lanza SesionNotFoundException (no 403) si la sesion es de otro usuario")
    void getLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(sesion(5L, OTRO_USUARIO, null, HOY)));

        assertThatThrownBy(() -> service.get(USUARIO, 5L)).isInstanceOf(SesionNotFoundException.class);
    }

    @Test
    @DisplayName("list delega usuario y filtro tal cual en el repositorio")
    void listDelegaUsuarioYFiltro() {
        SesionFilter filtro = SesionFilter.builder().desde(HOY.minusDays(7)).hasta(HOY).rutinaId(3L).build();
        List<Sesion> esperado = List.of(sesion(1L, USUARIO, 3L, HOY));
        when(repository.findAll(USUARIO, filtro)).thenReturn(esperado);

        assertThat(service.list(USUARIO, filtro)).isEqualTo(esperado);
    }

    // ---- update ----

    @Test
    @DisplayName("update conserva id y usuario de la sesion existente y reemplaza las series")
    void updateConservaIdYUsuarioYReemplazaSeries() {
        when(repository.findById(5L)).thenReturn(Optional.of(sesion(5L, USUARIO, null, HOY, serie(1L, USUARIO, 10L, 1, 8, "80", null))));
        ejercicioVisible(11L, null);
        when(rutinaRepository.findById(3L)).thenReturn(Optional.of(rutina(3L, USUARIO)));
        guardarDevuelveLoRecibido();

        Sesion actualizada = service.update(USUARIO, 5L,
                sesion(999L, 999L, 3L, HOY.minusDays(1), serie(888L, 999L, 11L, 1, 5, "100", "8")));

        assertThat(actualizada.getId()).isEqualTo(5L);
        assertThat(actualizada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(actualizada.getRutinaId()).isEqualTo(3L);
        assertThat(actualizada.getFecha()).isEqualTo(HOY.minusDays(1));
        assertThat(actualizada.getSeries()).hasSize(1);
        assertThat(actualizada.getSeries().get(0).getId()).isNull();
        assertThat(actualizada.getSeries().get(0).getEjercicioId()).isEqualTo(11L);
        assertThat(actualizada.getSeries().get(0).getUsuarioId()).isEqualTo(USUARIO);
    }

    @Test
    @DisplayName("update permite quitar la rutina (pasa a entreno libre)")
    void updateQuitaLaRutina() {
        when(repository.findById(5L)).thenReturn(Optional.of(sesion(5L, USUARIO, 3L, HOY)));
        ejercicioVisible(10L, null);
        guardarDevuelveLoRecibido();

        Sesion actualizada = service.update(USUARIO, 5L, libre(serie(10L, 1)));

        assertThat(actualizada.getRutinaId()).isNull();
    }

    @Test
    @DisplayName("update lanza SesionNotFoundException y no guarda si la sesion es de otro usuario")
    void updateLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(sesion(5L, OTRO_USUARIO, null, HOY)));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, libre(serie(10L, 1))))
                .isInstanceOf(SesionNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza SesionNotFoundException y no guarda si no existe")
    void updateLanzaSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(USUARIO, 42L, libre(serie(10L, 1))))
                .isInstanceOf(SesionNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update aplica las mismas reglas: ejercicio ajeno, rutina ajena, cero series, numero repetido, fecha futura")
    void updateValidaElAgregado() {
        when(repository.findById(5L)).thenReturn(Optional.of(sesion(5L, USUARIO, null, HOY)));
        ejercicioVisible(11L, OTRO_USUARIO);
        ejercicioVisible(10L, null);
        when(rutinaRepository.findById(3L)).thenReturn(Optional.of(rutina(3L, OTRO_USUARIO)));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, libre(serie(11L, 1))))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.update(USUARIO, 5L, sesion(null, null, 3L, HOY, serie(10L, 1))))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.update(USUARIO, 5L, libre()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.update(USUARIO, 5L, libre(serie(10L, 2), serie(10L, 2))))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.update(USUARIO, 5L, sesion(null, null, null, HOY.plusDays(2), serie(10L, 1))))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    // ---- delete ----

    @Test
    @DisplayName("delete borra la sesion propia")
    void deleteBorraSesionPropia() {
        when(repository.findById(5L)).thenReturn(Optional.of(sesion(5L, USUARIO, null, HOY)));

        service.delete(USUARIO, 5L);

        verify(repository).deleteById(5L);
    }

    @Test
    @DisplayName("delete lanza SesionNotFoundException y no borra si la sesion es de otro usuario")
    void deleteLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(sesion(5L, OTRO_USUARIO, null, HOY)));

        assertThatThrownBy(() -> service.delete(USUARIO, 5L)).isInstanceOf(SesionNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete lanza SesionNotFoundException y no borra si no existe")
    void deleteLanzaSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(USUARIO, 42L)).isInstanceOf(SesionNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
