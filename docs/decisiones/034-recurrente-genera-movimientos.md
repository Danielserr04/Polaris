# 034 — Recurrente: genera sus movimientos con un job diario

Estado: aceptada · 2026-10-02

## Contexto

Lumen tenía suscripciones y pagos a plazos que se cobraban solos. En [[kuiper]] el `Movimiento` solo tenía la marca `recurrente`, sin nada que lo generase.

## Decisión

- Nueva entidad **`Recurrente`** (tabla `recurrente`, V16): concepto, importe, tipo, categoría, método de pago, `frecuencia` (`SEMANAL`, `MENSUAL`, `ANUAL`), `fecha_inicio`, `proxima_fecha`, `cuotas_total` (nulo = sin fin), `cuotas_pagadas` y `activo`.
- **Un job diario** (`RecurrenteJob`, 00:05 Europe/Madrid y al arrancar) crea un `Movimiento` con `recurrente = true` por **cada** cargo con `proxima_fecha <= hoy` y avanza la fecha. Si el servidor estuvo apagado, recupera todos los atrasados.
- **El día de cobro lo fija `fecha_inicio`**: un recibo del 31 cae el 30 en abril, el 28 en febrero y vuelve al 31.
- **Plazos**: con `cuotas_total`, el concepto se numera ("Móvil (3/12)") y al pagar la última se desactiva solo.
- Al crear, el primer cargo es `fecha_inicio` aunque sea pasada: se generan los atrasados (sirve para dar de alta algo que empezó hace meses).
- **Pausar** es `activo = false` por `PUT`. **Reactivar no cobra la pausa**: el próximo cargo pasa a ser el primero desde hoy. Cambiar `fecha_inicio` también recalcula desde hoy.
- Misma regla que movimiento: la categoría es del usuario y del mismo tipo ([[012-movimiento-categoria-mismo-tipo]]). Una categoría con recurrentes no se borra ni cambia de tipo.
- Borrar un recurrente no borra los movimientos que ya generó.
- El job se apaga con `polaris.jobs.activos: false` (los tests lo hacen).

## Alternativas descartadas

- **Generar solo el último cargo atrasado.** Perdería gastos reales si el servidor estuvo parado.
- **Calcular los cargos al vuelo sin crear movimientos.** El resumen, los filtros y el análisis tendrían que mezclar dos fuentes.
- **Avanzar con `plusMonths` sobre la fecha anterior.** Un 31 se quedaría en 28 para siempre después de febrero.

## Consecuencias

- Una pasada del job es una sola transacción: o se generan todos los cargos o ninguno.
- No hay enlace entre el movimiento y su recurrente; solo la marca `recurrente`.
