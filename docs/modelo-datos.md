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
| recurrente | boolean | por defecto falso |

Ver [[012-movimiento-categoria-mismo-tipo]].

**`presupuesto`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | |
| categoria_id | bigint | FK a `categoria`, del mismo usuario y de tipo GASTO |
| periodo | enum | MENSUAL, ANUAL. Único con `(usuario_id, categoria_id)` |
| importe_limite | DECIMAL(10,2) | mayor que 0 |

Ver [[013-presupuesto-solo-gastos-uno-por-periodo]].

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

`serie_registro` es la tabla que más va a crecer y de la que sale toda la progresión. Índice por `(ejercicio_id, sesion_id)`.

---

## Índices previstos

| Tabla | Índice | Para qué |
|---|---|---|
| `titulo` | `(fuente_externa, id_externo)` | evitar duplicados al importar |
| `entrada` | `(usuario_id, estado)` | el listado filtrado, la consulta más frecuente |
| `movimiento` | `(usuario_id, fecha)` | vistas por mes |
| `comida` | `(usuario_id, fecha)` | resumen del día |
| `comida_linea` | `(alimento_id)` | comprobar si un alimento está en uso antes de borrarlo |
| `alimento` | `(fuente_externa, id_externo)` único | evitar duplicados al importar |
| `objetivo_nutricional` | `(usuario_id, vigente_desde)` único | el vigente en una fecha y el histórico |
| `serie_registro` | `(ejercicio_id, sesion_id)` | progresión por ejercicio |
| `registro_peso` | `(usuario_id, fecha)` único | un peso por día |
