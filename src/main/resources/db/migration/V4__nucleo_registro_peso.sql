-- Nucleo, entidad RegistroPeso (fase B4).
--
-- Un peso por dia: el unique (usuario_id, fecha) lo garantiza a nivel de
-- base de datos ademas del servicio, y sirve de indice para el listado por
-- rango de fechas de un usuario.
-- Ver docs/decisiones/010-registro-peso-un-peso-por-dia.md.
--
-- Pesos en DECIMAL, nunca FLOAT/DOUBLE (regla dura de CLAUDE.md).

CREATE TABLE `registro_peso` (
  `id`         bigint       NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint       NOT NULL,
  `fecha`      date         NOT NULL,
  `peso_kg`    decimal(5,2) NOT NULL,
  `grasa_pct`  decimal(4,1) DEFAULT NULL,
  `notas`      text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_registro_peso_usuario_fecha` (`usuario_id`, `fecha`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
