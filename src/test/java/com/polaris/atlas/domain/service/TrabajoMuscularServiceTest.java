package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.out.TrabajoMuscularRepositoryPort;
import com.polaris.atlas.domain.model.TrabajoMuscular;
import com.polaris.atlas.domain.model.TrabajoMuscularFilter;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * El trabajo muscular se pide para el usuario del JWT, con un rango coherente,
 * ordenado por series y con el volumen a escala 2.
 */
@ExtendWith(MockitoExtension.class)
class TrabajoMuscularServiceTest {

    private static final Long USUARIO = 1L;

    @Mock
    private TrabajoMuscularRepositoryPort repository;

    @InjectMocks
    private TrabajoMuscularService service;

    private static TrabajoMuscular grupo(String nombre, int series, String volumen) {
        return TrabajoMuscular.builder().grupoMuscular(nombre).numeroSeries(series).numeroSesiones(1)
                .volumen(new BigDecimal(volumen)).build();
    }

    @Test
    @DisplayName("ordena del grupo con mas series al que menos, desempata por nombre y deja el volumen con 2 decimales")
    void ordenaYEscala() {
        when(repository.findTrabajo(eq(USUARIO), any())).thenReturn(List.of(
                grupo("Pierna", 4, "1000"), grupo("Pecho", 9, "2400.5"), grupo("Espalda", 4, "0")));

        List<TrabajoMuscular> grupos = service.get(USUARIO, null);

        assertThat(grupos).extracting(TrabajoMuscular::getGrupoMuscular).containsExactly("Pecho", "Espalda", "Pierna");
        assertThat(grupos.get(0).getVolumen()).isEqualTo(new BigDecimal("2400.50"));
        assertThat(grupos.get(1).getVolumen()).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("desde posterior a hasta es un ValidationException y no consulta")
    void rangoAlReves() {
        TrabajoMuscularFilter filtro = TrabajoMuscularFilter.builder()
                .desde(LocalDate.of(2026, 9, 30)).hasta(LocalDate.of(2026, 9, 1)).build();

        assertThatThrownBy(() -> service.get(USUARIO, filtro)).isInstanceOf(ValidationException.class);
        verify(repository, never()).findTrabajo(any(), any());
    }
}
