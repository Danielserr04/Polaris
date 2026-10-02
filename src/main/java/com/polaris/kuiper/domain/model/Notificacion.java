package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en NotificacionEntity.
 *
 * <p>Un aviso dentro de la app. No se crea por HTTP: lo crean otros servicios
 * del modulo (recurrentes, presupuestos, resumen) a traves de
 * CrearNotificacionInterface. Por HTTP solo se lee, se marca leida y se borra.
 *
 * <p>{@code clave} identifica el hecho que se avisa ("recurrente-4-2026-10-05",
 * "presupuesto-excedido-7-2026-10"...) y es unica por usuario: el mismo hecho
 * nunca se avisa dos veces aunque el job pase varias veces.
 *
 * <p>{@code enlace} es una pista para el frontend, la pestana de Kuiper a la
 * que lleva: "movimientos", "recurrentes", "presupuestos" o "resumen". Puede
 * ser nulo.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    public static final String ENLACE_MOVIMIENTOS = "movimientos";
    public static final String ENLACE_RECURRENTES = "recurrentes";
    public static final String ENLACE_PRESUPUESTOS = "presupuestos";
    public static final String ENLACE_RESUMEN = "resumen";

    private Long id;
    private Long usuarioId;
    private TipoNotificacion tipo;
    private String clave;
    private String titulo;
    private String texto;
    private String enlace;
    private boolean leida;
    private LocalDateTime creadaEn;
}
