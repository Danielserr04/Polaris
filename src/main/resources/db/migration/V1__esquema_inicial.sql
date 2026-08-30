-- Esquema inicial de Polaris, cierre de la fase B2.
--
-- Sale del volcado de la base que construyo Hibernate con ddl-auto: update
-- durante B0-B2 (docs/decisiones/007-esquema-ddl-auto-luego-flyway.md).
-- A partir de aqui el esquema NO lo toca Hibernate: ddl-auto pasa a validate
-- y cualquier cambio es una migracion nueva.
--
-- Unico cambio respecto al volcado: los nombres de indices y claves ajenas.
-- Hibernate los genera como hashes (UK5171l57faosmj8myawaucatdw) y este
-- fichero es la referencia del esquema de ahora en adelante. Hibernate no
-- valida nombres de constraint, solo tablas, columnas y tipos.

CREATE TABLE `titulo` (
  `id`              bigint       NOT NULL AUTO_INCREMENT,
  `tipo`            enum('JUEGO','LIBRO','PELICULA','SERIE') NOT NULL,
  `titulo`          varchar(255) NOT NULL,
  `titulo_original` varchar(255) DEFAULT NULL,
  `anio`            int          DEFAULT NULL,
  `sinopsis`        text,
  `imagen_url`      varchar(255) DEFAULT NULL,
  `generos`         varchar(255) DEFAULT NULL,
  `duracion_min`    int          DEFAULT NULL,
  `fuente_externa`  enum('GOOGLE_BOOKS','IGDB','MANUAL','TMDB') NOT NULL,
  `id_externo`      varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  -- Evita fichas duplicadas al importar del catalogo externo. Los MANUAL
  -- llevan id_externo NULL y en MySQL los NULL no colisionan en un unique.
  UNIQUE KEY `uk_titulo_fuente_externa` (`fuente_externa`, `id_externo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `usuario` (
  `id`               bigint       NOT NULL AUTO_INCREMENT,
  `username`         varchar(255) NOT NULL,
  `email`            varchar(255) NOT NULL,
  `nombre`           varchar(255) NOT NULL,
  -- NULL en los usuarios que solo entran con Google.
  `password_hash`    varchar(255) DEFAULT NULL,
  `email_verificado` bit(1)       NOT NULL,
  -- NULL en los usuarios que solo entran con usuario y contrasena.
  `google_id`        varchar(255) DEFAULT NULL,
  `avatar_url`       varchar(255) DEFAULT NULL,
  `creado_en`        datetime(6)  NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_usuario_username` (`username`),
  UNIQUE KEY `uk_usuario_email` (`email`),
  UNIQUE KEY `uk_usuario_google_id` (`google_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `entrada` (
  `usuario_id`    bigint NOT NULL,
  `id`            bigint NOT NULL AUTO_INCREMENT,
  `titulo_id`     bigint NOT NULL,
  `estado`        enum('ABANDONADO','EN_CURSO','PENDIENTE','TERMINADO') NOT NULL,
  `valoracion`    int    DEFAULT NULL,
  `notas`         text,
  `fecha_inicio`  date   DEFAULT NULL,
  `fecha_fin`     date   DEFAULT NULL,
  `favorito`      bit(1) NOT NULL,
  `progreso`      int    DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_entrada_titulo` (`titulo_id`),
  -- Todo listado de entradas filtra por usuario_id dentro de la Specification.
  KEY `idx_entrada_usuario` (`usuario_id`),
  CONSTRAINT `fk_entrada_titulo` FOREIGN KEY (`titulo_id`) REFERENCES `titulo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
