package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.out.AlimentoRepositoryPort;
import com.polaris.fusion.application.out.CatalogoAlimentoExternoPort;
import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.FuenteAlimento;
import com.polaris.fusion.domain.model.ImportacionAlimento;
import com.polaris.fusion.domain.model.ResultadoCatalogoAlimento;
import com.polaris.shared.error.ExternalServiceException;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * Lo que importa: no duplicar fichas al importar (ni llamar a la API si ya
 * existe) y marcar en la busqueda lo ya importado.
 */
@ExtendWith(MockitoExtension.class)
class CatalogoAlimentoServiceTest {

    private static final String CODIGO = "3017620422003";

    @Mock
    private CatalogoAlimentoExternoPort fuente;

    @Mock
    private AlimentoRepositoryPort repository;

    private CatalogoAlimentoService service;

    @BeforeEach
    void setUp() {
        service = new CatalogoAlimentoService(fuente, repository);
    }

    private Alimento ficha() {
        return Alimento.builder()
                .nombre("Nutella")
                .kcal100g(new BigDecimal("539.00"))
                .proteinas100g(new BigDecimal("6.30"))
                .carbohidratos100g(new BigDecimal("57.50"))
                .grasas100g(new BigDecimal("30.90"))
                .fuenteExterna(FuenteAlimento.OPEN_FOOD_FACTS)
                .idExterno(CODIGO)
                .build();
    }

    @Test
    @DisplayName("buscar marca con alimentoId los resultados ya importados")
    void buscarMarcaLosYaImportados() {
        ResultadoCatalogoAlimento nuevo = ResultadoCatalogoAlimento.builder()
                .fuenteExterna(FuenteAlimento.OPEN_FOOD_FACTS).idExterno("1").nombre("A").build();
        ResultadoCatalogoAlimento importado = ResultadoCatalogoAlimento.builder()
                .fuenteExterna(FuenteAlimento.OPEN_FOOD_FACTS).idExterno("2").nombre("B").build();
        when(fuente.buscar("pan")).thenReturn(List.of(nuevo, importado));
        when(repository.findByFuenteExternaAndIdExterno(FuenteAlimento.OPEN_FOOD_FACTS, "1"))
                .thenReturn(Optional.empty());
        when(repository.findByFuenteExternaAndIdExterno(FuenteAlimento.OPEN_FOOD_FACTS, "2"))
                .thenReturn(Optional.of(Alimento.builder().id(7L).build()));

        List<ResultadoCatalogoAlimento> resultados = service.buscar("pan");

        assertThat(resultados).hasSize(2);
        assertThat(nuevo.getAlimentoId()).isNull();
        assertThat(importado.getAlimentoId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("importar crea el alimento con fuente OPEN_FOOD_FACTS y devuelve creado=true")
    void importarCreaElAlimento() {
        when(fuente.fuente()).thenReturn(FuenteAlimento.OPEN_FOOD_FACTS);
        when(repository.findByFuenteExternaAndIdExterno(FuenteAlimento.OPEN_FOOD_FACTS, CODIGO))
                .thenReturn(Optional.empty());
        when(fuente.obtener(CODIGO)).thenReturn(ficha());
        when(repository.save(any(Alimento.class))).thenAnswer(inv -> {
            Alimento guardado = inv.getArgument(0);
            guardado.setId(5L);
            return guardado;
        });

        ImportacionAlimento importacion = service.importar(CODIGO);

        assertThat(importacion.creado()).isTrue();
        assertThat(importacion.alimento().getId()).isEqualTo(5L);
        assertThat(importacion.alimento().getFuenteExterna()).isEqualTo(FuenteAlimento.OPEN_FOOD_FACTS);
        assertThat(importacion.alimento().getIdExterno()).isEqualTo(CODIGO);
    }

    @Test
    @DisplayName("importar reutiliza el alimento existente: no llama a la API ni guarda")
    void importarReutilizaElExistente() {
        Alimento existente = ficha();
        existente.setId(9L);
        when(fuente.fuente()).thenReturn(FuenteAlimento.OPEN_FOOD_FACTS);
        when(repository.findByFuenteExternaAndIdExterno(FuenteAlimento.OPEN_FOOD_FACTS, CODIGO))
                .thenReturn(Optional.of(existente));

        ImportacionAlimento importacion = service.importar(CODIGO);

        assertThat(importacion.creado()).isFalse();
        assertThat(importacion.alimento().getId()).isEqualTo(9L);
        verify(fuente, never()).obtener(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("importar propaga el 400 si la ficha externa no es valida y no guarda nada")
    void importarPropagaValidacion() {
        when(fuente.fuente()).thenReturn(FuenteAlimento.OPEN_FOOD_FACTS);
        when(repository.findByFuenteExternaAndIdExterno(FuenteAlimento.OPEN_FOOD_FACTS, CODIGO))
                .thenReturn(Optional.empty());
        when(fuente.obtener(CODIGO)).thenThrow(new ValidationException("sin macros"));

        assertThatThrownBy(() -> service.importar(CODIGO)).isInstanceOf(ValidationException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("importar propaga el 502 si la API externa falla")
    void importarPropagaFalloExterno() {
        when(fuente.fuente()).thenReturn(FuenteAlimento.OPEN_FOOD_FACTS);
        when(repository.findByFuenteExternaAndIdExterno(FuenteAlimento.OPEN_FOOD_FACTS, CODIGO))
                .thenReturn(Optional.empty());
        when(fuente.obtener(CODIGO)).thenThrow(new ExternalServiceException("caida"));

        assertThatThrownBy(() -> service.importar(CODIGO)).isInstanceOf(ExternalServiceException.class);
        verify(repository, never()).save(any());
    }
}
