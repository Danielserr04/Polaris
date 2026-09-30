package com.polaris.fusion.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Los macros no se guardan: se calculan como valor_100g * cantidad_g / 100,
 * escala 2, HALF_UP. Aqui esta el calculo y el redondeo; sin Spring ni mocks.
 */
class MacrosTest {

    private static Alimento alimento(String kcal, String prot, String carb, String grasa) {
        return Alimento.builder().id(1L).nombre("Test").kcal100g(new BigDecimal(kcal))
                .proteinas100g(new BigDecimal(prot)).carbohidratos100g(new BigDecimal(carb))
                .grasas100g(new BigDecimal(grasa)).build();
    }

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    @Test
    @DisplayName("100 g devuelven exactamente los valores por 100 g")
    void cienGramos() {
        Macros macros = Macros.de(alimento("130.00", "2.70", "28.00", "0.30"), bd("100"));

        assertThat(macros.kcal()).isEqualByComparingTo("130.00");
        assertThat(macros.proteinas()).isEqualByComparingTo("2.70");
        assertThat(macros.carbohidratos()).isEqualByComparingTo("28.00");
        assertThat(macros.grasas()).isEqualByComparingTo("0.30");
    }

    @Test
    @DisplayName("escala cantidades distintas de 100 g")
    void escalaLaCantidad() {
        Macros macros = Macros.de(alimento("250.00", "10.00", "30.00", "5.00"), bd("150"));

        assertThat(macros.kcal()).isEqualByComparingTo("375.00");
        assertThat(macros.proteinas()).isEqualByComparingTo("15.00");
        assertThat(macros.carbohidratos()).isEqualByComparingTo("45.00");
        assertThat(macros.grasas()).isEqualByComparingTo("7.50");
    }

    @Test
    @DisplayName("el resultado siempre tiene escala 2")
    void escalaDos() {
        Macros macros = Macros.de(alimento("100", "10", "20", "5"), bd("50"));

        assertThat(macros.kcal().scale()).isEqualTo(2);
        assertThat(macros.proteinas().scale()).isEqualTo(2);
        assertThat(macros.carbohidratos().scale()).isEqualTo(2);
        assertThat(macros.grasas().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("HALF_UP: el empate exacto (.005) sube")
    void redondeaEmpateHaciaArriba() {
        // 0.50 * 1.00 / 100 = 0.005 -> 0.01
        Macros macros = Macros.de(alimento("0.50", "0.50", "0.50", "0.50"), bd("1.00"));

        assertThat(macros.kcal()).isEqualByComparingTo("0.01");
        assertThat(macros.grasas()).isEqualByComparingTo("0.01");
    }

    @Test
    @DisplayName("HALF_UP: por debajo del empate (.0049..) baja")
    void redondeaPorDebajoHaciaAbajo() {
        // 0.49 * 1.00 / 100 = 0.0049 -> 0.00
        Macros macros = Macros.de(alimento("0.49", "0.49", "0.49", "0.49"), bd("1.00"));

        assertThat(macros.kcal()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("HALF_UP con decimales de etiqueta reales (a diferencia de HALF_EVEN)")
    void redondeaCasoRealDeDecimales() {
        // 33.33 * 33.33 / 100 = 11.108889 -> 11.11
        Macros a = Macros.de(alimento("33.33", "33.33", "33.33", "33.33"), bd("33.33"));
        assertThat(a.kcal()).isEqualByComparingTo("11.11");

        // 12.35 * 10.00 / 100 = 1.235 -> 1.24 (empate, sube)
        Macros b = Macros.de(alimento("12.35", "12.35", "12.35", "12.35"), bd("10.00"));
        assertThat(b.kcal()).isEqualByComparingTo("1.24");

        // 12.25 * 10.00 / 100 = 1.225 -> 1.23 (HALF_EVEN daria 1.22)
        Macros c = Macros.de(alimento("12.25", "12.25", "12.25", "12.25"), bd("10.00"));
        assertThat(c.kcal()).isEqualByComparingTo("1.23");
    }

    @Test
    @DisplayName("cantidad maxima (10000 g) con el alimento mas calorico (900 kcal) no desborda")
    void extremos() {
        Macros macros = Macros.de(alimento("900.00", "100.00", "100.00", "100.00"), bd("10000.00"));

        assertThat(macros.kcal()).isEqualByComparingTo("90000.00");
        assertThat(macros.proteinas()).isEqualByComparingTo("10000.00");
    }

    @Test
    @DisplayName("plus suma campo a campo y CERO es el elemento neutro")
    void plusYCero() {
        Macros a = new Macros(bd("1.10"), bd("2.20"), bd("3.30"), bd("4.40"));
        Macros b = new Macros(bd("0.05"), bd("0.05"), bd("0.05"), bd("0.05"));

        Macros suma = Macros.CERO.plus(a).plus(b);

        assertThat(suma.kcal()).isEqualByComparingTo("1.15");
        assertThat(suma.proteinas()).isEqualByComparingTo("2.25");
        assertThat(suma.carbohidratos()).isEqualByComparingTo("3.35");
        assertThat(suma.grasas()).isEqualByComparingTo("4.45");
    }

    @Test
    @DisplayName("el total de la comida es la suma de las lineas ya redondeadas")
    void totalEsSumaDeLineasRedondeadas() {
        // Cada linea: 0.50 * 1.00 / 100 = 0.005 -> 0.01. Tres lineas: 0.03, no 0.015 -> 0.02.
        Alimento a = alimento("0.50", "0.50", "0.50", "0.50");
        ComidaLinea linea = ComidaLinea.builder().alimento(a).alimentoId(1L).cantidadG(bd("1.00")).build();
        Comida comida = Comida.builder().lineas(List.of(linea, linea, linea)).build();

        assertThat(comida.getTotales().kcal()).isEqualByComparingTo("0.03");
    }

    @Test
    @DisplayName("una comida sin lineas suma cero")
    void comidaSinLineasSumaCero() {
        assertThat(Comida.builder().build().getTotales()).isEqualTo(Macros.CERO);
    }

    @Test
    @DisplayName("getMacros de una linea sin alimento cargado falla claramente")
    void lineaSinAlimento() {
        ComidaLinea linea = ComidaLinea.builder().alimentoId(1L).cantidadG(bd("100")).build();

        assertThatThrownBy(linea::getMacros).isInstanceOf(IllegalStateException.class);
    }
}
