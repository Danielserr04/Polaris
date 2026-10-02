package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.CalculoObjetivo;
import com.polaris.fusion.domain.model.NivelActividad;
import com.polaris.fusion.domain.model.TipoObjetivo;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CalcularObjetivoNutricionalInterface {

    /**
     * {@code nivelActividad} y {@code pesoKg} son opcionales: si llegan,
     * sustituyen a los del perfil y al ultimo peso registrado.
     */
    CalculoObjetivo calcular(Long usuarioId, LocalDate hoy, TipoObjetivo tipo,
                             NivelActividad nivelActividad, BigDecimal pesoKg);
}
