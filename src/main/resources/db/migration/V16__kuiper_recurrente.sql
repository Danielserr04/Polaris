-- Kuiper, entidad Recurrente: suscripciones, recibos y pagos a plazos que
-- generan un movimiento solos cada semana, mes o año. Traida de lumen-app.
-- Ver docs/decisiones/034-recurrente-genera-movimientos.md.
--
-- proxima_fecha es el siguiente cargo pendiente; el job genera los que tengan
-- proxima_fecha <= hoy y la avanza. fecha_inicio fija el dia del mes (o del
-- año) en el que se cobra, para que un recibo del 31 vuelva al 31 despues de
-- pasar por febrero.
--
-- cuotas_total nulo = sin fin. Con valor, al llegar cuotas_pagadas a
-- cuotas_total el recurrente se desactiva solo.
--
-- Que tipo coincida con el de la categoria lo comprueba RecurrenteService,
-- como en movimiento (docs/decisiones/012-movimiento-categoria-mismo-tipo.md).

CREATE TABLE `recurrente` (
  `id`             bigint        NOT NULL AUTO_INCREMENT,
  `usuario_id`     bigint        NOT NULL,
  `concepto`       varchar(255)  NOT NULL,
  `importe`        decimal(10,2) NOT NULL,
  `tipo`           enum('INGRESO','GASTO') NOT NULL,
  `categoria_id`   bigint        NOT NULL,
  `metodo_pago`    varchar(50)   DEFAULT NULL,
  `frecuencia`     enum('SEMANAL','MENSUAL','ANUAL') NOT NULL,
  `fecha_inicio`   date          NOT NULL,
  `proxima_fecha`  date          NOT NULL,
  `cuotas_total`   int           DEFAULT NULL,
  `cuotas_pagadas` int           NOT NULL DEFAULT 0,
  `activo`         bit(1)        NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_recurrente_usuario_proxima` (`usuario_id`, `proxima_fecha`),
  KEY `idx_recurrente_activo_proxima` (`activo`, `proxima_fecha`),
  KEY `idx_recurrente_categoria` (`categoria_id`),
  CONSTRAINT `fk_recurrente_categoria` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
