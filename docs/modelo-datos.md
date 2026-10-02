# Modelo de datos

MySQL 8, charset `utf8mb4`. Toda tabla de datos personales lleva `usuario_id`.

Desde el cierre de B2 la fuente de verdad del esquema es `src/main/resources/db/migration/`. Esta nota lo documenta y lo explica, pero si las dos discrepan, manda la migración. Ver [[007-esquema-ddl-auto-luego-flyway]].

## auth

**`usuario`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| username | varchar | único, en minúsculas |
| email | varchar | único, en minúsculas |
| nombre | varchar | |
| password_hash | varchar | nullable, BCrypt. Nulo si solo entra con Google |
| email_verificado | boolean | |
| google_id | varchar | nullable, único. Nulo si solo tiene login nativo |
| avatar_url | varchar | |
| creado_en | timestamp | |

Un usuario puede tener login nativo, login con Google, o ambos si se vinculan
por email (ver [[auth]]). Nunca ninguno de los dos.

---

## nucleo — ver [[nucleo]]

**`perfil`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | único: un perfil por usuario |
| altura_cm | int | opcional |
| fecha_nacimiento | date | opcional |
| sexo | enum | HOMBRE, MUJER. Opcional |
| nivel_actividad | enum | SEDENTARIO, LIGERO, MODERADO, ALTO, MUY_ALTO. Opcional |

Ver [[009-perfil-unico-por-usuario]].

**`registro_peso`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | |
| fecha | date | único por usuario+fecha; no puede ser futura |
| peso_kg | DECIMAL(5,2) | mayor que 0 |
| grasa_pct | DECIMAL(4,1) | opcional, 0-100 |
| notas | text | |

Consumido por [[fusion]] y por [[atlas]]. El dato vive aquí una sola vez. Ver [[010-registro-peso-un-peso-por-dia]].

---

## odisea — ver [[odisea]]

**`titulo`** — la ficha del contenido

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| tipo | varchar | PELICULA, SERIE, JUEGO, LIBRO |
| titulo | varchar | |
| titulo_original | varchar | |
| anio | int | |
| sinopsis | text | |
| imagen_url | varchar | |
| generos | varchar | separados por coma |
| duracion_min | int | páginas si es libro |
| fuente_externa | enum | TMDB, IGDB, OPEN_LIBRARY, MANUAL |
| id_externo | varchar | índice con fuente_externa |

**`entrada`** — tu relación con esa ficha

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | |
| titulo_id | bigint | FK |
| estado | varchar | PENDIENTE, EN_CURSO, TERMINADO, ABANDONADO |
| valoracion | int | 0-10, nullable |
| notas | text | |
| fecha_inicio | date | |
| fecha_fin | date | |
| favorito | boolean | |
| progreso | int | episodio, página u horas |

Una sola tabla para los cuatro tipos de contenido. Añadir uno nuevo es un valor más en el enum.

---

## kuiper — ver [[kuiper]]

**`categoria`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | |
| nombre | varchar(100) | único por usuario+tipo |
| color | varchar(7) | hex #RRGGBB, opcional |
| icono | varchar(50) | opcional |
| tipo | enum | INGRESO, GASTO |

Ver [[011-categoria-nombre-unico-por-tipo]].

**`movimiento`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | |
| fecha | date | índice `(usuario_id, fecha)`; no puede ser futura |
| importe | DECIMAL(10,2) | siempre positivo, mayor que 0 |
| tipo | enum | INGRESO, GASTO. Debe coincidir con el de la categoría |
| categoria_id | bigint | FK a `categoria`, del mismo usuario |
| concepto | varchar(255) | opcional |
| metodo_pago | varchar(50) | opcional, texto libre |
| recurrente | boolean | por defecto falso; verdadero si lo generó un `recurrente` |
| cuenta_id | bigint | FK a `cuenta`, opcional (nulo = sin cuenta). V22 |
| borrado_en | datetime | nulo fuera de la papelera; se purga a los 30 días. V19 |

Ver [[012-movimiento-categoria-mismo-tipo]] y [[038-movimiento-papelera-y-duplicar]].

**`presupuesto`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | |
| categoria_id | bigint | FK a `categoria`, del mismo usuario y de tipo GASTO |
| periodo | enum | MENSUAL, ANUAL. Único con `(usuario_id, categoria_id)` |
| importe_limite | DECIMAL(10,2) | mayor que 0 |
| porcentaje_alerta | int | 1 a 100, por defecto 80. V17 |

Ver [[013-presupuesto-solo-gastos-uno-por-periodo]] y [[035-presupuesto-umbral-de-alerta]].

