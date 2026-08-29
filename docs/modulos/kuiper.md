# Kuiper

Control de gastos. Ingresos, gastos, categorías y presupuestos.

Código nuevo. Sustituye a `lumen-app`, un intento anterior que quedó sin terminar. Va en **B5** del [[roadmap]].

## Entidades

**`Categoria`** — nombre, color, icono, tipo.

**`Movimiento`** — la unidad básica: fecha, importe, tipo, categoría, concepto.

**`Presupuesto`** — límite por categoría y periodo.

Esquema completo en [[modelo-datos]].

## La decisión de diseño

**`importe` siempre positivo. El signo lo pone `tipo` (INGRESO o GASTO).**

Guardar gastos en negativo parece cómodo hasta que sumas y te sale un número que no sabes interpretar, o hasta que alguien mete un gasto en positivo por error. Con un enum, el signo es explícito y las consultas son legibles.

## Endpoints

```
GET    /api/kuiper/movimiento?desde=&hasta=&categoriaId=&tipo=
GET    /api/kuiper/movimiento/{id}
POST   /api/kuiper/movimiento
PUT    /api/kuiper/movimiento/{id}
DELETE /api/kuiper/movimiento/{id}

GET    /api/kuiper/categoria
POST   /api/kuiper/categoria
...

GET    /api/kuiper/presupuesto
GET    /api/kuiper/resumen?periodo=       balance del mes, gasto por categoría
```

## Qué se aprovecha de lumen-app

El código no. Sí lo aprendido sobre el dominio: qué campos hacían falta de verdad, qué categorías se usaban, qué vistas se miraban y cuáles sobraban.

Antes de escribir la primera entidad, merece la pena abrir el proyecto viejo y quedarse con esas conclusiones. Es media hora que ahorra rehacer el modelo.

**No hay migración de datos.** El proyecto quedó sin terminar, así que no hay histórico que rescatar. Si hubiera algún dato suelto que interese, se mete a mano.

## Estado

| Entidad | Estado |
|---|---|
| `Categoria` | Pendiente — B5 |
| `Movimiento` | Pendiente — B5 |
| `Presupuesto` | Pendiente — B5 |
