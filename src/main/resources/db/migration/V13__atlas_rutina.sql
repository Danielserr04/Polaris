-- Atlas, entidades Rutina y RutinaEjercicio (fase B7). Un solo agregado: la
-- linea no existe sin su rutina. Ver docs/decisiones/024-rutina-agregado-con-lineas.md.
--
-- rutina_ejercicio lleva usuario_id aunque docs/modelo-datos.md no lo listaba:
-- regla dura 5 de CLAUDE.md (toda tabla de datos personales lleva usuario_id).
-- Lo fija el servicio igual que el de la rutina.
--
-- uk_rutina_usuario_nombre: un usuario no repite nombre de rutina. Con la
-- collation utf8mb4_unicode_ci "Push" y "push" son el mismo nombre, y tambien
-- se ignoran las tildes. RutinaService lo comprueba antes con un 409 legible; el unique
-- es la red ante dos peticiones simultaneas (409 via ADR 020). Su prefijo
-- (usuario_id) sirve tambien al listado, asi que no hay indice aparte por usuario.
--
-- SIN unique (rutina_id, orden) a proposito: que el orden no se repita dentro
-- de una rutina lo valida el servicio. Como Hibernate ejecuta los INSERT antes
-- que los DELETE, un PUT que reemplaza las lineas chocaria con el unique contra
-- las lineas viejas, que aun no se han borrado.
--
-- reps_objetivo es texto libre (varchar), para "8-12", "5" o "AMRAP".
-- series_objetivo es un int: numero de series, no de repeticiones.
--
-- Como en V10, las FKs no llevan ON DELETE CASCADE: el borrado en cascada de
-- las lineas lo hace JPA (cascade + orphanRemoval en RutinaEntity), para que un
-- DELETE directo contra la base no se lleve el historico sin querer. La FK a
-- ejercicio ademas impide borrar un ejercicio usado en una rutina.
--
-- idx_rutina_ejercicio_ejercicio sirve a la proteccion de borrado de Ejercicio
-- (existe alguna linea con este ejercicio). Sin FK a usuario, como el resto.

CREATE TABLE `rutina` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `usuario_id`  bigint       NOT NULL,
  `nombre`      varchar(100) NOT NULL,
  `descripcion` text,
  `activa`      bit(1)       NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rutina_usuario_nombre` (`usuario_id`, `nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `rutina_ejercicio` (
  `id`              bigint      NOT NULL AUTO_INCREMENT,
  `usuario_id`      bigint      NOT NULL,
  `rutina_id`       bigint      NOT NULL,
  `ejercicio_id`    bigint      NOT NULL,
  `orden`           int         NOT NULL,
  `series_objetivo` int         NOT NULL,
  `reps_objetivo`   varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rutina_ejercicio_rutina` (`rutina_id`),
  KEY `idx_rutina_ejercicio_ejercicio` (`ejercicio_id`),
  CONSTRAINT `fk_rutina_ejercicio_rutina` FOREIGN KEY (`rutina_id`) REFERENCES `rutina` (`id`),
  CONSTRAINT `fk_rutina_ejercicio_ejercicio` FOREIGN KEY (`ejercicio_id`) REFERENCES `ejercicio` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
