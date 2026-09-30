package com.polaris.nucleo.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Vacio a proposito. Existe porque la plantilla lo exige
 * (docs/plantilla-modulo.md), pero Perfil es uno por usuario y no tiene
 * listado que filtrar. Ver docs/decisiones/009-perfil-unico-por-usuario.md.
 */
@Getter
@Builder
@NoArgsConstructor
public class PerfilFilter {
}
