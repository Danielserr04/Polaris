package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.out.ObjetivoNutricionalRepositoryPort;
import com.polaris.fusion.domain.model.ObjetivoNutricional;
import com.polaris.fusion.domain.model.ObjetivoNutricionalNotFoundException;
import com.polaris.shared.error.DuplicateResourceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo que mas importa: el historico no se sobrescribe (siempre fila nueva),
 * una fecha no se repite y cada usuario solo ve lo suyo. Ver
 * docs/decisiones/016-objetivo-nutricional-historico-inmutable.md.
 */
@ExtendWith(MockitoExtension.class)
class ObjetivoNutricionalServiceTest {

    private static final Long USUARIO = 1L;
    private static final LocalDate HOY = LocalDate.of(2026, 9, 30);

    @Mock
    private ObjetivoNutricionalRepositoryPort repository;

    @InjectMocks
    private ObjetivoNutricionalService service;

    private static ObjetivoNutricional objetivo(Long id, Long usuarioId, LocalDate desde) {
        return ObjetivoNutricional.builder().id(id).usuarioId(usuarioId)
                .kcalDiarias(2200).proteinasObj(150).carbosObj(240).grasasObj(70)
                .vigenteDesde(desde).build();
    }

    @Test
    @DisplayName("create fija el usuarioId del JWT y fuerza id null aunque llegue uno")
    void createFijaUsuarioYSinId() {
        when(repository.existsByUsuarioIdAndVigenteDesde(USUARIO, HOY)).thenReturn(false);
        when(repository.save(any(ObjetivoNutricional.class))).thenAnswer(inv -> inv.getArgument(0));

        ObjetivoNutricional creado = service.create(USUARIO, objetivo(999L, 999L, HOY));

        assertThat(creado.getId()).isNull();
        assertThat(creado.getUsuarioId()).isEqualTo(USUARIO);
    }

    @Test
    @DisplayName("create acepta una vigente_desde futura")
    void createAceptaFechaFutura() {
        LocalDate lunes = HOY.plusDays(4);
        when(repository.existsByUsuarioIdAndVigenteDesde(USUARIO, lunes)).thenReturn(false);
        when(repository.save(any(ObjetivoNutricional.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.create(USUARIO, objetivo(null, null, lunes)).getVigenteDesde()).isEqualTo(lunes);
    }

    @Test
    @DisplayName("create lanza DuplicateResourceException y no guarda si esa fecha ya tiene objetivo")
    void createLanzaSiLaFechaEstaOcupada() {
        when(repository.existsByUsuarioIdAndVigenteDesde(USUARIO, HOY)).thenReturn(true);

        assertThatThrownBy(() -> service.create(USUARIO, objetivo(null, null, HOY)))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("getVigente devuelve el objetivo que el repositorio da para esa fecha")
    void getVigenteDevuelveElDelRepositorio() {
        when(repository.findVigente(USUARIO, HOY)).thenReturn(Optional.of(objetivo(3L, USUARIO, HOY)));

        assertThat(service.getVigente(USUARIO, HOY).getId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getVigente lanza ObjetivoNutricionalNotFoundException si no hay ninguno en esa fecha")
    void getVigenteLanzaNotFoundSiNoHay() {
        when(repository.findVigente(USUARIO, HOY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getVigente(USUARIO, HOY))
                .isInstanceOf(ObjetivoNutricionalNotFoundException.class);
    }

    @Test
    @DisplayName("list delega el usuarioId tal cual en el repositorio")
    void listDelegaUsuario() {
        List<ObjetivoNutricional> esperado = List.of(objetivo(2L, USUARIO, HOY), objetivo(1L, USUARIO, HOY.minusDays(30)));
        when(repository.findAll(USUARIO)).thenReturn(esperado);

        assertThat(service.list(USUARIO)).isEqualTo(esperado);
    }
}
