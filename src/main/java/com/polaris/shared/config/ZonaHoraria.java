package com.polaris.shared.config;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Fija la zona horaria por defecto de la JVM. Se llama desde main, antes de
 * arrancar Spring: TimeZone.setDefault es estado global y el driver de MySQL la
 * lee al abrir conexion, asi que despues de arrancar ya no sirve.
 */
public final class ZonaHoraria {

    public static final String POR_DEFECTO = "Europe/Madrid";

    private ZonaHoraria() {
    }

    public static void aplicar(String id) {
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(id)));
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Zona horaria no valida: " + id, e);
        }
    }
}
