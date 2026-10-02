package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.out.AlimentoRepositoryPort;
import com.polaris.fusion.application.out.PlanComidaLineaRepositoryPort;
import com.polaris.fusion.application.out.RecetaRepositoryPort;
import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.AlimentoNotFoundException;
import com.polaris.fusion.domain.model.Receta;
import com.polaris.fusion.domain.model.RecetaIngrediente;
import com.polaris.fusion.domain.model.RecetaNotFoundException;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
 * Receta e ingredientes con el usuario del JWT, alimentos que existen,
 * aislamiento entre usuarios y que una receta usada en un plan no se borra.
 * Ver docs/decisiones/050-receta-agregado-con-ingredientes.md.
 */
@ExtendWith(MockitoExtension.class)
class RecetaServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;

    @Mock
    private RecetaRepositoryPort repository;

    @Mock
    private AlimentoRepositoryPort alimentoRepository;

    @Mock
    private PlanComidaLineaRepositoryPort planComidaLineaRepository;

    @InjectMocks
    private RecetaService service;

    private static Alimento alimento(Long id) {
        return Alimento.builder().id(id).nombre("Alimento " + id).kcal100g(new BigDecimal("100.00"))
                .proteinas100g(new BigDecimal("10.00")).carbohidratos100g(new BigDecimal("20.00"))
                .grasas100g(new BigDecimal("5.00")).build();
    }

    private static RecetaIngrediente ingrediente(Long id, Long usuarioId, Long alimentoId) {
        return RecetaIngrediente.builder().id(id).usuarioId(usuarioId).alimentoId(alimentoId)
                .cantidadG(new BigDecimal("200.00")).build();
    }

    private static Receta receta(Long id, Long usuarioId, RecetaIngrediente... ingredientes) {
        return Receta.builder().id(id).usuarioId(usuarioId).nombre("Lentejas").raciones(4)
                .ingredientes(new ArrayList<>(List.of(ingredientes))).build();
    }

    @Test
    @DisplayName("create fija usuarioId en la receta y en cada ingrediente, y anula los ids que traiga")
    void createFijaUsuarioYAnulaIds() {
        when(alimentoRepository.findById(10L)).thenReturn(Optional.of(alimento(10L)));
        when(repository.save(any(Receta.class))).thenAnswer(inv -> inv.getArgument(0));

        Receta creada = service.create(USUARIO, receta(999L, 999L, ingrediente(777L, 999L, 10L)));

        assertThat(creada.getId()).isNull();
        assertThat(creada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(creada.getIngredientes()).allSatisfy(i -> {
            assertThat(i.getId()).isNull();
            assertThat(i.getUsuarioId()).isEqualTo(USUARIO);
        });
    }

    @Test
    @DisplayName("create sin ingredientes o con mas de 50 da ValidationException y no guarda")
    void createValidaNumeroDeIngredientes() {
        assertThatThrownBy(() -> service.create(USUARIO, receta(null, null)))
                .isInstanceOf(ValidationException.class);

        RecetaIngrediente[] muchos = IntStream.range(0, 51)
                .mapToObj(i -> ingrediente(null, null, 10L)).toArray(RecetaIngrediente[]::new);
        assertThatThrownBy(() -> service.create(USUARIO, receta(null, null, muchos)))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create con un alimento que no existe da 404 y no guarda")
    void createConAlimentoInexistente() {
        when(alimentoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(USUARIO, receta(null, null, ingrediente(null, null, 10L))))
                .isInstanceOf(AlimentoNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("get de una receta de otro usuario da 404, no 403")
    void getAjenaDa404() {
        when(repository.findById(5L)).thenReturn(Optional.of(receta(5L, OTRO_USUARIO)));

        assertThatThrownBy(() -> service.get(USUARIO, 5L)).isInstanceOf(RecetaNotFoundException.class);
    }

    @Test
    @DisplayName("update conserva id y usuario de la existente, aunque el body diga otra cosa")
    void updateConservaIdYUsuario() {
        when(repository.findById(5L)).thenReturn(Optional.of(receta(5L, USUARIO)));
        when(alimentoRepository.findById(10L)).thenReturn(Optional.of(alimento(10L)));
        when(repository.save(any(Receta.class))).thenAnswer(inv -> inv.getArgument(0));

        Receta actualizada = service.update(USUARIO, 5L, receta(99L, OTRO_USUARIO, ingrediente(1L, OTRO_USUARIO, 10L)));

        assertThat(actualizada.getId()).isEqualTo(5L);
        assertThat(actualizada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(actualizada.getIngredientes().get(0).getUsuarioId()).isEqualTo(USUARIO);
    }

    @Test
    @DisplayName("delete de una receta que esta en algun plan da ValidationException y no borra")
    void deleteUsadaEnPlan() {
        when(repository.findById(5L)).thenReturn(Optional.of(receta(5L, USUARIO)));
        when(planComidaLineaRepository.existsByRecetaId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(USUARIO, 5L)).isInstanceOf(ValidationException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete de una receta propia sin uso la borra")
    void deleteBorra() {
        when(repository.findById(5L)).thenReturn(Optional.of(receta(5L, USUARIO)));

        service.delete(USUARIO, 5L);

        verify(repository).deleteById(5L);
    }

    @Test
    @DisplayName("los macros por racion son los totales entre las raciones")
    void macrosPorRacion() {
        RecetaIngrediente i = ingrediente(null, USUARIO, 10L);
        i.setAlimento(alimento(10L));
        Receta r = receta(1L, USUARIO, i);

        assertThat(r.getTotales().kcal()).isEqualByComparingTo("200.00");
        assertThat(r.getPorRacion().kcal()).isEqualByComparingTo("50.00");
        assertThat(r.getPorRacion().proteinas()).isEqualByComparingTo("5.00");
    }
}
