# 046 — Calcular el objetivo nutricional a partir del perfil, sin guardarlo

Estado: aceptada · 2026-10-02

## Contexto

`fitcore` proponía kcal y macros con Mifflin-St Jeor a partir del perfil. En Polaris el perfil (altura, fecha de nacimiento, sexo, actividad) y el peso viven en [[nucleo]], que **no calcula nada**: lo interpreta cada módulo. El objetivo de Fusión es histórico e inmutable ([[016-objetivo-nutricional-historico-inmutable]]).

## Decisión

- `GET /api/fusion/objetivo/calculo?tipo=DEFINICION|MANTENIMIENTO|VOLUMEN[&nivelActividad=][&pesoKg=]` **propone y no guarda**. Para guardarlo, el cliente hace el `POST /api/fusion/objetivo` de siempre (puede retocar los números antes).
- Fórmula: basal = 10·peso + 6,25·altura − 5·edad + 5 (hombre) / −161 (mujer); gasto total = basal × factor de actividad (1,2 · 1,375 · 1,55 · 1,725 · 1,9); kcal = gasto + ajuste (−500 definición, 0, +300 volumen), entre 500 y 10000. Proteínas 2 g/kg, grasas el 25 % de las kcal, carbohidratos el resto. Los mismos números que fitcore.
- Datos: el perfil a través de un puerto propio `DatosCorporalesPort` y el **último peso hasta hoy** con el `PesoCorporalPort` que ya existía ([[022-peso-corporal-desde-fusion-y-atlas]]). El adaptador de `infrastructure/nucleo` llama a `GetPerfilInterface`; Fusión tiene sus propios enums `Sexo` y `NivelActividad`.
- **Sin datos no se inventa nada:** si falta altura, fecha de nacimiento, sexo, actividad o peso, 400 con la lista de lo que falta (fitcore suponía 30 años). `nivelActividad` y `pesoKg` de la petición sustituyen a los guardados.
- La respuesta incluye de dónde sale (edad, peso y su fecha, basal, gasto total) para que la pantalla lo explique.

## Alternativas descartadas

- **Calcular en Núcleo.** Contradice [[nucleo]]: no interpreta. Atlas no lo necesita igual.
- **Guardar el objetivo directamente.** Quita al usuario la revisión y mete filas en un histórico que no se puede editar.
- **Valores por defecto** (30 años, actividad ligera). Darían un objetivo falso sin avisar.

## Consecuencias

- Cambiar la fórmula o los porcentajes no exige migración.
- El "hoy" de la edad y del último peso es el del servidor.

> Antes era la 043, que chocaba con [[043-logros-calculados-y-metas]].
