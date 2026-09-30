package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.out.AlimentoRepositoryPort;
import com.polaris.fusion.application.out.ComidaRepositoryPort;
import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.AlimentoNotFoundException;
import com.polaris.fusion.domain.model.Comida;
import com.polaris.fusion.domain.model.ComidaFilter;
import com.polaris.fusion.domain.model.ComidaLinea;
import com.polaris.fusion.domain.model.ComidaNotFoundException;
import com.polaris.fusion.domain.model.MomentoComida;
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
 * Lo que mas importa: comida y lineas se guardan con el usuario del JWT, cada
 * alimento tiene que existir, y un usuario nunca ve ni toca la comida de otro.
 * Ver docs/decisiones/017-comida-agregado-con-lineas-macros-al-vuelo.md.
 */
@ExtendWith(MockitoExtension.class)
class ComidaServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;
    private static final LocalDate HOY = LocalDate.of(2026, 9, 30);

    @Mock
    private ComidaRepositoryPort repository;

    @Mock
    private AlimentoRepositoryPort alimentoRepository;

    @InjectMocks
    private ComidaService service;

    private static Alimento alimento(Long id) {
        return Alimento.builder().id(id).nombre("Alimento " + id).kcal100g(new BigDecimal("100.00"))
                .proteinas100g(new BigDecimal("10.00")).carbohidratos100g(new BigDecimal("20.00"))
                .grasas100g(new BigDecimal("5.00")).build();
    }

    private static ComidaLinea linea(Long id, Long usuarioId, Long alimentoId) {
        return ComidaLinea.builder().id(id).usuarioId(usuarioId).alimentoId(alimentoId)
                .cantidadG(new BigDecimal("150.00")).build();
    }

    private static Comida comida(Long id, Long usuarioId, ComidaLinea... lineas) {
        return Comida.builder().id(id).usuarioId(usuarioId).fecha(HOY).momento(MomentoComida.COMIDA)
                .lineas(new ArrayList<>(List.of(lineas))).build();
    }

    @Test
    @DisplayName("create fija usuarioId en la comida y en cada linea, y anula los ids que traiga")
    void createFijaUsuarioYAnulaIds() {
        when(alimentoRepository.findById(10L)).thenReturn(Optional.of(alimento(10L)));
        when(alimentoRepository.findById(11L)).thenReturn(Optional.of(alimento(11L)));
        when(repository.save(any(Comida.class))).thenAnswer(inv -> inv.getArgument(0));

        Comida creada = service.create(USUARIO,
                comida(999L, 999L, linea(777L, 999L, 10L), linea(778L, 999L, 11L)));

        assertThat(creada.getId()).isNull();
        assertThat(creada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(creada.getLineas()).hasSize(2)
                .allSatisfy(l -> {
                    assertThat(l.getId()).isNull();
                    assertThat(l.getUsuarioId()).isEqualTo(USUARIO);
                });
    }

    @Test
    @DisplayName("create lanza AlimentoNotFoundException y no guarda si un alimento no existe")
    void createLanzaSiElAlimentoNoExiste() {
        when(alimentoRepository.findById(10L)).thenReturn(Optional.of(alimento(10L)));
        when(alimentoRepository.findById(11L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(USUARIO,
                comida(null, null, linea(null, null, 10L), linea(null, null, 11L))))
                .isInstanceOf(AlimentoNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create lanza ValidationException con cero lineas")
    void createLanzaSinLineas() {
        assertThatThrownBy(() -> service.create(USUARIO, comida(null, null)))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create lanza ValidationException con lineas null")
    void createLanzaConLineasNull() {
        Comida sinLineas = comida(null, null);
        sinLineas.setLineas(null);

        assertThatThrownBy(() -> service.create(USUARIO, sinLineas))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("create admite exactamente 50 lineas")
    void createAdmiteCincuentaLineas() {
        when(alimentoRepository.findById(10L)).thenReturn(Optional.of(alimento(10L)));
        when(repository.save(any(Comida.class))).thenAnswer(inv -> inv.getArgument(0));

        Comida creada = service.create(USUARIO, comidaConLineas(50));

        assertThat(creada.getLineas()).hasSize(50);
    }

    @Test
    @DisplayName("create lanza ValidationException con 51 lineas")
    void createLanzaConMasDeCincuentaLineas() {
        assertThatThrownBy(() -> service.create(USUARIO, comidaConLineas(51)))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    private static Comida comidaConLineas(int n) {
        Comida c = comida(null, null);
        IntStream.range(0, n).forEach(i -> c.getLineas().add(linea(null, null, 10L)));
        return c;
    }

    @Test
    @DisplayName("get devuelve la comida propia")
    void getDevuelveComidaPropia() {
        when(repository.findById(5L)).thenReturn(Optional.of(comida(5L, USUARIO, linea(1L, USUARIO, 10L))));

        assertThat(service.get(USUARIO, 5L).getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("get lanza ComidaNotFoundException si el id no existe")
    void getLanzaSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(USUARIO, 42L))
                .isInstanceOf(ComidaNotFoundException.class);
    }

    @Test
    @DisplayName("get lanza ComidaNotFoundException (no 403) si la comida es de otro usuario")
    void getLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(comida(5L, OTRO_USUARIO, linea(1L, OTRO_USUARIO, 10L))));

        assertThatThrownBy(() -> service.get(USUARIO, 5L))
                .isInstanceOf(ComidaNotFoundException.class);
    }

    @Test
    @DisplayName("list delega usuario y filtro tal cual en el repositorio")
    void listDelegaUsuarioYFiltro() {
        ComidaFilter filtro = ComidaFilter.builder().desde(HOY).momento(MomentoComida.CENA).build();
        List<Comida> esperado = List.of(comida(1L, USUARIO, linea(1L, USUARIO, 10L)));
        when(repository.findAll(USUARIO, filtro)).thenReturn(esperado);

        assertThat(service.list(USUARIO, filtro)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("update conserva id y usuario de la comida existente y reemplaza las lineas")
    void updateConservaIdYUsuarioYReemplazaLineas() {
        when(repository.findById(5L)).thenReturn(Optional.of(comida(5L, USUARIO, linea(1L, USUARIO, 10L))));
        when(alimentoRepository.findById(11L)).thenReturn(Optional.of(alimento(11L)));
        when(repository.save(any(Comida.class))).thenAnswer(inv -> inv.getArgument(0));

        Comida actualizada = service.update(USUARIO, 5L,
                comida(999L, 999L, linea(888L, 999L, 11L)));

        assertThat(actualizada.getId()).isEqualTo(5L);
        assertThat(actualizada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(actualizada.getLineas()).hasSize(1);
        assertThat(actualizada.getLineas().get(0).getId()).isNull();
        assertThat(actualizada.getLineas().get(0).getAlimentoId()).isEqualTo(11L);
        assertThat(actualizada.getLineas().get(0).getUsuarioId()).isEqualTo(USUARIO);
    }

    @Test
    @DisplayName("update lanza ComidaNotFoundException y no guarda si la comida es de otro usuario")
    void updateLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(comida(5L, OTRO_USUARIO, linea(1L, OTRO_USUARIO, 10L))));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, comida(null, null, linea(null, null, 10L))))
                .isInstanceOf(ComidaNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza AlimentoNotFoundException y no guarda si un alimento no existe")
    void updateLanzaSiElAlimentoNoExiste() {
        when(repository.findById(5L)).thenReturn(Optional.of(comida(5L, USUARIO, linea(1L, USUARIO, 10L))));
        when(alimentoRepository.findById(11L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(USUARIO, 5L, comida(null, null, linea(null, null, 11L))))
                .isInstanceOf(AlimentoNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza ValidationException con cero lineas y no guarda")
    void updateLanzaSinLineas() {
        when(repository.findById(5L)).thenReturn(Optional.of(comida(5L, USUARIO, linea(1L, USUARIO, 10L))));

        assertThatThrownBy(() -> service.update(USUARIO, 5L, comida(null, null)))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("delete borra la comida propia")
    void deleteBorraComidaPropia() {
        when(repository.findById(5L)).thenReturn(Optional.of(comida(5L, USUARIO, linea(1L, USUARIO, 10L))));

        service.delete(USUARIO, 5L);

        verify(repository).deleteById(5L);
    }

    @Test
    @DisplayName("delete lanza ComidaNotFoundException y no borra si la comida es de otro usuario")
    void deleteLanzaSiEsDeOtroUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(comida(5L, OTRO_USUARIO, linea(1L, OTRO_USUARIO, 10L))));

        assertThatThrownBy(() -> service.delete(USUARIO, 5L))
                .isInstanceOf(ComidaNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete lanza ComidaNotFoundException y no borra si no existe")
    void deleteLanzaSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(USUARIO, 42L))
                .isInstanceOf(ComidaNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
