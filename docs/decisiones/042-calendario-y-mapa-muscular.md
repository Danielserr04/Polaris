# 042 — Calendario y mapa muscular: con lo que ya hay, más una agregación por grupo

Estado: aceptada · 2026-10-02

## Contexto

FitCore tenía un calendario de entrenos, un mapa muscular y calculadoras (1RM, discos, IMC). Atlas tiene las sesiones y las series, pero no sabe cuánto se ha trabajado cada grupo muscular en un periodo.

## Decisión

- **Calendario**: solo frontend, pestaña Calendario de Atlas, con `GET /api/atlas/sesion?desde=&hasta=` del mes. Añade la racha de semanas seguidas con al menos una sesión.
- **Mapa muscular**: un endpoint de solo lectura, `GET /api/atlas/trabajo-muscular?desde=&hasta=`, que agrega en la base series, sesiones distintas y volumen por `grupo_muscular` del ejercicio (mismo patrón que [[026-progresion-y-records-por-volumen]]). El grupo sale tal cual está escrito; el frontend lo reparte entre los músculos del dibujo por palabras clave (pecho, espalda, cuádriceps…). Los polígonos del dibujo son los de FitCore (react-body-highlighter, MIT).
- **Calculadoras** de 1RM (media de Epley y Brzycki, hasta 12 reps) y discos por lado: solo frontend, pestaña Calculadoras.
- **IMC** y cintura/altura: frontend, pestaña Cuerpo, con la altura de `GET /api/nucleo/perfil` y el último peso. Núcleo sigue sin interpretar.

## Alternativas descartadas

- **Pedir el detalle de cada sesión del mes para contar series por grupo.** Una petición por sesión; la agregación en la base es una consulta.
- **Convertir `grupo_muscular` en un enum.** Rompe los ejercicios ya escritos a mano y es un cambio de esquema que nadie ha pedido.
- **Calculadoras en el backend.** Son fórmulas sin datos guardados: no aportan nada en el servidor.

## Consecuencias

- Un grupo escrito de forma rara ("tren superior") no se pinta en el mapa, pero sí sale en la lista de grupos debajo.
- La pestaña Cuerpo de Atlas llama a `/api/nucleo/...` directamente, como ya hace Inicio con el peso.

## Actualización (2026-10-02)

Los polígonos de FitCore se sustituyen por un dibujo propio más fiel (`mapaMuscular.data.ts`): 20 músculos con curvas en lugar de polígonos rectos, separando por ejemplo cuádriceps en vasto lateral, recto femoral y vasto medial, el recto abdominal en bloques, isquios, sóleo, tibial, serrato, redondo y glúteo medio. Se dibuja media figura y se refleja. Al pasar el ratón se ve el nombre del músculo y sus series. Ya no hay código de react-body-highlighter.

Revisión anatómica (2026-10-02): el pectoral pasa a abanico hacia el brazo, bajo el deltoides; el dorsal sube hasta la axila; el glúteo mayor baja en diagonal; el sóleo asoma a los lados de los gemelos. Se añaden esternocleidomastoideo, braquial, sartorio y tensor de la fascia lata, y el antebrazo se parte en braquiorradial y flexores. De frente el tríceps asoma por dentro del brazo, no por fuera. Sigue siendo un esquema, no una lámina médica.

Puzle (2026-10-02): las piezas ya no se dibujan sueltas sobre una silueta. Cada borde tiene nombre y lo comparten las dos piezas que separa, así que los músculos encajan sin solaparse ni dejar huecos; las partes sin músculo (cabeza, manos, rodillas, tibia, pies) son piezas neutras. Cada borde se suaviza por su cuenta para que las dos piezas pinten la misma curva. Se añaden los peroneos.

Detalle (2026-10-02): las piezas se parten por cabezas o porciones (pectoral clavicular y esternal, deltoides anterior, lateral y posterior, bíceps y tríceps por cabezas, trapecio superior, medio e inferior, infraespinoso y redondo mayor, isquios en tres, aductores y antebrazo en varias). Siguen siendo el mismo músculo para colorear; la porción solo sale en el texto al pasar el ratón.

Redondeado (2026-10-02): a Daniel le gustaba más el estilo redondeado de la primera versión. Se mantiene el reparto en piezas que no se solapan, pero cada pieza se encoge un poco hacia dentro (un hueco igual entre músculos y con el contorno) y se redondean sus esquinas con Chaikin, que deja la curva dentro de la pieza. Las piezas sin músculo no se pintan: se ve la silueta.
