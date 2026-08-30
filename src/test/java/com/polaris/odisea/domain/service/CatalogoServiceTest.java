package com.polaris.odisea.domain.service;

import com.polaris.odisea.application.out.CatalogoExternoPort;
import com.polaris.odisea.application.out.EntradaRepositoryPort;
import com.polaris.odisea.application.out.TituloRepositoryPort;
import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.domain.model.EstadoEntrada;
import com.polaris.odisea.domain.model.FuenteExterna;
import com.polaris.odisea.domain.model.ResultadoCatalogo;
import com.polaris.odisea.domain.model.TipoContenido;
import com.polaris.odisea.domain.model.Titulo;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo que importa aqui: elegir la fuente por tipo, no duplicar fichas de
 * catalogo y no duplicar tu entrada. Ver docs/modulos/odisea.md.
 */
@ExtendWith(MockitoExtension.class)
class CatalogoServiceTest {

    private static final Long USUARIO = 1L;

    @Mock
    private CatalogoExternoPort tmdb;

    @Mock
    private TituloRepositoryPort tituloRepository;

    @Mock
    private EntradaRepositoryPort entradaRepository;

    private CatalogoService service;

    @BeforeEach
    void setUp() {
        service = new CatalogoService(List.of(tmdb), tituloRepository, entradaRepository);
    }

    @Test
    @DisplayName("buscar delega en la fuente que soporta el tipo")
    void buscarDelegaEnLaFuenteQueSoportaElTipo() {
        when(tmdb.soporta(TipoContenido.PELICULA)).thenReturn(true);
        when(tmdb.buscar("interstellar", TipoContenido.PELICULA))
                .thenReturn(List.of(resultado("157336")));

        List<ResultadoCatalogo> resultados = service.buscar("interstellar", TipoContenido.PELICULA);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.getFirst().getIdExterno()).isEqualTo("157336");
    }

    @Test
    @DisplayName("buscar marca con tituloId los resultados que ya estan importados")
    void buscarMarcaLosYaImportados() {
        when(tmdb.soporta(TipoContenido.PELICULA)).thenReturn(true);
        when(tmdb.buscar(any(), any())).thenReturn(List.of(resultado("157336")));
        when(tituloRepository.findByIdExternoAndFuenteExterna("157336", FuenteExterna.TMDB))
                .thenReturn(Optional.of(Titulo.builder().id(42L).build()));

        assertThat(service.buscar("interstellar", TipoContenido.PELICULA).getFirst().getTituloId())
                .isEqualTo(42L);
    }

    @Test
    @DisplayName("buscar deja tituloId a null si esa ficha no esta importada")
    void buscarDejaNullLosNoImportados() {
        when(tmdb.soporta(TipoContenido.PELICULA)).thenReturn(true);
        when(tmdb.buscar(any(), any())).thenReturn(List.of(resultado("157336")));
        when(tituloRepository.findByIdExternoAndFuenteExterna(any(), any())).thenReturn(Optional.empty());

        assertThat(service.buscar("interstellar", TipoContenido.PELICULA).getFirst().getTituloId())
                .isNull();
    }

    @Test
    @DisplayName("buscar lanza ValidationException si ninguna fuente soporta el tipo")
    void buscarLanzaSiNoHayFuenteParaElTipo() {
        when(tmdb.soporta(TipoContenido.JUEGO)).thenReturn(false);

        assertThatThrownBy(() -> service.buscar("zelda", TipoContenido.JUEGO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("JUEGO");
    }

    @Test
    @DisplayName("importar guarda la ficha si no estaba y crea la entrada como PENDIENTE")
    void importarGuardaLaFichaNuevaYCreaLaEntrada() {
        when(tmdb.soporta(TipoContenido.PELICULA)).thenReturn(true);
        when(tmdb.fuente()).thenReturn(FuenteExterna.TMDB);
        when(tituloRepository.findByIdExternoAndFuenteExterna("157336", FuenteExterna.TMDB))
                .thenReturn(Optional.empty());
        when(tmdb.obtener("157336", TipoContenido.PELICULA))
                .thenReturn(Titulo.builder().titulo("Interstellar").build());
        when(tituloRepository.save(any(Titulo.class))).thenReturn(Titulo.builder().id(7L).build());
        when(entradaRepository.existsByUsuarioIdAndTituloId(USUARIO, 7L)).thenReturn(false);
        when(entradaRepository.save(any(Entrada.class))).thenAnswer(inv -> inv.getArgument(0));

        Entrada entrada = service.importar(USUARIO, "157336", TipoContenido.PELICULA);

        assertThat(entrada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(entrada.getTituloId()).isEqualTo(7L);
        assertThat(entrada.getEstado()).isEqualTo(EstadoEntrada.PENDIENTE);
    }

    @Test
    @DisplayName("importar reutiliza el titulo existente y no vuelve a pedirlo a la fuente")
    void importarReutilizaElTituloYaGuardado() {
        when(tmdb.soporta(TipoContenido.PELICULA)).thenReturn(true);
        when(tmdb.fuente()).thenReturn(FuenteExterna.TMDB);
        when(tituloRepository.findByIdExternoAndFuenteExterna("157336", FuenteExterna.TMDB))
                .thenReturn(Optional.of(Titulo.builder().id(7L).build()));
        when(entradaRepository.existsByUsuarioIdAndTituloId(USUARIO, 7L)).thenReturn(false);
        when(entradaRepository.save(any(Entrada.class))).thenAnswer(inv -> inv.getArgument(0));

        service.importar(USUARIO, "157336", TipoContenido.PELICULA);

        verify(tmdb, never()).obtener(any(), any());
        verify(tituloRepository, never()).save(any());
    }

    @Test
    @DisplayName("importar lanza DuplicateResourceException si ya tienes ese titulo en tu lista")
    void importarLanzaSiYaLoTienes() {
        when(tmdb.soporta(TipoContenido.PELICULA)).thenReturn(true);
        when(tmdb.fuente()).thenReturn(FuenteExterna.TMDB);
        when(tituloRepository.findByIdExternoAndFuenteExterna(any(), any()))
                .thenReturn(Optional.of(Titulo.builder().id(7L).build()));
        when(entradaRepository.existsByUsuarioIdAndTituloId(USUARIO, 7L)).thenReturn(true);

        assertThatThrownBy(() -> service.importar(USUARIO, "157336", TipoContenido.PELICULA))
                .isInstanceOf(DuplicateResourceException.class);

        verify(entradaRepository, never()).save(any());
    }

    private ResultadoCatalogo resultado(String idExterno) {
        return ResultadoCatalogo.builder()
                .fuenteExterna(FuenteExterna.TMDB)
                .idExterno(idExterno)
                .tipo(TipoContenido.PELICULA)
                .titulo("Interstellar")
                .build();
    }
}
