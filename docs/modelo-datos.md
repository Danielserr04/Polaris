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

| Campo | Tipo |
|---|---|
| id | BIGINT AUTO_INCREMENT |
| usuario_id | bigint |
| altura_cm | int |
| fecha_nacimiento | date |
| sexo | varchar |
| nivel_actividad | varchar |

**`registro_peso`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | |
| fecha | date | único por usuario+fecha |
| peso_kg | DECIMAL(5,2) | |
| grasa_pct | DECIMAL(4,1) | opcional |
| notas | text | |

Consumido por [[fusion]] y por [[atlas]]. El dato vive aquí una sola vez.

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
| nombre | varchar | |
| color | varchar | hex |
| icono | varchar | |
| tipo | varchar | INGRESO, GASTO |

**`movimiento`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | |
| fecha | date | índice |
| importe | DECIMAL(10,2) | siempre positivo |
| tipo | varchar | INGRESO, GASTO |
| categoria_id | bigint | FK |
| concepto | varchar | |
| metodo_pago | varchar | |
| recurrente | boolean | |

**`presupuesto`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | |
| categoria_id | bigint | FK |
| periodo | varchar | MENSUAL, ANUAL |
| importe_limite | DECIMAL(10,2) | |

`importe` siempre positivo y el signo lo pone `tipo`: evita sumas con signos mezclados.

---

## fusion — ver [[fusion]]

**`alimento`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| nombre | varchar | |
| marca | varchar | |
| kcal_100g | DECIMAL(6,2) | |
| proteinas_100g | DECIMAL(5,2) | |
| carbohidratos_100g | DECIMAL(5,2) | |
| grasas_100g | DECIMAL(5,2) | |
| fuente_externa | varchar | |
| id_externo | varchar | |

**`comida`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | |
| fecha | date | |
| momento | varchar | DESAYUNO, COMIDA, CENA, SNACK |

**`comida_linea`**

| Campo | Tipo |
|---|---|
| id | BIGINT AUTO_INCREMENT |
| comida_id | bigint |
| alimento_id | bigint |
| cantidad_g | DECIMAL(7,2) |

**`objetivo_nutricional`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| usuario_id | bigint | |
| kcal_diarias | int | |
| proteinas_obj | int | gramos |
| carbos_obj | int | gramos |
| grasas_obj | int | gramos |
| vigente_desde | date | histórico, no se sobrescribe |

Los macros se guardan por 100 g y se calculan al vuelo con `cantidad_g`. Nunca se guarda el total calculado: si corriges el alimento, se corrige el histórico.

---

## atlas — ver [[atlas]]

**`ejercicio`**

| Campo | Tipo | Nota |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | |
| nombre | varchar | |
| grupo_muscular | varchar | |
| equipamiento | varchar | |
| es_propio | boolean | creado por ti vs catálogo |

**`rutina`**

| Campo | Tipo |
|---|---|
| id | BIGINT AUTO_INCREMENT |
| usuario_id | bigint |
| nombre | varchar |
| descripcion | text |
| activa | boolean |

**`rutina_ejercicio`**

| Campo | Tipo |
|---|---|
| id | BIGINT AUTO_INCREMENT |
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
| `serie_registro` | `(ejercicio_id, sesion_id)` | progresión por ejercicio |
| `registro_peso` | `(usuario_id, fecha)` único | un peso por día |
