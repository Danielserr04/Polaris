package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.out.AlimentoRepositoryPort;
import com.polaris.fusion.application.out.ComidaLineaRepositoryPort;
import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.AlimentoFilter;
import com.polaris.fusion.domain.model.AlimentoNotFoundException;
import com.polaris.fusion.domain.model.FuenteAlimento;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Catalogo compartido: aqui no hay aislamiento por usuario que probar. Lo que
 * importa es que un alimento hecho a mano no pueda hacerse pasar por externo y
 * que editar no cambie su origen. Ver
 * docs/decisiones/015-alimento-catalogo-compartido-macros-por-100g.md.
 */
@ExtendWith(MockitoExtension.class)
class AlimentoServiceTest {

    @Mock
    private AlimentoRepositoryPort repository;

    @Mock
    private ComidaLineaRepositoryPort comidaLineaRepository;

    @InjectMocks
    private AlimentoService service;

    private static Alimento alimento(Long id, String nombre) {
        return Alimento.builder().id(id).nombre(nombre).kcal100g(new BigDecimal("130.00"))
                .proteinas100g(new BigDecimal("2.70")).carbohidratos100g(new BigDecimal("28.00"))
                .grasas100g(new BigDecimal("0.30")).build();
    }

    @Test
    @DisplayName("create fuerza fuente MANUAL, sin idExterno ni id, aunque el cliente los mande")
    void createFuerzaManual() {
        Alimento entrante = alimento(999L, "Arroz");
        entrante.setIdExterno("falso-123");
        when(repository.save(any(Alimento.class))).thenAnswer(inv -> inv.getArgument(0));

        Alimento creado = service.create(entrante);

        assertThat(creado.getId()).isNull();
        assertThat(creado.getFuenteExterna()).isEqualTo(FuenteAlimento.MANUAL);
        assertThat(creado.getIdExterno()).isNull();
    }

    @Test
    @DisplayName("get devuelve el alimento cuando existe")
    void getDevuelveAlimento() {
        when(repository.findById(5L)).thenReturn(Optional.of(alimento(5L, "Arroz")));

        assertThat(service.get(5L).getNombre()).isEqualTo("Arroz");
    }

    @Test
    @DisplayName("get lanza AlimentoNotFoundException si el id no existe")
    void getLanzaNotFoundSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L))
                .isInstanceOf(AlimentoNotFoundException.class);
    }

    @Test
    @DisplayName("list delega el filtro tal cual en el repositorio")
    void listDelegaFiltro() {
        AlimentoFilter filtro = AlimentoFilter.builder().texto("arroz").build();
        List<Alimento> esperado = List.of(alimento(1L, "Arroz"));
        when(repository.findAll(filtro)).thenReturn(esperado);

        assertThat(service.list(filtro)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("update conserva id, fuente e idExterno del alimento existente")
    void updateConservaIdYOrigen() {
        Alimento existente = alimento(5L, "Arroz");
        existente.setFuenteExterna(FuenteAlimento.MANUAL);
        existente.setIdExterno("abc");
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Alimento.class))).thenAnswer(inv -> inv.getArgument(0));

        Alimento cambios = alimento(999L, "Arroz integral");
        cambios.setIdExterno("otro");
        Alimento actualizado = service.update(5L, cambios);

        assertThat(actualizado.getId()).isEqualTo(5L);
        assertThat(actualizado.getFuenteExterna()).isEqualTo(FuenteAlimento.MANUAL);
        assertThat(actualizado.getIdExterno()).isEqualTo("abc");
        assertThat(actualizado.getNombre()).isEqualTo("Arroz integral");
    }

    @Test
    @DisplayName("update lanza AlimentoNotFoundException y no guarda si no existe")
    void updateLanzaSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(42L, alimento(null, "Arroz")))
                .isInstanceOf(AlimentoNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("delete borra el alimento si existe")
    void deleteBorraSiExiste() {
        when(repository.findById(5L)).thenReturn(Optional.of(alimento(5L, "Arroz")));

        service.delete(5L);

        verify(repository).deleteById(5L);
    }

    @Test
    @DisplayName("delete lanza ValidationException y no borra si el alimento esta en alguna comida")
    void deleteLanzaSiEstaEnUnaComida() {
        when(repository.findById(5L)).thenReturn(Optional.of(alimento(5L, "Arroz")));
        when(comidaLineaRepository.existsByAlimentoId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(5L))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete lanza AlimentoNotFoundException y no borra si no existe")
    void deleteLanzaSiNoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(42L))
                .isInstanceOf(AlimentoNotFoundException.class);

        verify(repository, never()).deleteById(any());
        verify(comidaLineaRepository, never()).existsByAlimentoId(any());
    }
}
