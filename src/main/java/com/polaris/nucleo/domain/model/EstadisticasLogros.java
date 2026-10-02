package com.polaris.nucleo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/** Lo que necesitan los logros de Nucleo, sin orden garantizado en las listas. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasLogros {

    /** Un dia por peso apuntado (hay uno por dia como mucho). */
    private List<LocalDate> fechasPeso;
    /** Un dia por medida apuntada (una por dia como mucho). */
    private List<LocalDate> fechasMedida;
    /** Campos del perfil rellenos, de 0 a 4. */
    private int camposPerfil;
}
