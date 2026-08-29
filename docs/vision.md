# Visión

Polaris es una aplicación web personal que reúne en un solo sitio lo que hoy está repartido en apps sueltas: ocio, gastos, nutrición y gym.

No es un producto. Es una herramienta de uso propio, y las decisiones se toman con ese criterio: lo que sea más útil para una persona gana a lo que sea más vendible.

## Por qué existe

Hubo dos intentos previos y separados: `lumen-app` para gastos y `fitcore` para nutrición y gym. **Ninguno llegó a completarse.** Polaris los sustituye: mismos dominios, una sola aplicación, y esta vez con una arquitectura pensada desde el principio para aguantar cuatro módulos.

**Todo el código es nuevo.** De los proyectos viejos se aprovecha lo aprendido sobre el dominio — qué campos hacían falta, qué pantallas se usaban de verdad — no el código.

El objetivo secundario, y no menor: aprender. Arquitectura hexagonal aplicada de cero, React, y despliegue real en un servidor.

## Qué tiene que cumplir

- **Modular.** Cada módulo se añade, se toca o se borra sin romper los demás.
- **Ampliable.** Añadir un tipo de contenido o un módulo nuevo no debe requerir migraciones ni rediseños.
- **Móvil y escritorio.** PWA instalable. Si algún día hace falta Play Store, se envuelve sin reescribir.
- **Rápida y agradable de usar.** Es una app que se abre varias veces al día.

## Módulos

| Nombre | Qué hace |
|---|---|
| [[odisea]] | Pelis, series, juegos y libros: pendientes, en curso, terminados, valoraciones |
| [[kuiper]] | Ingresos, gastos, categorías, presupuestos |
| [[fusion]] | Alimentos, comidas, macros, objetivos |
| [[atlas]] | Ejercicios, rutinas, sesiones, progresión |
| [[nucleo]] | Perfil y peso corporal, compartido por Fusión y Atlas |

Los nombres son espaciales por decisión explícita: ver [[004-nombres-espaciales]].

## Fuera de alcance por ahora

- **Calendario.** Era el quinto módulo y depende de todos los demás. Se retoma cuando el resto funcione.
- **Multiusuario.** El modelo lleva `usuario_id` desde el día 1 para poder abrirlo, pero no se construye para varios usuarios todavía.
- **Temporadas y episodios de series.** Ver [[odisea]].
