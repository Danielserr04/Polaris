-- Kuiper, metas de ahorro con su historial de aportaciones. Traidas de
-- lumen-app. Ver docs/decisiones/036-meta-ahorro-con-aportaciones.md.
--
-- Lo ahorrado no es una columna: es la suma de aportacion_meta.importe. Asi
-- queda el historial y no hay un total que se desincronice. importe con signo:
-- positivo aporta, negativo retira. Que la suma nunca baje de 0 lo comprueba
-- MetaAhorroService (MySQL no expresa una regla sobre la suma de otra tabla
-- sin triggers).
--
-- Nombre unico por usuario; con utf8mb4_unicode_ci ignora mayusculas y tildes.
-- Borrar una meta borra sus aportaciones (ON DELETE CASCADE): sin la meta no
-- significan nada. DECIMAL, nunca FLOAT/DOUBLE (regla dura de CLAUDE.md).

CREATE TABLE `meta_ahorro` (
  `id`               bigint        NOT NULL AUTO_INCREMENT,
  `usuario_id`       bigint        NOT NULL,
  `nombre`           varchar(100)  NOT NULL,
  `importe_objetivo` decimal(10,2) NOT NULL,
  `fecha_limite`     date          DEFAULT NULL,
  `color`            varchar(7)    DEFAULT NULL,
  `icono`            varchar(50)   DEFAULT NULL,
  `creada_en`        datetime(6)   NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meta_ahorro_usuario_nombre` (`usuario_id`, `nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `aportacion_meta` (
  `id`         bigint        NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint        NOT NULL,
  `meta_id`    bigint        NOT NULL,
  `fecha`      date          NOT NULL,
  `importe`    decimal(10,2) NOT NULL,
  `nota`       varchar(255)  DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_aportacion_meta_meta_fecha` (`meta_id`, `fecha`),
  CONSTRAINT `fk_aportacion_meta_meta` FOREIGN KEY (`meta_id`) REFERENCES `meta_ahorro` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
