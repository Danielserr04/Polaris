package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.out.AlimentoRepositoryPort;
import com.polaris.fusion.application.out.PlanComidaRepositoryPort;
import com.polaris.fusion.application.out.RecetaRepositoryPort;
import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.ArticuloCompra;
import com.polaris.fusion.domain.model.DiaSemana;
import com.polaris.fusion.domain.model.MomentoComida;
import com.polaris.fusion.domain.model.PlanComida;
import com.polaris.fusion.domain.model.PlanComidaLinea;
import com.polaris.fusion.domain.model.PlanComidaNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lineas de alimento o de receta (nunca las dos), recetas solo propias, un
 * unico plan activo y la lista de la compra con las recetas desplegadas.
 * Ver docs/decisiones/051-plan-de-comidas-y-lista-de-la-compra.md.
 */
@ExtendWith(MockitoExtension.class)
class PlanComidaServiceTest {

    private static final Long USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;

    @Mock
    private PlanComidaRepositoryPort repository;

    @Mock
    private AlimentoRepositoryPort alimentoRepository;

    @Mock
    private RecetaRepositoryPort recetaRepository;

    @InjectMocks
    private PlanComidaService service;

    private static Alimento alimento(Long id, String nombre) {
        return Alimento.builder().id(id).nombre(nombre).kcal100g(new BigDecimal("100.00"))
                .proteinas100g(new BigDecimal("10.00")).carbohidratos100g(new BigDecimal("20.00"))
                .grasas100g(new BigDecimal("5.00")).build();
    }

    private static PlanComidaLinea conAlimento(Long alimentoId, String gramos) {
        return PlanComidaLinea.builder().diaSemana(DiaSemana.LUNES).momento(MomentoComida.COMIDA)
                .alimentoId(alimentoId).cantidadG(new BigDecimal(gramos)).build();
    }

    private static PlanComidaLinea conReceta(Long recetaId, String raciones) {
        return PlanComidaLinea.builder().diaSemana(DiaSemana.MARTES).momento(MomentoComida.CENA)
                .recetaId(recetaId).raciones(new BigDecimal(raciones)).build();
    }

    private static PlanComida plan(Long id, Long usuarioId, PlanComidaLinea... lineas) {
        return PlanComida.builder().id(id).usuarioId(usuarioId).nombre("Semana")
                .lineas(new ArrayList<>(List.of(lineas))).build();
    }

    /** Receta de 4 raciones: 400 g de arroz (id 10) y 200 g de pollo (id 11). */
    private static Receta receta(Long id, Long usuarioId) {
        RecetaIngrediente arroz = RecetaIngrediente.builder().alimentoId(10L).alimento(alimento(10L, "Arroz"))
                .cantidadG(new BigDecimal("400.00")).build();
        RecetaIngrediente pollo = RecetaIngrediente.builder().alimentoId(11L).alimento(alimento(11L, "Pollo"))
                .cantidadG(new BigDecimal("200.00")).build();
        return Receta.builder().id(id).usuarioId(usuarioId).nombre("Arroz con pollo").raciones(4)
                .ingredientes(new ArrayList<>(List.of(arroz, pollo))).build();
    }

