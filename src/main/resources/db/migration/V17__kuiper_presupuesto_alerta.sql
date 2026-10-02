-- Kuiper, umbral de alerta del presupuesto. Traido de lumen-app.
-- Ver docs/decisiones/035-presupuesto-umbral-de-alerta.md.
--
-- porcentaje_alerta es el % del limite a partir del cual la categoria pasa a
-- "aviso" en el resumen (80 = avisa al llegar al 80 % de lo presupuestado).
-- Los presupuestos que ya existen se quedan con el valor por defecto, 80.
-- int y no tinyint para que encaje con el Integer de la Entity bajo
-- ddl-auto: validate. El CHECK lo aplica MySQL 8.0.16+; ademas lo valida el DTO.

ALTER TABLE `presupuesto`
  ADD COLUMN `porcentaje_alerta` int NOT NULL DEFAULT 80 AFTER `importe_limite`,
  ADD CONSTRAINT `chk_presupuesto_porcentaje_alerta` CHECK (`porcentaje_alerta` BETWEEN 1 AND 100);
