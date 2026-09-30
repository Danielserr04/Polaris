-- Alimentos: se eligio Open Food Facts como API externa
-- (docs/decisiones/018-alimentos-open-food-facts.md).
--
-- fuente_externa es un ENUM de MySQL, asi que cambiar el valor del enum de
-- Java no basta: hay que cambiar tambien la columna o Hibernate guardaria un
-- valor que la tabla no admite (mismo caso que V2 con OPEN_LIBRARY).
--
-- Solo se anade un valor al final: las filas existentes (todas MANUAL) siguen
-- siendo validas y no hace falta ningun UPDATE. El unique
-- (fuente_externa, id_externo) de V8 se conserva y es lo que evita importar
-- dos veces el mismo codigo de barras.
--
-- La numeracion tiene hueco (V9 y V10 viven en otras ramas): es correcto,
-- Flyway solo exige que no se repitan.

ALTER TABLE `alimento`
    MODIFY `fuente_externa` enum('MANUAL','OPEN_FOOD_FACTS') NOT NULL;
