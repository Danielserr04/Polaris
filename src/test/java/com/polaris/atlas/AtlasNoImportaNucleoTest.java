package com.polaris.atlas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regla dura 2 (CLAUDE.md): un modulo no llama a otro. Sin ArchUnit (no hay
 * dependencia): escaneo simple de los ficheros fuente. Ver
 * docs/decisiones/022-peso-corporal-desde-fusion-y-atlas.md.
 *
 * <p>El unico sitio de Atlas que puede importar {@code com.polaris.nucleo} es
 * el adaptador de {@code infrastructure/nucleo}.
 */
class AtlasNoImportaNucleoTest {

    private static final Path ATLAS = Path.of("src/main/java/com/polaris/atlas");
    private static final Path ADAPTADOR_NUCLEO = ATLAS.resolve("infrastructure/nucleo");
    private static final String PROHIBIDO = "com.polaris.nucleo";

    private static List<Path> ficheros(Path raiz) throws IOException {
        try (Stream<Path> arbol = Files.walk(raiz)) {
            return arbol.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    /**
     * Lineas de codigo que nombran algo de Nucleo: imports, pero tambien un
     * nombre completo usado en el cuerpo. Los comentarios no cuentan.
     */
    private static List<String> referenciasANucleo(Path fichero) throws IOException {
        return Files.readAllLines(fichero).stream()
                .map(String::trim)
                .filter(linea -> !linea.startsWith("*") && !linea.startsWith("//") && !linea.startsWith("/*"))
                .filter(linea -> linea.contains(PROHIBIDO))
                .toList();
    }

    private static List<String> violaciones(List<Path> ficheros) throws IOException {
        List<String> violaciones = new java.util.ArrayList<>();
        for (Path fichero : ficheros) {
            for (String linea : referenciasANucleo(fichero)) {
                violaciones.add(fichero + " -> " + linea);
            }
        }
        return violaciones;
    }

    @Test
    @DisplayName("atlas/domain no importa nada de com.polaris.nucleo")
    void domainNoImportaNucleo() throws IOException {
        List<Path> ficheros = ficheros(ATLAS.resolve("domain"));

        // Si el escaneo no encuentra ficheros, el test pasaria en vacio.
        assertThat(ficheros).as("ficheros de atlas/domain escaneados").hasSizeGreaterThan(4);
        assertThat(violaciones(ficheros)).isEmpty();
    }

    @Test
    @DisplayName("fuera de infrastructure/nucleo, ningun fichero de atlas importa com.polaris.nucleo")
    void soloElAdaptadorConoceNucleo() throws IOException {
        List<Path> fuera = ficheros(ATLAS).stream()
                .filter(p -> !p.startsWith(ADAPTADOR_NUCLEO))
                .toList();

        assertThat(fuera).as("ficheros de atlas escaneados").hasSizeGreaterThan(20);
        assertThat(violaciones(fuera)).isEmpty();
    }

    @Test
    @DisplayName("el detector funciona: el adaptador si importa Nucleo (si no, los tests anteriores no prueban nada)")
    void elDetectorEncuentraElAdaptador() throws IOException {
        assertThat(violaciones(ficheros(ADAPTADOR_NUCLEO))).isNotEmpty();
    }
}
