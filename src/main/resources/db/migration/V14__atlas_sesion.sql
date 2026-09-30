-- Atlas, entidades Sesion y SerieRegistro (fase B7). Un solo agregado: la serie
-- no existe sin su sesion. Ver docs/decisiones/025-sesion-agregado-con-series.md.
--
-- serie_registro lleva usuario_id aunque docs/modelo-datos.md no lo listaba:
-- regla dura 5 de CLAUDE.md (toda tabla de datos personales lleva usuario_id).
-- Lo fija el servicio igual que el de la sesion. Es ademas la tabla que mas va
-- a crecer (casi 100 filas a la semana), y la que alimentara la progresion.
--
-- sesion.rutina_id es NULLABLE a proposito: un entreno libre, sin rutina, es
-- valido. Con valor, apunta a la rutina con la que se hizo; esa rutina no se
-- puede borrar mientras tenga sesiones (RutinaService, 400), y la FK es la red
-- de seguridad. idx_sesion_rutina sirve a esa comprobacion y al filtro
-- ?rutinaId= del listado.
--
-- serie_registro.ejercicio_id: un ejercicio con series no se puede borrar
-- (EjercicioService, 400); la FK es la red de seguridad. El indice
-- idx_serie_registro_ejercicio_sesion (ejercicio_id, sesion_id) es el previsto en
-- docs/modelo-datos.md para la progresion por ejercicio; su prefijo
-- (ejercicio_id) sirve tambien a la comprobacion de borrado y a la FK.
--
-- Pesos y RPE son DECIMAL, nunca FLOAT/DOUBLE (regla dura 7 de CLAUDE.md).
-- peso_kg es NOT NULL: 0 para un ejercicio con el peso corporal. rpe es
-- opcional. Que el peso este en 0-1000 kg, el rpe en 1-10 en pasos de 0.5, las
-- reps en 1-999 y numero_serie >= 1 lo valida el servicio, no la base.
--
-- SIN unique (sesion_id, ejercicio_id, numero_serie) a proposito: que el numero
-- de serie no se repita dentro de un ejercicio de la sesion lo valida el
-- servicio. Como Hibernate ejecuta los INSERT antes que los DELETE, un PUT que
-- reemplaza las series chocaria con el unique contra las series viejas, que aun
-- no se han borrado (mismo motivo que rutina_ejercicio en V13).
--
-- Sin unique (usuario_id, fecha): un dia puede tener dos sesiones.
--
-- El orden de las series dentro de la sesion (ejercicios en el orden en que
-- aparecen, numero_serie ascendente dentro de cada uno) es el orden de id: el
-- servicio las inserta ya ordenadas y las lecturas usan ORDER BY id.
--
-- Como en V10 y V13, las FKs no llevan ON DELETE CASCADE: el borrado en cascada
-- de las series lo hace JPA (cascade + orphanRemoval en SesionEntity), para que
-- un DELETE directo contra la base no se lleve el historico sin querer. Sin FK a
-- usuario, como el resto de tablas.
--
-- idx_sesion_usuario_fecha es el indice del listado (?desde=&hasta=), como
-- idx_comida_usuario_fecha.

CREATE TABLE `sesion` (
  `id`           bigint NOT NULL AUTO_INCREMENT,
  `usuario_id`   bigint NOT NULL,
  `rutina_id`    bigint DEFAULT NULL,
  `fecha`        date   NOT NULL,
  `duracion_min` int    DEFAULT NULL,
  `notas`        text,
  PRIMARY KEY (`id`),
  KEY `idx_sesion_usuario_fecha` (`usuario_id`, `fecha`),
  KEY `idx_sesion_rutina` (`rutina_id`),
  CONSTRAINT `fk_sesion_rutina` FOREIGN KEY (`rutina_id`) REFERENCES `rutina` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `serie_registro` (
  `id`           bigint       NOT NULL AUTO_INCREMENT,
  `usuario_id`   bigint       NOT NULL,
  `sesion_id`    bigint       NOT NULL,
  `ejercicio_id` bigint       NOT NULL,
  `numero_serie` int          NOT NULL,
  `reps`         int          NOT NULL,
  `peso_kg`      decimal(6,2) NOT NULL,
  `rpe`          decimal(3,1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_serie_registro_sesion` (`sesion_id`),
  KEY `idx_serie_registro_ejercicio_sesion` (`ejercicio_id`, `sesion_id`),
  CONSTRAINT `fk_serie_registro_sesion` FOREIGN KEY (`sesion_id`) REFERENCES `sesion` (`id`),
  CONSTRAINT `fk_serie_registro_ejercicio` FOREIGN KEY (`ejercicio_id`) REFERENCES `ejercicio` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
