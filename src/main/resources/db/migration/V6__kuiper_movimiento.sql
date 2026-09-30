-- Kuiper, entidad Movimiento (fase B5).
--
-- importe siempre positivo: el signo lo pone tipo (docs/modulos/kuiper.md).
-- DECIMAL, nunca FLOAT/DOUBLE (regla dura de CLAUDE.md).
--
-- Que tipo coincida con el de la categoria lo comprueba MovimientoService, no
-- la base de datos: MySQL no puede expresar una regla entre dos tablas sin
-- triggers. Ver docs/decisiones/012-movimiento-categoria-mismo-tipo.md.
--
-- idx_movimiento_usuario_fecha es el indice previsto en docs/modelo-datos.md
-- ("vistas por mes"): todo listado filtra por usuario y rango de fechas.

CREATE TABLE `movimiento` (
  `id`           bigint        NOT NULL AUTO_INCREMENT,
  `usuario_id`   bigint        NOT NULL,
  `fecha`        date          NOT NULL,
  `importe`      decimal(10,2) NOT NULL,
  `tipo`         enum('INGRESO','GASTO') NOT NULL,
  `categoria_id` bigint        NOT NULL,
  `concepto`     varchar(255)  DEFAULT NULL,
  `metodo_pago`  varchar(50)   DEFAULT NULL,
  `recurrente`   bit(1)        NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_movimiento_usuario_fecha` (`usuario_id`, `fecha`),
  KEY `idx_movimiento_categoria` (`categoria_id`),
  CONSTRAINT `fk_movimiento_categoria` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
