package com.polaris.shared.persistence;

/**
 * Construye patrones para LIKE en los que el texto del usuario es literal:
 * "50%" busca "50%" y "a_b" busca "a_b", no "50 y lo que sea" ni "a, un
 * caracter cualquiera, b". Quien lo use tiene que declarar {@link #ESCAPE}
 * como caracter de escape: {@code cb.like(expr, patron, PatronLike.ESCAPE)}.
 *
 * <p>Vive en shared/ porque lo usan varios modulos y shared/ no conoce a
 * ninguno.
 */
public final class PatronLike {

    /** Caracter de escape que hay que pasar a cb.like(..., patron, ESCAPE). */
    public static final char ESCAPE = '\\';

    private PatronLike() {
    }

    /** Patron "contiene" en minusculas, con %, _ y el escape del texto escapados. */
    public static String contieneMinusculas(String texto) {
        return "%" + escapar(texto.toLowerCase()) + "%";
    }

    /** Antepone el escape a %, _ y al propio escape. */
    static String escapar(String texto) {
        StringBuilder sb = new StringBuilder(texto.length() + 4);
        for (char c : texto.toCharArray()) {
            if (c == ESCAPE || c == '%' || c == '_') {
                sb.append(ESCAPE);
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
