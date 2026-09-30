package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.out.ComidaRepositoryPort;
import com.polaris.fusion.application.out.ObjetivoNutricionalRepositoryPort;
import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.Comida;
import com.polaris.fusion.domain.model.ComidaFilter;
import com.polaris.fusion.domain.model.ComidaLinea;
import com.polaris.fusion.domain.model.MomentoComida;
import com.polaris.fusion.domain.model.ObjetivoNutricional;
import com.polaris.fusion.domain.model.ResumenDiario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo que mas importa: que los macros sumen lo que se ve por linea (redondeo
 * HALF_UP de la ADR 017), que se compare con el objetivo vigente de ese dia y
 * que sin objetivo no haya restante ni porcentaje.
 * Ver docs/decisiones/021-resumen-diario-fusion.md.
 */
@ExtendWith(MockitoExtension.class)
class ResumenDiarioServiceTest {

    private static final Long USUARIO = 1L;
    private static final LocalDate DIA = LocalDate.of(2026, 9, 30);

    @Mock
    private ComidaRepositoryPort comidaRepository;

    @Mock
    private ObjetivoNutricionalRepositoryPort objetivoRepository;

    @InjectMocks
    private ResumenDiarioService service;

    private static Alimento alimento(String kcal, String prot, String carbos, String grasas) {
        return Alimento.builder().id(1L).nombre("Alimento").kcal100g(new BigDecimal(kcal))
                .proteinas100g(new BigDecimal(prot)).carbohidratos100g(new BigDecimal(carbos))
                .grasas100g(new BigDecimal(grasas)).build();
    }

    private static ComidaLinea linea(Alimento alimento, String gramos) {
        return ComidaLinea.builder().usuarioId(USUARIO).alimentoId(alimento.getId()).alimento(alimento)
                .cantidadG(new BigDecimal(gramos)).build();
    }

    private static Comida comida(MomentoComida momento, ComidaLinea... lineas) {
        return Comida.builder().usuarioId(USUARIO).fecha(DIA).momento(momento)
                .lineas(List.of(lineas)).build();
    }

    private static ObjetivoNutricional objetivo(int kcal, int prot, int carbos, int grasas) {
        return ObjetivoNutricional.builder().usuarioId(USUARIO).kcalDiarias(kcal).proteinasObj(prot)
                .carbosObj(carbos).grasasObj(grasas).vigenteDesde(LocalDate.of(2026, 9, 1)).build();
    }

    private void conComidas(Comida... comidas) {
        when(comidaRepository.findAll(eq(USUARIO), any(ComidaFilter.class))).thenReturn(List.of(comidas));
    }

