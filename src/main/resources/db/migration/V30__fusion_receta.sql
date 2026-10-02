-- Fusion, entidades Receta y RecetaIngrediente. Un solo agregado, como comida y
-- comida_linea (V10). Ver docs/decisiones/041-receta-agregado-con-ingredientes.md.
--
-- Las recetas son de cada usuario: las dos tablas llevan usuario_id (regla dura 5).
-- cantidad_g son los gramos para la receta ENTERA, no por racion; los macros no
-- se guardan, se calculan al vuelo con alimento.*_100g. DECIMAL, nunca FLOAT.
--
-- Igual que en V10, las FKs no llevan ON DELETE CASCADE: el borrado de los
-- ingredientes lo hace JPA (cascade + orphanRemoval en RecetaEntity).
--
-- idx_receta_ingrediente_alimento sirve a la proteccion de borrado del catalogo.

CREATE TABLE `receta` (
  `id`            bigint       NOT NULL AUTO_INCREMENT,
  `usuario_id`    bigint       NOT NULL,
  `nombre`        varchar(120) NOT NULL,
  `descripcion`   varchar(500) DEFAULT NULL,
  `raciones`      int          NOT NULL,
  `instrucciones` text         DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_receta_usuario_nombre` (`usuario_id`, `nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `receta_ingrediente` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `usuario_id`  bigint       NOT NULL,
  `receta_id`   bigint       NOT NULL,
  `alimento_id` bigint       NOT NULL,
  `cantidad_g`  decimal(7,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_receta_ingrediente_receta` (`receta_id`),
  KEY `idx_receta_ingrediente_alimento` (`alimento_id`),
  CONSTRAINT `fk_receta_ingrediente_receta` FOREIGN KEY (`receta_id`) REFERENCES `receta` (`id`),
  CONSTRAINT `fk_receta_ingrediente_alimento` FOREIGN KEY (`alimento_id`) REFERENCES `alimento` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
