# 039 — Cuentas y transferencias: saldo calculado, cuenta opcional en movimientos

Estado: aceptada · 2026-10-02

## Contexto

Lumen separaba el dinero por cuentas (corriente, ahorro, tarjeta, efectivo) y permitía mover dinero entre ellas. En [[kuiper]] un `Movimiento` solo tenía categoría: no se sabía dónde estaba el dinero ni cuánto había en total.

## Decisión

- Nueva entidad **`Cuenta`** (tabla `cuenta`, V22): `nombre` (**único por usuario**, 409 si se repite), `tipo` (`CORRIENTE`, `AHORRO`, `TARJETA`, `EFECTIVO`), `saldo_inicial` `DECIMAL(12,2)` **con signo** (una tarjeta puede empezar en negativo), `color`, `icono`, `banco` (opcional) y `archivada`.
- **El saldo actual no se guarda.** `CuentaService` lo calcula en cada lectura:
  `saldoInicial + ingresos − gastos (de sus movimientos) + transferencias entrantes − salientes`.
  Son **cuatro `SUM … GROUP BY cuenta`** (dos sobre `movimiento` por tipo, dos sobre `transferencia`) que sirven para todas las cuentas del usuario a la vez: el listado cuesta lo mismo con 2 cuentas que con 20, y editar o borrar un movimiento nunca deja un saldo desincronizado. Los puertos devuelven `Map<cuentaId, total>`; la aritmética vive en el dominio.
- **`movimiento.cuenta_id` y `recurrente.cuenta_id` son opcionales** (FK nullable). Las filas que ya existían se quedan a `NULL` = "sin cuenta" y no cuentan en ningún saldo. Si viene, la cuenta tiene que ser del usuario (**404** si no). Los movimientos que genera un recurrente **heredan su cuenta**. `GET /api/kuiper/movimiento?cuentaId=` filtra por cuenta; los DTOs de movimiento y recurrente traen `cuentaId` y `cuentaNombre`.
- Nueva entidad **`Transferencia`** (tabla `transferencia`, V22): cuenta de origen y de destino (**ambas del usuario**, 404 si no; **distintas**, 400 si no, con un `CHECK` como red), `importe > 0` `DECIMAL(10,2)`, `fecha` no futura y `concepto`. CRUD en `/api/kuiper/transferencia` con filtros `?desde=&hasta=&cuentaId=` (la cuenta casa como origen o como destino), de la más reciente a la más antigua.
- **Una transferencia no es ingreso ni gasto**: vive en su propia tabla y el resumen mensual ([[014-resumen-mensual-agregado-en-servicio]]) no la ve.
- **Una cuenta con movimientos, recurrentes o transferencias no se borra** (400): se **archiva** (`archivada = true` por `PUT`). La FK sin `ON DELETE` lo garantiza también en la base. Archivar solo la oculta de los selectores del frontend; el backend sigue aceptándola y conserva su histórico y su saldo.
- `GET /api/kuiper/cuenta` devuelve las cuentas con `saldoActual`, activas primero y por nombre (filtros `?archivada=&tipo=`). **`GET /api/kuiper/cuenta/patrimonio`** devuelve el total: la suma del saldo actual de **todas** las cuentas, archivadas incluidas (archivar no hace desaparecer el dinero).

## Alternativas descartadas

- **Guardar el saldo en la cuenta y actualizarlo en cada movimiento.** Más rápido de leer, pero cada alta, edición, borrado o cambio de cuenta de un movimiento o transferencia tendría que tocar uno o dos saldos en la misma transacción; un fallo deja el saldo mal para siempre.
- **Una consulta de saldo por cuenta.** N × 4 consultas en el listado.
- **Representar la transferencia como dos movimientos** (gasto en origen, ingreso en destino). Inflaría ingresos y gastos del resumen y obligaría a una categoría ficticia.
- **`cuenta_id` obligatorio.** Rompería los movimientos existentes y obligaría a crear una cuenta antes de apuntar nada.
- **Borrado en cascada** de movimientos o transferencias al borrar la cuenta. Pierde datos sin avisar.

## Consecuencias

- Leer una cuenta o el listado son 1 + 4 consultas; el patrimonio, las mismas. Si algún día pesa, el índice `idx_movimiento_cuenta` y los de `transferencia` son el punto de partida.
- Un movimiento sin cuenta suma en el resumen pero en ningún saldo: el patrimonio solo refleja lo apuntado en cuentas.
- Cambiar los valores del enum de tipo de cuenta exige una migración nueva, como en V2.
- La migración es `V22`, no `V20`: se fusionó después de `V21` (notificaciones) y Flyway rechazaría una versión menor que la última aplicada. `V20` queda sin usar.