    @Test
    @DisplayName("create fija el usuario, nace sin activar y anula ids")
    void createFijaUsuarioYNoActiva() {
        when(alimentoRepository.findById(10L)).thenReturn(Optional.of(alimento(10L, "Arroz")));
        when(repository.save(any(PlanComida.class))).thenAnswer(inv -> inv.getArgument(0));
        PlanComida entrada = plan(9L, 9L, conAlimento(10L, "150"));
        entrada.setActivo(true);

        PlanComida creado = service.create(USUARIO, entrada);

        assertThat(creado.getId()).isNull();
        assertThat(creado.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(creado.isActivo()).isFalse();
        assertThat(creado.getLineas().get(0).getUsuarioId()).isEqualTo(USUARIO);
    }

    @Test
    @DisplayName("una linea con alimento y receta a la vez, o sin ninguno, da ValidationException")
    void lineaConLasDosCosasOConNinguna() {
        PlanComidaLinea ambas = conAlimento(10L, "100");
        ambas.setRecetaId(3L);
        ambas.setRaciones(BigDecimal.ONE);
        PlanComidaLinea ninguna = PlanComidaLinea.builder().diaSemana(DiaSemana.LUNES).momento(MomentoComida.CENA).build();

        assertThatThrownBy(() -> service.create(USUARIO, plan(null, null, ambas))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.create(USUARIO, plan(null, null, ninguna))).isInstanceOf(ValidationException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("una receta de otro usuario en una linea da 404 y no guarda")
    void recetaAjenaDa404() {
        when(recetaRepository.findById(3L)).thenReturn(Optional.of(receta(3L, OTRO_USUARIO)));

        assertThatThrownBy(() -> service.create(USUARIO, plan(null, null, conReceta(3L, "1"))))
                .isInstanceOf(RecetaNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update conserva si estaba activo")
    void updateConservaActivo() {
        PlanComida existente = plan(5L, USUARIO);
        existente.setActivo(true);
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(repository.save(any(PlanComida.class))).thenAnswer(inv -> inv.getArgument(0));

        PlanComida actualizado = service.update(USUARIO, 5L, plan(null, OTRO_USUARIO));

        assertThat(actualizado.isActivo()).isTrue();
        assertThat(actualizado.getUsuarioId()).isEqualTo(USUARIO);
    }

    @Test
    @DisplayName("activar deja ese plan como el unico activo del usuario")
    void activar() {
        when(repository.findById(5L)).thenReturn(Optional.of(plan(5L, USUARIO)));

        service.activar(USUARIO, 5L);

        verify(repository).activarUnico(USUARIO, 5L);
    }

    @Test
    @DisplayName("activar un plan ajeno da 404 y no toca nada")
    void activarAjeno() {
        when(repository.findById(5L)).thenReturn(Optional.of(plan(5L, OTRO_USUARIO)));

        assertThatThrownBy(() -> service.activar(USUARIO, 5L)).isInstanceOf(PlanComidaNotFoundException.class);
        verify(repository, never()).activarUnico(any(), any());
    }

    @Test
    @DisplayName("la lista de la compra suma por alimento y despliega las recetas segun las raciones")
    void listaCompra() {
        PlanComidaLinea arrozSuelto = conAlimento(10L, "150");
        arrozSuelto.setAlimento(alimento(10L, "Arroz"));
        PlanComidaLinea mediaReceta = conReceta(3L, "2");
        mediaReceta.setReceta(receta(3L, USUARIO));
        when(repository.findById(5L)).thenReturn(Optional.of(plan(5L, USUARIO, arrozSuelto, mediaReceta)));

        List<ArticuloCompra> lista = service.getListaCompra(USUARIO, 5L);

        // 2 de 4 raciones: la mitad de cada ingrediente. Arroz 150 + 200, pollo 100.
        assertThat(lista).extracting(ArticuloCompra::getNombre).containsExactly("Arroz", "Pollo");
        assertThat(lista.get(0).getCantidadG()).isEqualByComparingTo("350.00");
        assertThat(lista.get(1).getCantidadG()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("los macros de una linea de receta son los totales escalados a sus raciones; la media es por dia con lineas")
    void macrosYMedia() {
        PlanComidaLinea arroz = conAlimento(10L, "100");
        arroz.setAlimento(alimento(10L, "Arroz"));
        PlanComidaLinea receta = conReceta(3L, "1");
        receta.setReceta(receta(3L, USUARIO));
        PlanComida p = plan(5L, USUARIO, arroz, receta);

        // Receta: 600 g a 100 kcal/100 g = 600 kcal en 4 raciones; 1 racion = 150.
        assertThat(receta.getMacros().kcal()).isEqualByComparingTo("150.00");
        assertThat(p.getDiasConLineas()).isEqualTo(2);
        assertThat(p.getMediaDiaria().kcal()).isEqualByComparingTo("125.00");
    }
}