**`recurrente`** (V16)

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | |
| concepto | varchar(255) | concepto de cada movimiento generado |
| importe | DECIMAL(10,2) | siempre positivo |
| tipo | enum | INGRESO, GASTO. Debe coincidir con el de la categoría |
| categoria_id | bigint | FK a `categoria` |
| metodo_pago | varchar(50) | opcional |
| frecuencia | enum | SEMANAL, MENSUAL, ANUAL |
| fecha_inicio | date | primer cargo; fija el día de cobro |
| proxima_fecha | date | siguiente cargo pendiente |
| cuotas_total | int | nulo = sin fin |
| cuotas_pagadas | int | |
| activo | boolean | falso = pausado o plazos terminados |
| cuenta_id | bigint | FK a `cuenta`, opcional. V22 |

Ver [[034-recurrente-genera-movimientos]].

**`meta_ahorro`** y **`aportacion_meta`** (V18)

| Campo | Tipo | Nota |
|---|---|---|
| meta_ahorro.nombre | varchar(100) | único por usuario |
| meta_ahorro.importe_objetivo | DECIMAL(10,2) | mayor que 0 |
| meta_ahorro.fecha_limite | date | opcional |
| meta_ahorro.color, icono | varchar | opcionales |
| aportacion_meta.meta_id | bigint | FK a `meta_ahorro`, borrado en cascada |
| aportacion_meta.importe | DECIMAL(10,2) | con signo: positivo aporta, negativo retira |
| aportacion_meta.fecha, nota | date, varchar(255) | |

Lo ahorrado es la suma de las aportaciones; nunca baja de 0. Ver [[036-meta-ahorro-con-aportaciones]].

**`cuenta`** y **`transferencia`** (V22)

| Campo | Tipo | Nota |
|---|---|---|
| cuenta.nombre | varchar(100) | único por usuario |
| cuenta.tipo | enum | CORRIENTE, AHORRO, TARJETA, EFECTIVO |
| cuenta.saldo_inicial | DECIMAL(12,2) | con signo; el saldo actual se calcula |
| cuenta.banco, color, icono | varchar | opcionales |
| cuenta.archivada | boolean | |
| transferencia.cuenta_origen_id, cuenta_destino_id | bigint | FK a `cuenta`, distintas |
| transferencia.importe | DECIMAL(10,2) | mayor que 0 |
| transferencia.fecha, concepto | date, varchar(255) | |

Las transferencias no cuentan como ingreso ni gasto. Ver [[039-cuentas-y-transferencias]].

**`notificacion`** (V21)

| Campo | Tipo | Nota |
|---|---|---|
| tipo | enum | CARGO_RECURRENTE, CARGO_PROXIMO, PRESUPUESTO_AVISO, PRESUPUESTO_EXCEDIDO, META_ALCANZADA, RESUMEN_MENSUAL |
| clave | varchar(120) | único con `usuario_id`: el mismo aviso nunca se repite |
| titulo, texto | varchar(150), varchar(500) | |
| enlace | varchar(50) | pestaña de Kuiper a la que lleva |
| leida | boolean | |
| creada_en | datetime(6) | |

Ver [[040-notificaciones-de-kuiper]].

`importe` siempre positivo y el signo lo pone `tipo`: evita sumas con signos mezclados.

---

## fusion — ver [[fusion]]

**`alimento`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| nombre | varchar(150) | |
| marca | varchar(100) | opcional |
| kcal_100g | DECIMAL(6,2) | 0-900 |
| proteinas_100g | DECIMAL(5,2) | 0-100 |
| carbohidratos_100g | DECIMAL(5,2) | 0-100 |
| grasas_100g | DECIMAL(5,2) | 0-100 |
| fuente_externa | enum | `ENUM('MANUAL','OPEN_FOOD_FACTS')`. Único con `id_externo` |
| id_externo | varchar(255) | nulo en los MANUAL; el código de barras en los de Open Food Facts |

Catálogo compartido: sin `usuario_id`. Ver [[015-alimento-catalogo-compartido-macros-por-100g]] y [[018-alimentos-open-food-facts]]. `V8` creó el enum solo con `MANUAL`; `V11` añadió `OPEN_FOOD_FACTS`.

**`comida`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | |
| fecha | date | |
| momento | enum | DESAYUNO, COMIDA, CENA, SNACK. El orden del `ENUM` es el del día y el listado ordena por él |

Sin unique por `(usuario_id, fecha, momento)`: un día puede tener varios SNACK. Índice `(usuario_id, fecha)`. Ver [[017-comida-agregado-con-lineas-macros-al-vuelo]].

**`comida_linea`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | el mismo que el de la comida; lo fija el servicio |
| comida_id | bigint | FK a `comida`, sin `ON DELETE CASCADE` (borra JPA) |
| alimento_id | bigint | FK a `alimento`; índice para proteger el borrado del catálogo |
| cantidad_g | DECIMAL(7,2) | mayor que 0 |

