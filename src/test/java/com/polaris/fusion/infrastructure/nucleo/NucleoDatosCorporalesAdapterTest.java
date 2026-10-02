package com.polaris.fusion.infrastructure.nucleo;

import com.polaris.fusion.domain.model.DatosCorporales;
import com.polaris.nucleo.application.in.GetPerfilInterface;
import com.polaris.nucleo.domain.model.NivelActividad;
import com.polaris.nucleo.domain.model.Perfil;
import com.polaris.nucleo.domain.model.PerfilNotFoundException;
import com.polaris.nucleo.domain.model.Sexo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * El caso de uso de Nucleo va con mock y el mapper es el real: se prueba la
 * traduccion de Perfil a DatosCorporales, enums incluidos.
 */
@ExtendWith(MockitoExtension.class)
class NucleoDatosCorporalesAdapterTest {

    private static final Long USUARIO = 7L;

    @Mock
    private GetPerfilInterface getPerfil;

    private NucleoDatosCorporalesAdapter adapter() {
        return new NucleoDatosCorporalesAdapter(getPerfil, new DatosCorporalesNucleoMapperImpl());
    }

    @Test
    @DisplayName("traduce el perfil de Nucleo al modelo de Fusion")
    void traducePerfil() {
        when(getPerfil.get(USUARIO)).thenReturn(Perfil.builder().id(1L).usuarioId(USUARIO).alturaCm(178)
                .fechaNacimiento(LocalDate.of(1995, 3, 2)).sexo(Sexo.MUJER).nivelActividad(NivelActividad.ALTO).build());

        Optional<DatosCorporales> datos = adapter().find(USUARIO);

        assertThat(datos).hasValueSatisfying(d -> {
            assertThat(d.getAlturaCm()).isEqualTo(178);
            assertThat(d.getFechaNacimiento()).isEqualTo(LocalDate.of(1995, 3, 2));
            assertThat(d.getSexo()).isEqualTo(com.polaris.fusion.domain.model.Sexo.MUJER);
            assertThat(d.getNivelActividad()).isEqualTo(com.polaris.fusion.domain.model.NivelActividad.ALTO);
        });
    }

    @Test
    @DisplayName("sin perfil guardado devuelve vacio, no un error")
    void sinPerfil() {
        when(getPerfil.get(USUARIO)).thenThrow(new PerfilNotFoundException(USUARIO));

        assertThat(adapter().find(USUARIO)).isEmpty();
    }
}
