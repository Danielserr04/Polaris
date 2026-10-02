package com.polaris.shared.logro;

import java.time.LocalDate;

/**
 * Un paso hacia un logro: el dia en que se avanzo y cuanto. Diez sesiones son
 * diez hitos de 1 con la fecha de cada sesion. La fecha puede ser nula si el
 * dato no la tiene (una receta, un titulo terminado sin fecha de fin).
 */
public record Hito(LocalDate fecha, long cantidad) {
}
