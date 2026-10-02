-- Nucleo, avisos al movil (Web Push). Ver docs/decisiones/044-recordatorios.md.
--
-- suscripcion_push: un dispositivo que ha pedido recibir avisos. endpoint es
-- la URL del servicio de push del fabricante para ese dispositivo y lo
-- identifica: es unico en toda la tabla (un navegador usado con dos cuentas
-- pasa a la ultima). p256dh y auth son sus claves, en base64url, para cifrar
-- el mensaje. Sin clave foranea a usuario, como el resto del esquema.
--
-- push_vapid: el par de claves VAPID con el que el servidor firma los avisos.
-- Una sola fila; la genera el backend la primera vez si no llegan por
-- variables de entorno (POLARIS_VAPID_PUBLICA / POLARIS_VAPID_PRIVADA). No
-- son datos personales: no lleva usuario_id.

CREATE TABLE `suscripcion_push` (
  `id`         bigint        NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint        NOT NULL,
  `endpoint`   varchar(500)  NOT NULL,
  `p256dh`     varchar(120)  NOT NULL,
  `auth`       varchar(50)   NOT NULL,
  `creada_en`  datetime(6)   NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_suscripcion_push_endpoint` (`endpoint`),
  KEY `idx_suscripcion_push_usuario` (`usuario_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `push_vapid` (
  `id`       tinyint      NOT NULL,
  `publica`  varchar(100) NOT NULL,
  `privada`  varchar(60)  NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
