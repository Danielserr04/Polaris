-- Nucleo, entidad MedidaCorporal (traida de FitCore).
--
-- Una medicion por dia, igual que registro_peso: el unique (usuario_id, fecha)
-- lo garantiza ademas del servicio y sirve de indice para el listado.
-- Todos los perimetros son opcionales; que haya al menos uno lo exige el
-- servicio. Ver docs/decisiones/041-medida-corporal-una-por-dia.md.
--
-- Medidas en DECIMAL, nunca FLOAT/DOUBLE (regla dura de CLAUDE.md).

CREATE TABLE `medida_corporal` (
  `id`             bigint       NOT NULL AUTO_INCREMENT,
  `usuario_id`     bigint       NOT NULL,
  `fecha`          date         NOT NULL,
  `cuello_cm`      decimal(4,1) DEFAULT NULL,
  `pecho_cm`       decimal(4,1) DEFAULT NULL,
  `cintura_cm`     decimal(4,1) DEFAULT NULL,
  `cadera_cm`      decimal(4,1) DEFAULT NULL,
  `brazo_izq_cm`   decimal(4,1) DEFAULT NULL,
  `brazo_dcho_cm`  decimal(4,1) DEFAULT NULL,
  `muslo_izq_cm`   decimal(4,1) DEFAULT NULL,
  `muslo_dcho_cm`  decimal(4,1) DEFAULT NULL,
  `notas`          text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_medida_corporal_usuario_fecha` (`usuario_id`, `fecha`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
