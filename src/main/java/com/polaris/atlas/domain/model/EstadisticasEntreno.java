package com.polaris.atlas.domain.model;

import com.polaris.shared.logro.CalculoLogros.FechaImporte;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * La historia de entreno de un usuario, agregada en la base, con fechas para
 * saber cuando se consiguio cada logro. Sin orden garantizado en las listas.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasEntreno {

    /** La fecha de cada sesion: dos sesiones el mismo dia son dos fechas iguales. */
    private List<LocalDate> fechasSesion;
    /** El primer dia de cada ejercicio con alguna serie: una fecha por ejercicio distinto. */
    private List<LocalDate> primerUsoEjercicios;
    /** Reps por peso de cada sesion con series, en kg. */
    private List<FechaImporte> volumenPorSesion;
}
