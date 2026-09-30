-- Fusion, entidad Alimento (fase B6).
--
-- Catalogo compartido, como `titulo`: sin usuario_id. Ver
-- docs/decisiones/015-alimento-catalogo-compartido-macros-por-100g.md.
--
-- Macros por 100 g y en DECIMAL, nunca FLOAT/DOUBLE (regla dura de CLAUDE.md).
-- Nunca se guarda un total: se calcula con la cantidad de cada linea.
--
-- fuente_externa solo admite MANUAL de momento: la API de alimentos esta sin
-- elegir. Cuando se elija, anadir su valor requiere una migracion nueva
-- (MODIFY del ENUM), como V2 con OPEN_LIBRARY.
--
-- El unique (fuente_externa, id_externo) evitara fichas duplicadas al
-- importar. Los MANUAL llevan id_externo NULL y en MySQL los NULL no colisionan
-- en un unique.

CREATE TABLE `alimento` (
  `id`                 bigint       NOT NULL AUTO_INCREMENT,
  `nombre`             varchar(150) NOT NULL,
  `marca`              varchar(100) DEFAULT NULL,
  `kcal_100g`          decimal(6,2) NOT NULL,
  `proteinas_100g`     decimal(5,2) NOT NULL,
  `carbohidratos_100g` decimal(5,2) NOT NULL,
  `grasas_100g`        decimal(5,2) NOT NULL,
  `fuente_externa`     enum('MANUAL') NOT NULL,
  `id_externo`         varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_alimento_fuente_externa` (`fuente_externa`, `id_externo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
