package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo puro. Una meta personal de entreno, traida de FitCore. Ver
 * docs/decisiones/043-logros-calculados-y-metas.md.
 *
 * <p>Se guarda la definicion (tipo, objetivo, punto de partida, plazo). El
 * valor actual, el progreso y si esta conseguida no se guardan: los rellena
 * el servicio al leer, a partir de los pesos, los records y las sesiones.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaEntreno {

    private Long id;
    private Long usuarioId;
    private TipoMetaEntreno tipo;
    /** Solo en MARCA_EJERCICIO. */
    private Long ejercicioId;
    private String ejercicioNombre;
    private BigDecimal valorObjetivo;
    /**
     * De donde se partio, fijado al crear: el peso o el record de ese momento.
     * Nulo en SESIONES_SEMANA, que empieza de cero cada semana.
     */
    private BigDecimal valorInicial;
    private LocalDate fechaLimite;
    private LocalDate creadaEn;

    // Calculado al leer; nunca se persiste.
    private BigDecimal valorActual;
    /** De 0 a 100. */
    private int progresoPct;
    private boolean conseguida;
}
