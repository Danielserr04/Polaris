package com.polaris.nucleo.domain.model;

/**
 * Lo que hay que recordar hoy, ya escrito: sale en la campana y en el push.
 * enlace es la ruta del frontend a la que lleva ("/fusion", "/kuiper").
 */
public record AvisoRecordatorio(
        TipoRecordatorio tipo,
        String titulo,
        String texto,
        String enlace
) {
}
