-- Fusion, entidades Comida y ComidaLinea (fase B6). Un solo agregado: la linea
-- no existe sin su comida. Ver docs/decisiones/017-comida-agregado-con-lineas-macros-al-vuelo.md.
--
-- comida_linea lleva usuario_id aunque docs/modelo-datos.md no lo listaba:
-- regla dura 5 de CLAUDE.md (toda tabla de datos personales lleva usuario_id).
-- Lo fija el servicio igual que el de la comida.
--
-- Los macros NUNCA se guardan: se calculan al vuelo con alimento.*_100g y
-- cantidad_g. cantidad_g es DECIMAL, nunca FLOAT/DOUBLE (regla dura de CLAUDE.md).
--
-- Sin unique por (usuario_id, fecha, momento): un dia puede tener varios SNACK.
--
-- El orden del ENUM momento es el orden natural del dia: el listado ordena por
-- esta columna y MySQL ordena un ENUM por su posicion, no alfabeticamente.
--
-- El borrado en cascada de las lineas lo hace JPA (cascade + orphanRemoval en
-- ComidaEntity); las FKs no llevan ON DELETE CASCADE a proposito, para que un
-- DELETE directo contra la base no se lleve el historico sin querer.
--
-- idx_comida_usuario_fecha es el indice previsto en docs/modelo-datos.md
-- ("resumen del dia"). idx_comida_linea_alimento sirve a la proteccion de
-- borrado del catalogo (existe alguna linea con este alimento).

CREATE TABLE `comida` (
  `id`         bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `fecha`      date   NOT NULL,
  `momento`    enum('DESAYUNO','COMIDA','CENA','SNACK') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_comida_usuario_fecha` (`usuario_id`, `fecha`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `comida_linea` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `usuario_id`  bigint       NOT NULL,
  `comida_id`   bigint       NOT NULL,
  `alimento_id` bigint       NOT NULL,
  `cantidad_g`  decimal(7,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_comida_linea_comida` (`comida_id`),
  KEY `idx_comida_linea_alimento` (`alimento_id`),
  CONSTRAINT `fk_comida_linea_comida` FOREIGN KEY (`comida_id`) REFERENCES `comida` (`id`),
  CONSTRAINT `fk_comida_linea_alimento` FOREIGN KEY (`alimento_id`) REFERENCES `alimento` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
