# 041 — MedidaCorporal: perímetros en Núcleo, una medición por día

Estado: aceptada · 2026-10-02

## Contexto

FitCore tenía medidas corporales (cuello, pecho, cintura, cadera, brazos y piernas) además del peso. Polaris solo guarda el peso. Hay que decidir dónde viven, qué campos llevan y cómo se escriben.

## Decisión

- Entidad `MedidaCorporal` en [[nucleo]], tabla `medida_corporal` (`V23`), CRUD en `/api/nucleo/medida-corporal`. Es dato corporal crudo, como el peso: Núcleo lo guarda y no interpreta.
- Ocho perímetros en cm, todos opcionales: `cuelloCm`, `pechoCm`, `cinturaCm`, `caderaCm`, `brazoIzqCm`, `brazoDchoCm`, `musloIzqCm`, `musloDchoCm`, más `notas`. `DECIMAL(4,1)`: hasta 999,9 cm con precisión de milímetro.
- **Al menos una medida** por registro: un registro vacío es un 400 (`ValidationException`).
- Mismo patrón que [[010-registro-peso-un-peso-por-dia]]: una medición por día (unique `(usuario_id, fecha)`), el `POST` sobre un día ya medido lo reemplaza y responde 201, el `PUT` a una fecha ocupada por otra es 409, fecha no futura, listado del más reciente al más antiguo con `?desde=&hasta=`.

## Alternativas descartadas

- **Columnas en `registro_peso`.** Mezcla dos ritmos distintos (el peso es diario, las medidas mensuales) y llenaría la tabla de nulos.
- **Tabla clave-valor (`tipo`, `valor`).** Flexible, pero el formulario y la gráfica se complican y no hay más medidas en el horizonte.
- **En [[atlas]].** Fusión también podría usarlas (cintura para el % de grasa). Si las usan dos módulos, van en Núcleo.

## Consecuencias

- Sin puertos desde Atlas ni Fusión por ahora: se ven y se apuntan en Perfil. Si un módulo las necesita, se añade su puerto como con el peso ([[022-peso-corporal-desde-fusion-y-atlas]]).
- Un `POST` que solo corrige la cintura borra las demás medidas de ese día si no se reenvían. Reemplazo completo, igual que el peso.
