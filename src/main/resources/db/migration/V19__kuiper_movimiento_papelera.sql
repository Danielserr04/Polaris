-- Kuiper, papelera de movimientos: borrar un movimiento ya no lo elimina, lo
-- marca con borrado_en. Se restaura poniendolo a NULL; un job diario elimina
-- de verdad los que llevan mas de 30 dias en la papelera.
-- Ver docs/decisiones/038-movimiento-papelera-y-duplicar.md.
--
-- borrado_en NULL = movimiento normal. Todas las lecturas normales (listado,
-- detalle, resumen) filtran borrado_en IS NULL.
--
-- idx_movimiento_borrado sirve a la purga del job (borrado_en < limite, de
-- todos los usuarios). La papelera de un usuario ya la acota
-- idx_movimiento_usuario_fecha por su prefijo usuario_id.

ALTER TABLE `movimiento`
  ADD COLUMN `borrado_en` datetime DEFAULT NULL,
  ADD KEY `idx_movimiento_borrado` (`borrado_en`);
