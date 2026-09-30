package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.out.EjercicioRepositoryPort;
import com.polaris.atlas.application.out.RutinaRepositoryPort;
import com.polaris.atlas.application.out.SesionRutinaRepositoryPort;
import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.Rutina;
import com.polaris.atlas.domain.model.RutinaEjercicio;
import com.polaris.atlas.domain.model.RutinaFilter;
import com.polaris.atlas.domain.model.RutinaNotFoundException;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
 * Lo que mas importa: rutina y lineas se guardan con el usuario del JWT, cada
 * ejercicio tiene que ser visible para el usuario (catalogo o suyo, nunca propio
 * de otro), el orden no se repite, hay al menos una linea, el nombre es unico
 * por usuario y nadie ve ni toca la rutina de otro. Ver
 * docs/decisiones/024-rutina-agregado-con-lineas.md.
 */
@ExtendWith(MockitoExtension.class)
class RutinaServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;

    @Mock
    private RutinaRepositoryPort repository;

    @Mock
    private EjercicioRepositoryPort ejercicioRepository;

    @Mock
    private SesionRutinaRepositoryPort sesionRepository;

    @InjectMocks
    private RutinaService service;

    private static Ejercicio ejercicio(Long id, Long usuarioId) {
        return Ejercicio.builder().id(id).usuarioId(usuarioId).nombre("Ejercicio " + id).grupoMuscular("Pecho").build();
    }

    private static RutinaEjercicio linea(Long id, Long usuarioId, Long ejercicioId, int orden) {
        return RutinaEjercicio.builder().id(id).usuarioId(usuarioId).ejercicioId(ejercicioId).orden(orden)
                .seriesObjetivo(4).repsObjetivo("8-12").build();
    }

    private static Rutina rutina(Long id, Long usuarioId, String nombre, RutinaEjercicio... lineas) {
        return Rutina.builder().id(id).usuarioId(usuarioId).nombre(nombre).activa(true)
                .lineas(new ArrayList<>(List.of(lineas))).build();
    }

    private void ejercicioVisible(Long id, Long usuarioId) {
        when(ejercicioRepository.findById(id)).thenReturn(Optional.of(ejercicio(id, usuarioId)));
    }

    // ---- create ----

    @Test
    @DisplayName("create fija usuarioId en la rutina y en cada linea, y anula los ids que traiga")
    void createFijaUsuarioYAnulaIds() {
        ejercicioVisible(10L, null);
        ejercicioVisible(11L, USUARIO);
        when(repository.findByUsuarioIdAndNombre(USUARIO, "Push")).thenReturn(Optional.empty());
        when(repository.save(any(Rutina.class))).thenAnswer(inv -> inv.getArgument(0));

        Rutina creada = service.create(USUARIO,
                rutina(999L, 999L, "Push", linea(777L, 999L, 10L, 1), linea(778L, 999L, 11L, 2)));

        assertThat(creada.getId()).isNull();
        assertThat(creada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(creada.getLineas()).hasSize(2).allSatisfy(l -> {
            assertThat(l.getId()).isNull();
            assertThat(l.getUsuarioId()).isEqualTo(USUARIO);
        });
    }

    @Test
    @DisplayName("create guarda las lineas ordenadas por orden, vengan como vengan")
    void createOrdenaLasLineas() {
        ejercicioVisible(10L, null);
        ejercicioVisible(11L, null);
        ejercicioVisible(12L, null);
        when(repository.findByUsuarioIdAndNombre(USUARIO, "Push")).thenReturn(Optional.empty());
        when(repository.save(any(Rutina.class))).thenAnswer(inv -> inv.getArgument(0));

        Rutina creada = service.create(USUARIO, rutina(null, null, "Push",
                linea(null, null, 12L, 3), linea(null, null, 10L, 1), linea(null, null, 11L, 2)));

        assertThat(creada.getLineas()).extracting(RutinaEjercicio::getOrden).containsExactly(1, 2, 3);
        assertThat(creada.getLineas()).extracting(RutinaEjercicio::getEjercicioId).containsExactly(10L, 11L, 12L);
    }

    @Test
    @DisplayName("create acepta un ejercicio del catalogo y uno propio del usuario")
    void createAceptaCatalogoYPropio() {
        ejercicioVisible(10L, null);
        ejercicioVisible(11L, USUARIO);
        when(repository.findByUsuarioIdAndNombre(USUARIO, "Push")).thenReturn(Optional.empty());
        when(repository.save(any(Rutina.class))).thenAnswer(inv -> inv.getArgument(0));

        Rutina creada = service.create(USUARIO,
                rutina(null, null, "Push", linea(null, null, 10L, 1), linea(null, null, 11L, 2)));

        assertThat(creada.getLineas()).hasSize(2);
        verify(repository).save(any(Rutina.class));
    }

    @Test
    @DisplayName("create lanza ValidationException legible y no guarda si el ejercicio es propio de otro usuario")
    void createLanzaSiElEjercicioEsDeOtroUsuario() {
        ejercicioVisible(10L, OTRO_USUARIO);

        assertThatThrownBy(() -> service.create(USUARIO, rutina(null, null, "Push", linea(null, null, 10L, 1))))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("El ejercicio 10 no existe o no esta disponible");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create da el mismo 400 si el ejercicio no existe: no se confirma que el id sea de otro")
    void createLanzaSiElEjercicioNoExiste() {
        when(ejercicioRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(USUARIO, rutina(null, null, "Push", linea(null, null, 10L, 1))))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("El ejercicio 10 no existe o no esta disponible");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create lanza ValidationException y no guarda con dos lineas con el mismo orden")
    void createLanzaConOrdenRepetido() {
        assertThatThrownBy(() -> service.create(USUARIO, rutina(null, null, "Push",
                linea(null, null, 10L, 1), linea(null, null, 11L, 1))))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("orden 1");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create admite repetir el mismo ejercicio en varias lineas con distinto orden")
    void createAdmiteEjercicioRepetido() {
        ejercicioVisible(10L, null);
        when(repository.findByUsuarioIdAndNombre(USUARIO, "Push")).thenReturn(Optional.empty());
        when(repository.save(any(Rutina.class))).thenAnswer(inv -> inv.getArgument(0));

        Rutina creada = service.create(USUARIO,
                rutina(null, null, "Push", linea(null, null, 10L, 1), linea(null, null, 10L, 2)));

        assertThat(creada.getLineas()).hasSize(2);
    }

    @Test
    @DisplayName("create lanza ValidationException con cero lineas o con lineas null")
    void createLanzaSinLineas() {
        Rutina sinLineas = rutina(null, null, "Push");
        Rutina lineasNull = rutina(null, null, "Push");
        lineasNull.setLineas(null);

        assertThatThrownBy(() -> service.create(USUARIO, sinLineas))
                .isInstanceOf(ValidationException.class).hasMessageContaining("al menos un ejercicio");
        assertThatThrownBy(() -> service.create(USUARIO, lineasNull))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create admite exactamente 50 lineas y rechaza 51")
    void createLimiteDeLineas() {
        ejercicioVisible(10L, null);
        when(repository.findByUsuarioIdAndNombre(USUARIO, "Push")).thenReturn(Optional.empty());
        when(repository.save(any(Rutina.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.create(USUARIO, rutinaConLineas(50)).getLineas()).hasSize(50);

        assertThatThrownBy(() -> service.create(USUARIO, rutinaConLineas(51)))
                .isInstanceOf(ValidationException.class);
    }

    private static Rutina rutinaConLineas(int n) {
        Rutina r = rutina(null, null, "Push");
        IntStream.rangeClosed(1, n).forEach(i -> r.getLineas().add(linea(null, null, 10L, i)));
        return r;
    }

    @Test
    @DisplayName("create lanza DuplicateResourceException (409) y no guarda si el usuario ya tiene una rutina con ese nombre")
    void createLanzaSiElNombreEstaOcupado() {
        ejercicioVisible(10L, null);
        when(repository.findByUsuarioIdAndNombre(USUARIO, "Push"))
                .thenReturn(Optional.of(rutina(3L, USUARIO, "push", linea(1L, USUARIO, 10L, 1))));

        assertThatThrownBy(() -> service.create(USUARIO, rutina(null, null, "Push", linea(null, null, 10L, 1))))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create recorta los espacios del nombre antes de comprobar y guardar")
    void createRecortaElNombre() {
        ejercicioVisible(10L, null);
        when(repository.findByUsuarioIdAndNombre(USUARIO, "Push")).thenReturn(Optional.empty());
        when(repository.save(any(Rutina.class))).thenAnswer(inv -> inv.getArgument(0));

        Rutina creada = service.create(USUARIO, rutina(null, null, "  Push ", linea(null, null, 10L, 1)));

        assertThat(creada.getNombre()).isEqualTo("Push");
    }

    // ---- get / list ----

    @Test
    @DisplayName("get devuelve la rutina propia")
    void getDevuelveRutinaPropia() {
        when(repository.findById(5L)).thenReturn(Optional.of(rutina(5L, USUARIO, "Push", linea(1L, USUARIO, 10L, 1))));

        assertThat(service.get(USUARIO, 5L).getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("get lanza RutinaNotFoundException si el id no existe")
    void getLanzaSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(USUARIO, 42L)).isInstanceOf(RutinaNotFoundException.class);
    }

    @Test
    @DisplayName("get lanza RutinaNotFoundException (no 403) si la rutina es de otro usuario")
    void getLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L))
                .thenReturn(Optional.of(rutina(5L, OTRO_USUARIO, "Push", linea(1L, OTRO_USUARIO, 10L, 1))));

        assertThatThrownBy(() -> service.get(USUARIO, 5L)).isInstanceOf(RutinaNotFoundException.class);
    }

    @Test
    @DisplayName("list delega usuario y filtro tal cual en el repositorio")
    void listDelegaUsuarioYFiltro() {
        RutinaFilter filtro = RutinaFilter.builder().activa(true).build();
        List<Rutina> esperado = List.of(rutina(1L, USUARIO, "Push", linea(1L, USUARIO, 10L, 1)));
        when(repository.findAll(USUARIO, filtro)).thenReturn(esperado);

        assertThat(service.list(USUARIO, filtro)).isEqualTo(esperado);
    }

    // ---- update ----

    @Test
    @DisplayName("update conserva id y usuario de la rutina existente y reemplaza las lineas")
    void updateConservaIdYUsuarioYReemplazaLineas() {
        when(repository.findById(5L)).thenReturn(Optional.of(rutina(5L, USUARIO, "Push", linea(1L, USUARIO, 10L, 1))));
        ejercicioVisible(11L, null);
        when(repository.findByUsuarioIdAndNombre(USUARIO, "Pull")).thenReturn(Optional.empty());
        when(repository.save(any(Rutina.class))).thenAnswer(inv -> inv.getArgument(0));

        Rutina actualizada = service.update(USUARIO, 5L, rutina(999L, 999L, "Pull", linea(888L, 999L, 11L, 1)));

        assertThat(actualizada.getId()).isEqualTo(5L);
        assertThat(actualizada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(actualizada.getNombre()).isEqualTo("Pull");
        assertThat(actualizada.getLineas()).hasSize(1);
        assertThat(actualizada.getLineas().get(0).getId()).isNull();
        assertThat(actualizada.getLineas().get(0).getEjercicioId()).isEqualTo(11L);
        assertThat(actualizada.getLineas().get(0).getUsuarioId()).isEqualTo(USUARIO);
    }

    @Test
    @DisplayName("update permite conservar el nombre de la propia rutina")
    void updateAdmiteElMismoNombre() {
        Rutina existente = rutina(5L, USUARIO, "Push", linea(1L, USUARIO, 10L, 1));
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        ejercicioVisible(10L, null);
        when(repository.findByUsuarioIdAndNombre(USUARIO, "push")).thenReturn(Optional.of(existente));
        when(repository.save(any(Rutina.class))).thenAnswer(inv -> inv.getArgument(0));

        Rutina actualizada = service.update(USUARIO, 5L, rutina(null, null, "push", linea(null, null, 10L, 1)));

        assertThat(actualizada.getNombre()).isEqualTo("push");
    }

    @Test
    @DisplayName("update lanza DuplicateResourceException si el nombre es el de otra rutina del usuario")
    void updateLanzaSiElNombreEsDeOtraRutina() {
        when(repository.findById(5L)).thenReturn(Optional.of(rutina(5L, USUARIO, "Push", linea(1L, USUARIO, 10L, 1))));
        ejercicioVisible(10L, null);
        when(repository.findByUsuarioIdAndNombre(USUARIO, "Pull"))
                .thenReturn(Optional.of(rutina(6L, USUARIO, "Pull", linea(2L, USUARIO, 10L, 1))));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, rutina(null, null, "Pull", linea(null, null, 10L, 1))))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza RutinaNotFoundException y no guarda si la rutina es de otro usuario")
    void updateLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L))
                .thenReturn(Optional.of(rutina(5L, OTRO_USUARIO, "Push", linea(1L, OTRO_USUARIO, 10L, 1))));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, rutina(null, null, "Push", linea(null, null, 10L, 1))))
                .isInstanceOf(RutinaNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza ValidationException y no guarda con un ejercicio ajeno, cero lineas u orden repetido")
    void updateValidaLasLineas() {
        when(repository.findById(5L)).thenReturn(Optional.of(rutina(5L, USUARIO, "Push", linea(1L, USUARIO, 10L, 1))));
        ejercicioVisible(11L, OTRO_USUARIO);

        assertThatThrownBy(() -> service.update(USUARIO, 5L, rutina(null, null, "Push", linea(null, null, 11L, 1))))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.update(USUARIO, 5L, rutina(null, null, "Push")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.update(USUARIO, 5L, rutina(null, null, "Push",
                linea(null, null, 10L, 2), linea(null, null, 12L, 2))))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    // ---- delete ----

    @Test
    @DisplayName("delete borra la rutina propia")
    void deleteBorraRutinaPropia() {
        when(repository.findById(5L)).thenReturn(Optional.of(rutina(5L, USUARIO, "Push", linea(1L, USUARIO, 10L, 1))));

        service.delete(USUARIO, 5L);

        verify(repository).deleteById(5L);
    }

    @Test
    @DisplayName("delete lanza ValidationException (400) y no borra una rutina que tiene sesiones")
    void deleteNoBorraSiTieneSesiones() {
        when(repository.findById(5L)).thenReturn(Optional.of(rutina(5L, USUARIO, "Push", linea(1L, USUARIO, 10L, 1))));
        when(sesionRepository.existsByRutinaId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(USUARIO, 5L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("sesiones")
                .hasMessageContaining("inactiva");

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete no consulta las sesiones si la rutina es de otro usuario o no existe")
    void deleteNoConsultaSesionesSiNoEsPropia() {
        when(repository.findById(5L))
                .thenReturn(Optional.of(rutina(5L, OTRO_USUARIO, "Push", linea(1L, OTRO_USUARIO, 10L, 1))));
        when(repository.findById(6L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(USUARIO, 5L)).isInstanceOf(RutinaNotFoundException.class);
        assertThatThrownBy(() -> service.delete(USUARIO, 6L)).isInstanceOf(RutinaNotFoundException.class);

        verify(sesionRepository, never()).existsByRutinaId(any());
    }

    @Test
    @DisplayName("delete lanza RutinaNotFoundException y no borra si la rutina es de otro usuario")
    void deleteLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L))
                .thenReturn(Optional.of(rutina(5L, OTRO_USUARIO, "Push", linea(1L, OTRO_USUARIO, 10L, 1))));

        assertThatThrownBy(() -> service.delete(USUARIO, 5L)).isInstanceOf(RutinaNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete lanza RutinaNotFoundException y no borra si no existe")
    void deleteLanzaSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(USUARIO, 42L)).isInstanceOf(RutinaNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
