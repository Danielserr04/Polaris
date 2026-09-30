-- Atlas, entidad Ejercicio (fase B7).
--
-- Catalogo compartido mas ejercicios propios en la misma tabla. usuario_id es
-- NULLABLE a proposito: NULL es un ejercicio del catalogo (de todos, solo
-- lectura); con valor, un ejercicio propio de ese usuario. Regla dura 5 de
-- CLAUDE.md: los datos personales (los ejercicios propios) llevan usuario_id;
-- el catalogo es un dato del mundo, no personal.
-- Ver docs/decisiones/023-ejercicio-catalogo-y-propios.md.
--
-- Diferencia con docs/modelo-datos.md: no hay columna es_propio. Es
-- (usuario_id IS NOT NULL) y guardarla aparte permitiria filas contradictorias
-- (es_propio = 1 sin dueno). La API la sigue exponiendo, derivada.
--
-- uk_ejercicio_usuario_nombre: un usuario no repite nombre entre los suyos. En
-- MySQL los NULL no colisionan en un unique, asi que NO protege el catalogo
-- ni el choque de un ejercicio propio con uno del catalogo: eso lo aplica
-- EjercicioService con un 409. Con la collation utf8mb4_unicode_ci "Press
-- banca" y "press banca" son el mismo nombre.
--
-- grupo_muscular y equipamiento son texto libre (varchar), como en
-- modelo-datos.md; el filtro ?grupoMuscular= es una igualdad que ignora
-- mayusculas y tildes por la collation.
--
-- Sin FK a usuario, como el resto de tablas (V5, V6, V10). idx_ejercicio_grupo_muscular
-- sirve al filtro del listado.

CREATE TABLE `ejercicio` (
  `id`             bigint       NOT NULL AUTO_INCREMENT,
  `usuario_id`     bigint       DEFAULT NULL,
  `nombre`         varchar(150) NOT NULL,
  `grupo_muscular` varchar(50)  NOT NULL,
  `equipamiento`   varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ejercicio_usuario_nombre` (`usuario_id`, `nombre`),
  KEY `idx_ejercicio_grupo_muscular` (`grupo_muscular`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
