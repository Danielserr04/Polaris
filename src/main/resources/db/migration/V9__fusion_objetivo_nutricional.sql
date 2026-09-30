-- Fusion, entidad ObjetivoNutricional (fase B6).
--
-- Historico inmutable: cada cambio de objetivo es una fila nueva con su
-- vigente_desde, nunca se sobrescribe. Ver
-- docs/decisiones/016-objetivo-nutricional-historico-inmutable.md.
--
-- El unique (usuario_id, vigente_desde) impide dos objetivos que empiecen el
-- mismo dia (no tendrian orden) y sirve de indice para "el vigente en una
-- fecha" y para el historico de un usuario.
--
-- Enteros (kcal y gramos): nada de FLOAT/DOUBLE (regla dura de CLAUDE.md).

CREATE TABLE `objetivo_nutricional` (
  `id`            bigint NOT NULL AUTO_INCREMENT,
  `usuario_id`    bigint NOT NULL,
  `kcal_diarias`  int    NOT NULL,
  `proteinas_obj` int    NOT NULL,
  `carbos_obj`    int    NOT NULL,
  `grasas_obj`    int    NOT NULL,
  `vigente_desde` date   NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_objetivo_nutricional_usuario_vigente` (`usuario_id`, `vigente_desde`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