**`objetivo_nutricional`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | |
| kcal_diarias | int NOT NULL | 500-10000 |
| proteinas_obj | int NOT NULL | gramos, 0-1000 |
| carbos_obj | int NOT NULL | gramos, 0-1000 |
| grasas_obj | int NOT NULL | gramos, 0-1000 |
| vigente_desde | date NOT NULL | histórico, no se sobrescribe. Único con `usuario_id` |

Ver [[016-objetivo-nutricional-historico-inmutable]].

Los macros se guardan por 100 g y se calculan al vuelo con `cantidad_g`. Nunca se guarda el total calculado: si corriges el alimento, se corrige el histórico.

---

## atlas — ver [[atlas]]

**`ejercicio`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | nullable: NULL = catálogo compartido, con valor = ejercicio propio |
| nombre | varchar(150) | único por `usuario_id` (el catálogo se valida en el servicio) |
| grupo_muscular | varchar(50) | índice |
| equipamiento | varchar(100) | nullable |

Sin columna `es_propio`: se deriva de `usuario_id`. Ver [[023-ejercicio-catalogo-y-propios]].

**`rutina`**

| Campo | Tipo |
|---|---|
| id | BIGINT AUTO_INCREMENT |
| usuario_id | bigint |
| nombre | varchar | `UNIQUE (usuario_id, nombre)` |
| descripcion | text |
| activa | boolean |

**`rutina_ejercicio`**

| Campo | Tipo |
|---|---|
| id | BIGINT AUTO_INCREMENT |
| usuario_id | bigint |
| rutina_id | bigint |
| ejercicio_id | bigint |
| orden | int |
| series_objetivo | int |
| reps_objetivo | varchar |

**`sesion`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | |
| rutina_id | bigint | nullable, entrenos libres |
| fecha | date | índice |
| duracion_min | int | |
| notas | text | |

**`serie_registro`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | regla dura 5, como `rutina_ejercicio` |
| sesion_id | bigint | |
| ejercicio_id | bigint | |
| numero_serie | int | |
| reps | int | |
| peso_kg | DECIMAL(6,2) | |
| rpe | DECIMAL(3,1) | esfuerzo percibido, opcional |

`serie_registro` es la tabla que más va a crecer y de la que sale toda la progresión. Índice por `(ejercicio_id, usuario_id, peso_kg)` (V15; ver [[028-revision-de-indices-b8]]).

---

## Índices

Los índices reales, tras la revisión de B8 con `EXPLAIN` sobre datos de volumen ([[028-revision-de-indices-b8]]). Todo `ref`/`range`/`const` salvo lo indicado como "vigilar" en esa nota. Los nombres son los de las migraciones (`V1` a `V22`, sin `V20`); la clave primaria no se lista. Los marcados como FK existen porque MySQL exige un índice por cada clave ajena y de paso sirven a la comprobación de uso antes de borrar.