    @Test
    @DisplayName("con objetivo: suma varias comidas y lineas y calcula restante y porcentaje")
    void conObjetivoSumaYCompara() {
        Alimento arroz = alimento("130.00", "2.70", "28.00", "0.30");
        Alimento pollo = alimento("165.00", "31.00", "0.00", "3.60");
        conComidas(
                comida(MomentoComida.COMIDA, linea(arroz, "200.00"), linea(pollo, "150.00")),
                comida(MomentoComida.CENA, linea(pollo, "100.00")));
        when(objetivoRepository.findVigente(USUARIO, DIA))
                .thenReturn(Optional.of(objetivo(2000, 150, 250, 70)));

        ResumenDiario r = service.get(USUARIO, DIA);

        // kcal: 260 + 247.5 + 165 = 672.5 ; prot: 5.4 + 46.5 + 31 = 82.9
        // carbos: 56 + 0 + 0 = 56 ; grasas: 0.6 + 5.4 + 3.6 = 9.6
        assertThat(r.getFecha()).isEqualTo(DIA);
        assertThat(r.getObjetivoVigenteDesde()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(r.getKcal().getConsumido()).isEqualByComparingTo("672.50");
        assertThat(r.getKcal().getObjetivo()).isEqualByComparingTo("2000.00");
        assertThat(r.getKcal().getRestante()).isEqualByComparingTo("1327.50");
        assertThat(r.getKcal().getPorcentaje()).isEqualByComparingTo("33.63");
        assertThat(r.getProteinas().getConsumido()).isEqualByComparingTo("82.90");
        assertThat(r.getProteinas().getRestante()).isEqualByComparingTo("67.10");
        assertThat(r.getProteinas().getPorcentaje()).isEqualByComparingTo("55.27");
        assertThat(r.getCarbohidratos().getConsumido()).isEqualByComparingTo("56.00");
        assertThat(r.getCarbohidratos().getRestante()).isEqualByComparingTo("194.00");
        assertThat(r.getGrasas().getConsumido()).isEqualByComparingTo("9.60");
        assertThat(r.getGrasas().getRestante()).isEqualByComparingTo("60.40");
    }

    @Test
    @DisplayName("restante negativo cuando se supera el objetivo, y porcentaje mayor que 100")
    void restanteNegativoSiSeExcede() {
        Alimento aceite = alimento("900.00", "0.00", "0.00", "100.00");
        conComidas(comida(MomentoComida.COMIDA, linea(aceite, "100.00")));
        when(objetivoRepository.findVigente(USUARIO, DIA))
                .thenReturn(Optional.of(objetivo(700, 100, 200, 50)));

        ResumenDiario r = service.get(USUARIO, DIA);

        assertThat(r.getKcal().getRestante()).isEqualByComparingTo("-200.00");
        assertThat(r.getKcal().getPorcentaje()).isEqualByComparingTo("128.57");
        assertThat(r.getGrasas().getRestante()).isEqualByComparingTo("-50.00");
        assertThat(r.getGrasas().getPorcentaje()).isEqualByComparingTo("200.00");
        assertThat(r.getProteinas().getRestante()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("sin objetivo vigente: consumido calculado, y objetivo, restante y porcentaje a null")
    void sinObjetivoNoComparaNiFalla() {
        Alimento arroz = alimento("130.00", "2.70", "28.00", "0.30");
        conComidas(comida(MomentoComida.COMIDA, linea(arroz, "200.00")));
        when(objetivoRepository.findVigente(USUARIO, DIA)).thenReturn(Optional.empty());

        ResumenDiario r = service.get(USUARIO, DIA);

        assertThat(r.getObjetivoVigenteDesde()).isNull();
        assertThat(r.getKcal().getConsumido()).isEqualByComparingTo("260.00");
        assertThat(List.of(r.getKcal(), r.getProteinas(), r.getCarbohidratos(), r.getGrasas()))
                .allSatisfy(m -> {
                    assertThat(m.getObjetivo()).isNull();
                    assertThat(m.getRestante()).isNull();
                    assertThat(m.getPorcentaje()).isNull();
                });
    }

    @Test
    @DisplayName("dia sin comidas: ceros con escala 2 y todo el objetivo por consumir")
    void diaSinComidas() {
        conComidas();
        when(objetivoRepository.findVigente(USUARIO, DIA))
                .thenReturn(Optional.of(objetivo(2000, 150, 250, 70)));

        ResumenDiario r = service.get(USUARIO, DIA);

        assertThat(r.getKcal().getConsumido()).isEqualTo(new BigDecimal("0.00"));
        assertThat(r.getKcal().getRestante()).isEqualTo(new BigDecimal("2000.00"));
        assertThat(r.getKcal().getPorcentaje()).isEqualTo(new BigDecimal("0.00"));
        assertThat(r.getGrasas().getConsumido()).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("dia sin comidas y sin objetivo: solo ceros")
    void diaSinComidasNiObjetivo() {
        conComidas();
        when(objetivoRepository.findVigente(USUARIO, DIA)).thenReturn(Optional.empty());

        ResumenDiario r = service.get(USUARIO, DIA);

        assertThat(r.getKcal().getConsumido()).isEqualTo(new BigDecimal("0.00"));
        assertThat(r.getKcal().getObjetivo()).isNull();
        assertThat(r.getKcal().getRestante()).isNull();
    }

    @Test
    @DisplayName("redondeo HALF_UP por linea: el total es la suma de las lineas ya redondeadas")
    void redondeoPorLineaLuegoSuma() {
        // 33.33 kcal/100g * 10 g = 3.333 -> 3.33 ; 3 lineas = 9.99 (no 10.00 = redondeo del total sin redondear)
        // 0.05 kcal/100g * 10 g = 0.005 -> 0.01 (HALF_UP)
        Alimento a = alimento("33.33", "0.05", "0.00", "0.00");
        conComidas(comida(MomentoComida.SNACK, linea(a, "10.00"), linea(a, "10.00"), linea(a, "10.00")));
        when(objetivoRepository.findVigente(USUARIO, DIA)).thenReturn(Optional.empty());

        ResumenDiario r = service.get(USUARIO, DIA);

        assertThat(r.getKcal().getConsumido()).isEqualByComparingTo("9.99");
        assertThat(r.getProteinas().getConsumido()).isEqualByComparingTo("0.03");
    }

    @Test
    @DisplayName("el porcentaje se redondea HALF_UP a 2 decimales")
    void porcentajeHalfUp() {
        // 1 kcal / 8 = 12.5 % ; con 2 decimales: 1*100/16 = 6.25 ; 1*100/32 = 3.125 -> 3.13
        Alimento a = alimento("100.00", "0.00", "0.00", "0.00");
        conComidas(comida(MomentoComida.SNACK, linea(a, "1.00")));
        when(objetivoRepository.findVigente(USUARIO, DIA))
                .thenReturn(Optional.of(objetivo(32, 100, 100, 100)));

        ResumenDiario r = service.get(USUARIO, DIA);

        assertThat(r.getKcal().getConsumido()).isEqualByComparingTo("1.00");
        assertThat(r.getKcal().getPorcentaje()).isEqualTo(new BigDecimal("3.13"));
    }

    @Test
    @DisplayName("objetivo 0 en un macro: restante calculado y porcentaje null, sin dividir por cero")
    void objetivoCeroSinPorcentaje() {
        Alimento a = alimento("100.00", "10.00", "0.00", "0.00");
        conComidas(comida(MomentoComida.COMIDA, linea(a, "100.00")));
        when(objetivoRepository.findVigente(USUARIO, DIA))
                .thenReturn(Optional.of(objetivo(2000, 0, 250, 70)));

        ResumenDiario r = service.get(USUARIO, DIA);

        assertThat(r.getProteinas().getObjetivo()).isEqualByComparingTo("0");
        assertThat(r.getProteinas().getRestante()).isEqualByComparingTo("-10.00");
        assertThat(r.getProteinas().getPorcentaje()).isNull();
    }

    @Test
    @DisplayName("pide las comidas de ese usuario y ese dia exacto, y el objetivo vigente de esa fecha")
    void pideElDiaYUsuarioCorrectos() {
        conComidas();
        when(objetivoRepository.findVigente(USUARIO, DIA)).thenReturn(Optional.empty());

        service.get(USUARIO, DIA);

        ArgumentCaptor<ComidaFilter> filtro = ArgumentCaptor.forClass(ComidaFilter.class);
        verify(comidaRepository).findAll(eq(USUARIO), filtro.capture());
        assertThat(filtro.getValue().getFecha()).isEqualTo(DIA);
        assertThat(filtro.getValue().getDesde()).isNull();
        assertThat(filtro.getValue().getHasta()).isNull();
        assertThat(filtro.getValue().getMomento()).isNull();
        verify(objetivoRepository).findVigente(USUARIO, DIA);
    }
}
