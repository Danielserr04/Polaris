package com.polaris.kuiper.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Formato de importes, fechas y meses para los textos de las notificaciones,
 * en espanol: "1.234,50 EUR" (con el simbolo del euro), "05/10/2026",
 * "septiembre de 2026". Un solo sitio para que todos los avisos se lean igual.
 */
public final class TextoAviso {

    private static final Locale ES = Locale.forLanguageTag("es-ES");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private TextoAviso() {
    }

    public static String euros(BigDecimal importe) {
        return String.format(ES, "%,.2f €", importe.setScale(2, RoundingMode.HALF_UP));
    }

    public static String fecha(LocalDate fecha) {
        return fecha.format(FECHA);
    }

    public static String mes(YearMonth mes) {
        return mes.getMonth().getDisplayName(TextStyle.FULL, ES) + " de " + mes.getYear();
    }

    /** Corta a {@code max} caracteres: los textos van a columnas con limite. */
    public static String cortar(String texto, int max) {
        return texto.length() > max ? texto.substring(0, max) : texto;
    }
}
