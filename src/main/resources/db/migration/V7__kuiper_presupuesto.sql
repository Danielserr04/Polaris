-- Kuiper, entidad Presupuesto (fase B5).
--
-- Un presupuesto por categoria y periodo: se puede tener uno MENSUAL y otro
-- ANUAL para "Comida", pero no dos MENSUALES. El unique tambien sirve de
-- indice para buscar por usuario. Ver
-- docs/decisiones/013-presupuesto-solo-gastos-uno-por-periodo.md.
--
-- Que la categoria sea de GASTO lo comprueba PresupuestoService, no la base
-- de datos: MySQL no expresa una regla entre dos tablas sin triggers.
-- DECIMAL, nunca FLOAT/DOUBLE (regla dura de CLAUDE.md).

CREATE TABLE `presupuesto` (
  `id`             bigint        NOT NULL AUTO_INCREMENT,
  `usuario_id`     bigint        NOT NULL,
  `categoria_id`   bigint        NOT NULL,
  `periodo`        enum('MENSUAL','ANUAL') NOT NULL,
  `importe_limite` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_presupuesto_usuario_categoria_periodo` (`usuario_id`, `categoria_id`, `periodo`),
  KEY `idx_presupuesto_categoria` (`categoria_id`),
  CONSTRAINT `fk_presupuesto_categoria` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
