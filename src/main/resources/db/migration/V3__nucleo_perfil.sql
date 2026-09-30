-- Nucleo, entidad Perfil (fase B4).
--
-- Un perfil por usuario: de ahi el unique de usuario_id, que ademas sirve de
-- indice para la unica consulta que se hace (buscar por usuario).
-- Ver docs/decisiones/009-perfil-unico-por-usuario.md.
--
-- Todo nullable salvo usuario_id: se puede guardar un perfil a medias.
-- sexo y nivel_actividad como ENUM de MySQL, igual que el resto de enums del
-- esquema (V1): es lo que Hibernate espera al validar un @Enumerated(STRING).
-- Anadir un valor al enum de Java obliga a una migracion nueva, como en V2.

CREATE TABLE `perfil` (
  `id`               bigint NOT NULL AUTO_INCREMENT,
  `usuario_id`       bigint NOT NULL,
  `altura_cm`        int    DEFAULT NULL,
  `fecha_nacimiento` date   DEFAULT NULL,
  `sexo`             enum('HOMBRE','MUJER') DEFAULT NULL,
  `nivel_actividad`  enum('SEDENTARIO','LIGERO','MODERADO','ALTO','MUY_ALTO') DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perfil_usuario` (`usuario_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
