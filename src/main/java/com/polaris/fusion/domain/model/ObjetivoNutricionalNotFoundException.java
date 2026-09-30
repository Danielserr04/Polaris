package com.polaris.fusion.domain.model;

import com.polaris.shared.error.NotFoundException;

import java.time.LocalDate;

/**
 * Se traduce a 404. Se lanza cuando el usuario no tiene ningun objetivo
 * vigente en la fecha pedida (no tiene ninguno, o todos empiezan despues).
 */
public class ObjetivoNutricionalNotFoundException extends NotFoundException {

    public ObjetivoNutricionalNotFoundException(LocalDate fecha) {
        super("No hay ningun objetivo nutricional vigente en " + fecha);
    }
}
