-- Atlas, entidad MetaEntreno (traida de FitCore).
--
-- Solo la definicion de la meta: el valor actual, el progreso y si esta
-- conseguida se calculan al leer. valor_inicial es el punto de partida
-- (peso o record el dia de crearla). Ver
-- docs/decisiones/043-logros-calculados-y-metas.md.
--
-- Borrar un ejercicio propio borra sus metas (ON DELETE CASCADE): una meta de
-- un ejercicio que ya no existe no se puede medir.
-- Pesos en DECIMAL, nunca FLOAT/DOUBLE (regla dura de CLAUDE.md).

CREATE TABLE `meta_entreno` (
  `id`             bigint       NOT NULL AUTO_INCREMENT,
  `usuario_id`     bigint       NOT NULL,
  `tipo`           enum('PESO_CORPORAL','MARCA_EJERCICIO','SESIONES_SEMANA') NOT NULL,
  `ejercicio_id`   bigint       DEFAULT NULL,
  `valor_objetivo` decimal(6,2) NOT NULL,
  `valor_inicial`  decimal(6,2) DEFAULT NULL,
  `fecha_limite`   date         DEFAULT NULL,
  `creada_en`      date         NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_meta_entreno_usuario` (`usuario_id`),
  KEY `idx_meta_entreno_ejercicio` (`ejercicio_id`),
  CONSTRAINT `fk_meta_entreno_ejercicio` FOREIGN KEY (`ejercicio_id`) REFERENCES `ejercicio` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
