-- Revision de indices, cierre de B8.
--
-- Salio de pasar EXPLAIN a las consultas reales de todos los modulos sobre
-- MySQL 8.4 con ~350.000 series, ~250.000 movimientos y ~50.000 entradas
-- repartidas entre 8 usuarios. Solo una consulta fallaba (full scan con
-- volumen real): los records de Atlas. Ver
-- docs/decisiones/028-revision-de-indices-b8.md.
--
-- Records, primera consulta: el peso maximo de cada ejercicio sale de una
-- tabla derivada (max(peso_kg) ... where usuario_id = ? group by ejercicio_id).
-- Con el indice anterior (ejercicio_id, sesion_id) MySQL recorria TODAS las
-- series leyendo cada fila de la tabla para ver usuario_id y peso_kg: 0,5 s
-- solo en la derivada, 0,75 s la consulta, y crece lineal con la tabla (con
-- un solo usuario, 0,3 s con 98.000 series). Este indice cubre las tres
-- columnas: la derivada sale del propio indice sin leer ninguna fila
-- (loose index scan) y la consulta externa entra por (ejercicio_id,
-- usuario_id, peso_kg) con los tres valores. 0,75 s -> 2 ms.
--
-- usuario_id va en SEGUNDO lugar a proposito: con usuario_id el primero, el
-- optimizador pasaba a usar el indice tambien en la segunda consulta de
-- records (volumen por ejercicio y sesion) y la dejaba mas lenta que sin el.
--
-- Sustituye a idx_serie_registro_ejercicio_sesion (V14) en vez de sumarse:
-- empieza igual por ejercicio_id, asi que sigue sirviendo a la clave ajena
-- fk_serie_registro_ejercicio, a la comprobacion de uso de un ejercicio y a
-- la progresion (que ahora entra por ejercicio_id y usuario_id y lee solo las
-- series del usuario). El antiguo no mejoraba ninguna consulta: la progresion
-- une con sesion por clave primaria y no necesita sesion_id en el indice. Asi
-- serie_registro, que es la tabla que mas crece, no paga un indice mas en
-- cada insercion.
--
-- ADD y DROP van en la misma sentencia: la clave ajena nunca se queda sin
-- indice.

ALTER TABLE `serie_registro`
    ADD INDEX `idx_serie_registro_ejercicio_usuario_peso` (`ejercicio_id`, `usuario_id`, `peso_kg`),
    DROP INDEX `idx_serie_registro_ejercicio_sesion`;
