package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.out.DatosCorporalesPort;
import com.polaris.fusion.application.out.PesoCorporalPort;
import com.polaris.fusion.domain.model.CalculoObjetivo;
import com.polaris.fusion.domain.model.DatosCorporales;
import com.polaris.fusion.domain.model.NivelActividad;
import com.polaris.fusion.domain.model.PesoCorporal;
import com.polaris.fusion.domain.model.Sexo;
import com.polaris.fusion.domain.model.TipoObjetivo;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cuentas de Mifflin-St Jeor con numeros comprobados a mano, y que sin datos
 * del perfil el error dice que falta. Ver
 * docs/decisiones/046-calculo-del-objetivo-desde-el-perfil.md.
 */
@ExtendWith(MockitoExtension.class)
class CalculoObjetivoServiceTest {

    private static final Long USUARIO = 1L;
    private static final LocalDate HOY = LocalDate.of(2026, 10, 2);

    @Mock
    private DatosCorporalesPort datosCorporalesPort;

    @Mock
    private PesoCorporalPort pesoCorporalPort;

    @InjectMocks
    private CalculoObjetivoService service;

    /** Hombre de 30 anos, 180 cm, actividad moderada. */
    private static DatosCorporales hombre() {
        return DatosCorporales.builder().alturaCm(180).fechaNacimiento(LocalDate.of(1996, 1, 15))
                .sexo(Sexo.HOMBRE).nivelActividad(NivelActividad.MODERADO).build();
    }

    private static PesoCorporal peso(String kg) {
        return PesoCorporal.builder().fecha(HOY.minusDays(1)).pesoKg(new BigDecimal(kg)).build();
    }

    @Test
    @DisplayName("mantenimiento: basal 1780, gasto 2759, macros 160/315/77")
    void mantenimiento() {
        when(datosCorporalesPort.find(USUARIO)).thenReturn(Optional.of(hombre()));
        when(pesoCorporalPort.findAll(eq(USUARIO), any())).thenReturn(List.of(peso("80.00")));

        CalculoObjetivo c = service.calcular(USUARIO, HOY, TipoObjetivo.MANTENIMIENTO, null, null);

        // 10*80 + 6.25*180 - 5*30 + 5 = 1780; * 1.55 = 2759
        assertThat(c.getEdad()).isEqualTo(30);
        assertThat(c.getTmb()).isEqualTo(1780);
        assertThat(c.getGastoTotal()).isEqualTo(2759);
        assertThat(c.getKcalDiarias()).isEqualTo(2759);
        assertThat(c.getProteinasObj()).isEqualTo(160);
        // grasas: 2759 * 0.25 / 9 = 76.6 -> 77; carbos: (2759 - 640 - 693) / 4 = 356.5 -> 357
        assertThat(c.getGrasasObj()).isEqualTo(77);
        assertThat(c.getCarbosObj()).isEqualTo(357);
        assertThat(c.getPesoFecha()).isEqualTo(HOY.minusDays(1));
    }

    @Test
    @DisplayName("definicion resta 500 y volumen suma 300; mujer resta 161 en el basal")
    void tiposYSexo() {
        DatosCorporales mujer = DatosCorporales.builder().alturaCm(165).fechaNacimiento(LocalDate.of(1990, 6, 1))
                .sexo(Sexo.MUJER).nivelActividad(NivelActividad.SEDENTARIO).build();
        when(datosCorporalesPort.find(USUARIO)).thenReturn(Optional.of(mujer));

        // 10*60 + 6.25*165 - 5*36 - 161 = 1290.25; * 1.2 = 1548.3
        CalculoObjetivo def = service.calcular(USUARIO, HOY, TipoObjetivo.DEFINICION, null, new BigDecimal("60"));
        CalculoObjetivo vol = service.calcular(USUARIO, HOY, TipoObjetivo.VOLUMEN, null, new BigDecimal("60"));

        assertThat(def.getTmb()).isEqualTo(1290);
        assertThat(def.getKcalDiarias()).isEqualTo(1048);
        assertThat(vol.getKcalDiarias()).isEqualTo(1848);
        assertThat(def.getPesoFecha()).isNull();
        verify(pesoCorporalPort, never()).findAll(any(), any());
    }

    @Test
    @DisplayName("el nivel de actividad de la peticion sustituye al del perfil")
    void nivelDeLaPeticion() {
        when(datosCorporalesPort.find(USUARIO)).thenReturn(Optional.of(hombre()));

        CalculoObjetivo c = service.calcular(USUARIO, HOY, TipoObjetivo.MANTENIMIENTO,
                NivelActividad.SEDENTARIO, new BigDecimal("80"));

        assertThat(c.getNivelActividad()).isEqualTo(NivelActividad.SEDENTARIO);
        assertThat(c.getGastoTotal()).isEqualTo(2136);
    }

    @Test
    @DisplayName("sin perfil ni peso, el error lista todo lo que falta")
    void sinDatos() {
        when(datosCorporalesPort.find(USUARIO)).thenReturn(Optional.empty());
        when(pesoCorporalPort.findAll(eq(USUARIO), any())).thenReturn(List.of());

        assertThatThrownBy(() -> service.calcular(USUARIO, HOY, TipoObjetivo.MANTENIMIENTO, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("la altura")
                .hasMessageContaining("la fecha de nacimiento")
                .hasMessageContaining("el sexo")
                .hasMessageContaining("el nivel de actividad")
                .hasMessageContaining("un peso registrado");
    }
}
