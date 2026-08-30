-- Libros: se eligio OpenLibrary sobre Google Books (docs/modulos/odisea.md).
--
-- fuente_externa es un ENUM de MySQL, asi que cambiar el valor del enum de
-- Java no basta: hay que cambiar tambien la columna o Hibernate guardaria un
-- valor que la tabla no admite.
--
-- No hay filas que migrar: GOOGLE_BOOKS nunca llego a usarse, solo existia
-- como valor previsto. Si las hubiera, esto necesitaria un UPDATE en medio y
-- dos ALTER, porque MySQL no deja cambiar un valor de ENUM que este en uso.

ALTER TABLE `titulo`
    MODIFY `fuente_externa` enum('TMDB','IGDB','OPEN_LIBRARY','MANUAL') NOT NULL;