| Tabla | Índice | Migración | Para qué |
|---|---|---|---|
| `usuario` | `uk_usuario_username`, `uk_usuario_email`, `uk_usuario_google_id` (únicos) | V1 | login por usuario o email, entrada con Google |
| `titulo` | `uk_titulo_fuente_externa` `(fuente_externa, id_externo)` único | V1 | evitar duplicados al importar |
| `entrada` | `idx_entrada_usuario` `(usuario_id)` | V1 | el listado filtrado (con `estado` o `tipo` filtra después del índice; ver abajo) |
| `entrada` | `idx_entrada_titulo` `(titulo_id)` | V1 | FK; "esta persona ya tiene este título" y bloquear el borrado de un título en uso |
| `perfil` | `uk_perfil_usuario` `(usuario_id)` único | V3 | un perfil por usuario y su búsqueda |
| `registro_peso` | `uk_registro_peso_usuario_fecha` `(usuario_id, fecha)` único | V4 | un peso por día y el listado por rango |
| `categoria` | `uk_categoria_usuario_nombre_tipo` `(usuario_id, nombre, tipo)` único | V5 | nombre único por tipo y el listado por usuario |
| `movimiento` | `idx_movimiento_usuario_fecha` `(usuario_id, fecha)` | V6 | vistas por mes y resumen mensual, sin `filesort` |
| `movimiento` | `idx_movimiento_categoria` `(categoria_id)` | V6 | FK; bloquear el borrado de una categoría en uso |
| `presupuesto` | `uk_presupuesto_usuario_categoria_periodo` `(usuario_id, categoria_id, periodo)` único | V7 | uno por categoría y periodo y el listado por usuario |
| `presupuesto` | `idx_presupuesto_categoria` `(categoria_id)` | V7 | FK; bloquear el borrado de una categoría en uso |
| `recurrente` | `idx_recurrente_usuario_proxima` `(usuario_id, proxima_fecha)` | V16 | listado de próximos cargos |
| `recurrente` | `idx_recurrente_activo_proxima` `(activo, proxima_fecha)` | V16 | el job de cargos pendientes |
| `meta_ahorro` | `uk_meta_ahorro_usuario_nombre` `(usuario_id, nombre)` único | V18 | nombre único y listado por usuario |
| `aportacion_meta` | `idx_aportacion_meta_meta_fecha` `(meta_id, fecha)` | V18 | FK; suma e historial de una meta |
| `movimiento` | `idx_movimiento_borrado` `(borrado_en)` | V19 | purgar la papelera |
| `cuenta` | `uk_cuenta_usuario_nombre` `(usuario_id, nombre)` único | V22 | nombre único y listado por usuario |
| `transferencia` | `idx_transferencia_usuario_fecha` `(usuario_id, fecha)` | V22 | listado por rango |
| `notificacion` | `uk_notificacion_usuario_clave` `(usuario_id, clave)` único | V21 | no repetir avisos |
| `notificacion` | `idx_notificacion_usuario_creada` `(usuario_id, creada_en)` | V21 | listado más reciente primero |
| `alimento` | `uk_alimento_fuente_externa` `(fuente_externa, id_externo)` único | V8 | evitar duplicados al importar |
| `objetivo_nutricional` | `uk_objetivo_nutricional_usuario_vigente` `(usuario_id, vigente_desde)` único | V9 | el vigente en una fecha y el histórico |
| `comida` | `idx_comida_usuario_fecha` `(usuario_id, fecha)` | V10 | resumen del día y listado por rango |
| `comida_linea` | `idx_comida_linea_comida` `(comida_id)` | V10 | FK; las líneas de una comida |
| `comida_linea` | `idx_comida_linea_alimento` `(alimento_id)` | V10 | FK; comprobar si un alimento está en uso antes de borrarlo |
| `ejercicio` | `uk_ejercicio_usuario_nombre` `(usuario_id, nombre)` único | V12 | nombre único por usuario y los ejercicios visibles |
| `ejercicio` | `idx_ejercicio_grupo_muscular` `(grupo_muscular)` | V12 | filtro por grupo (uso marginal: la tabla es pequeña) |
| `rutina` | `uk_rutina_usuario_nombre` `(usuario_id, nombre)` único | V13 | nombre único por usuario y el listado |
| `rutina_ejercicio` | `idx_rutina_ejercicio_rutina` `(rutina_id)` | V13 | FK; las líneas de una rutina |
| `rutina_ejercicio` | `idx_rutina_ejercicio_ejercicio` `(ejercicio_id)` | V13 | FK; bloquear el borrado de un ejercicio en uso |
| `sesion` | `idx_sesion_usuario_fecha` `(usuario_id, fecha)` | V14 | listado por rango y orden por fecha, sin `filesort` |
| `sesion` | `idx_sesion_rutina` `(rutina_id)` | V14 | FK; listado por rutina y bloquear el borrado de una rutina con sesiones |
| `serie_registro` | `idx_serie_registro_sesion` `(sesion_id)` | V14 | FK; las series de una sesión |
| `serie_registro` | `idx_serie_registro_ejercicio_usuario_peso` `(ejercicio_id, usuario_id, peso_kg)` | **V15** | FK; progresión por ejercicio, bloquear el borrado de un ejercicio con series y el peso máximo de los records (sale del propio índice, sin leer filas). Sustituye a `(ejercicio_id, sesion_id)` de V14 |

Revisado en B8 y **descartado** (medido, sin full scan ni `filesort` relevante con volumen real; ver [[028-revision-de-indices-b8]]):

- `entrada (usuario_id, estado)`, que figuraba aquí como previsto y nunca se creó: ganaría milisegundos sobre 14.000 entradas de un usuario.
- `movimiento (usuario_id, categoria_id, fecha)` y `comida (usuario_id, momento, fecha)`: mejoran filtros poco habituales (categoría o momento sin acotar fechas) de 10 a 1 ms.
- Índices sobre `titulo (tipo)` y `alimento (nombre)`: no los usaría ninguna consulta real (cuatro valores; `like '%x%'`).

Sin índice que lo arregle y **vigilar** (detalle y cifras en la nota): la búsqueda de texto de `titulo` y `alimento` (full scan de 30.000–40.000 filas, 30–50 ms), el segundo cálculo de records de Atlas (200 ms con 98.000 series, lineal), y los listados sin paginar. El listado de `entrada` es un N+1 de la capa JPA, no un problema de índices.
