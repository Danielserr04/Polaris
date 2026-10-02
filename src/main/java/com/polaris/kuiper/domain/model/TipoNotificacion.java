package com.polaris.kuiper.domain.model;

/**
 * De donde sale cada aviso. Ver docs/decisiones/040-notificaciones-de-kuiper.md.
 *
 * <p>META_ALCANZADA existe ya aunque las metas de ahorro las anade otra pieza:
 * cuando una meta llegue a su objetivo, su servicio llamara a
 * CrearNotificacionInterface con este tipo.
 */
public enum TipoNotificacion {
    CARGO_RECURRENTE,
    CARGO_PROXIMO,
    PRESUPUESTO_AVISO,
    PRESUPUESTO_EXCEDIDO,
    META_ALCANZADA,
    RESUMEN_MENSUAL
}
