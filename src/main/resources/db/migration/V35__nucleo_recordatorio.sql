-- Nucleo, entidad Recordatorio: avisos para apuntar comidas (Fusion), gastos
-- (Kuiper), entrenar (Atlas) y presupuestos cerca del limite (Kuiper).
-- Ver docs/decisiones/044-recordatorios.md.
--
-- La configuracion del usuario: encendido, hora y dias. Si ya has cumplido lo
-- del dia lo dice cada modulo a traves de ComprobarRecordatorioPort. Uno
-- por usuario y tipo: el unique lo garantiza y sirve de indice por usuario.
-- Los tipos que el usuario no ha tocado no tienen fila; salen por defecto.
--
-- avisado_en es el ultimo dia en que se mando al movil (un push por tipo y
-- dia como mucho) y descartado_en el ultimo en que se dio por hecho desde la
-- campana.
--
-- dias son los dias ISO separados por comas: "1,3,5" es lunes, miercoles y
-- viernes. hora es hora de Madrid (docs/decisiones/019-zona-horaria-europe-madrid.md).

CREATE TABLE `recordatorio` (
  `id`         bigint       NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint       NOT NULL,
  `tipo`       enum('COMIDAS','GASTOS','ENTRENO','PRESUPUESTO') NOT NULL,
  `activo`     bit(1)       NOT NULL,
  `hora`       time         NOT NULL,
  `dias`       varchar(13)  NOT NULL,
  `avisado_en`    date      DEFAULT NULL,
  `descartado_en` date      DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_recordatorio_usuario_tipo` (`usuario_id`, `tipo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
