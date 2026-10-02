-- Kuiper, entidad Notificacion: avisos dentro de la app (cargo recurrente
-- anotado, cargo proximo, presupuesto al 80 % o superado, meta alcanzada y
-- resumen del mes). Ver docs/decisiones/040-notificaciones-de-kuiper.md.
--
-- No se crean por HTTP: las crean los servicios del modulo y el job diario
-- de avisos. clave identifica el hecho avisado ("recurrente-4-2026-10-05",
-- "presupuesto-excedido-7-2026-10"...) y el unique (usuario_id, clave) impide
-- avisar dos veces de lo mismo. El unique tambien sirve de indice por usuario.
--
-- Sin claves foraneas: el texto se guarda ya escrito y la notificacion
-- sobrevive aunque se borre el movimiento o el presupuesto que la provoco.
-- enlace es la pestana de Kuiper a la que lleva, como pista para el frontend.

CREATE TABLE `notificacion` (
  `id`         bigint        NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint        NOT NULL,
  `tipo`       enum('CARGO_RECURRENTE','CARGO_PROXIMO','PRESUPUESTO_AVISO','PRESUPUESTO_EXCEDIDO','META_ALCANZADA','RESUMEN_MENSUAL') NOT NULL,
  `clave`      varchar(120)  NOT NULL,
  `titulo`     varchar(150)  NOT NULL,
  `texto`      varchar(500)  NOT NULL,
  `enlace`     varchar(50)   DEFAULT NULL,
  `leida`      bit(1)        NOT NULL,
  `creada_en`  datetime(6)   NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notificacion_usuario_clave` (`usuario_id`, `clave`),
  KEY `idx_notificacion_usuario_creada` (`usuario_id`, `creada_en`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
