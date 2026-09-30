-- Kuiper, entidad Categoria (fase B5).
--
-- Nombre unico por usuario y tipo: se puede tener "Otros" como INGRESO y como
-- GASTO, pero no dos "Comida" de GASTO. Con la collation utf8mb4_unicode_ci la
-- comparacion ignora mayusculas y tildes ("Comida" = "comida").
-- Ver docs/decisiones/011-categoria-nombre-unico-por-tipo.md.
--
-- tipo como ENUM de MySQL, igual que el resto de enums del esquema (V1, V3).

CREATE TABLE `categoria` (
  `id`         bigint       NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint       NOT NULL,
  `nombre`     varchar(100) NOT NULL,
  `color`      varchar(7)   DEFAULT NULL,
  `icono`      varchar(50)  DEFAULT NULL,
  `tipo`       enum('INGRESO','GASTO') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_categoria_usuario_nombre_tipo` (`usuario_id`, `nombre`, `tipo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
