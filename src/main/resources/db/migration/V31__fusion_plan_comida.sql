-- Fusion, entidades PlanComida y PlanComidaLinea: un plan semanal. Un solo
-- agregado. Ver docs/decisiones/051-plan-de-comidas-y-lista-de-la-compra.md.
--
-- Las dos tablas llevan usuario_id (regla dura 5). Como mucho un plan activo por
-- usuario: lo garantiza el servicio con un solo UPDATE al activar.
--
-- Cada linea es O BIEN un alimento con cantidad_g O BIEN una receta con
-- raciones; el CHECK lo asegura tambien en base de datos (MySQL >= 8.0.16).
-- raciones admite medias (1.5): DECIMAL(5,2), nunca FLOAT.
--
-- dia_semana y momento son ENUM en el orden de la semana y del dia: el detalle
-- ordena por ellos y MySQL ordena un ENUM por su posicion.
--
-- Sin ON DELETE CASCADE, como en V10. idx_plan_comida_linea_alimento y
-- idx_plan_comida_linea_receta sirven a la proteccion de borrado de alimentos y recetas.

CREATE TABLE `plan_comida` (
  `id`         bigint       NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint       NOT NULL,
  `nombre`     varchar(120) NOT NULL,
  `activo`     bit(1)       NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_plan_comida_usuario` (`usuario_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `plan_comida_linea` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `usuario_id`  bigint       NOT NULL,
  `plan_id`     bigint       NOT NULL,
  `dia_semana`  enum('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO','DOMINGO') NOT NULL,
  `momento`     enum('DESAYUNO','COMIDA','CENA','SNACK') NOT NULL,
  `alimento_id` bigint       DEFAULT NULL,
  `cantidad_g`  decimal(7,2) DEFAULT NULL,
  `receta_id`   bigint       DEFAULT NULL,
  `raciones`    decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_plan_comida_linea_plan` (`plan_id`),
  KEY `idx_plan_comida_linea_alimento` (`alimento_id`),
  KEY `idx_plan_comida_linea_receta` (`receta_id`),
  CONSTRAINT `fk_plan_comida_linea_plan` FOREIGN KEY (`plan_id`) REFERENCES `plan_comida` (`id`),
  CONSTRAINT `fk_plan_comida_linea_alimento` FOREIGN KEY (`alimento_id`) REFERENCES `alimento` (`id`),
  CONSTRAINT `fk_plan_comida_linea_receta` FOREIGN KEY (`receta_id`) REFERENCES `receta` (`id`),
  CONSTRAINT `chk_plan_comida_linea_tipo` CHECK (
    (`alimento_id` IS NOT NULL AND `cantidad_g` IS NOT NULL AND `receta_id` IS NULL AND `raciones` IS NULL)
    OR (`receta_id` IS NOT NULL AND `raciones` IS NOT NULL AND `alimento_id` IS NULL AND `cantidad_g` IS NULL)
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
