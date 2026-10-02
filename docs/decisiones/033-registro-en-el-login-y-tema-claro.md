# 033 — Registro dentro de la tarjeta del login y tema claro opcional

Estado: aceptada · 2026-10-01

## Contexto

Dos pendientes del frontend que el diseño no resuelve: no hay pantalla de registro (las cuentas nativas se creaban por Swagger o entrando con Google) y del tema claro solo existían los tokens (`[data-theme="light"]` en `colors.css`), sin selector ni acentos por módulo pensados para fondo claro.

## Decisión

- **Registro:** un modo «Crear cuenta» dentro de la misma tarjeta del login, sin ruta nueva. Usuario (3 a 50), email y contraseña (8 a 100), validados en cliente con las mismas reglas que `RegistroRequestDto`. Tras el 201 la tarjeta pasa a «Revisa tu correo» (el backend manda el enlace de verificación, [[031-login-google-redirige-al-frontend]]) y un botón lleva a entrar. Un 409 se muestra como «ya registrados». No hay contraseña repetida: el email se verifica de todos modos y la contraseña se puede cambiar en Perfil.
- **Tema claro:** opcional, **oscuro sigue siendo el de la marca y el único del login**. Se elige en Perfil (sección «Apariencia»: Oscuro, Claro, Sistema), se guarda en `localStorage` (`polaris.tema`, es del navegador, no de la cuenta) y `AppShell` lo pone como `data-theme` en `<html>` mientras haya shell; en el login nunca está. Cada módulo tiene su acento oscurecido para el claro (`[data-theme="light"] [data-module=…]` y `--mod-*`), sin cielo estrellado ni velo. La carátula de Odisea (cartel oscuro) mantiene su texto claro.
- `api()` acepta respuestas sin cuerpo que no sean 204 (el registro responde 201 vacío).

## Alternativas descartadas

- **Pantalla `/registro` aparte.** Duplica el cromo del login (estrellas, cabecera) y obliga a una ruta más para una pantalla que se usa una vez.
- **Registro con contraseña repetida.** Más fricción para un campo que se puede cambiar después; la verificación por email ya filtra errores.
- **Tema claro por defecto o por la preferencia del sistema.** La marca es nocturna; lo claro es una opción, no un cambio de identidad. «Sistema» existe para quien lo quiera.
- **Guardar el tema en la cuenta (backend).** Es una preferencia del dispositivo (pantalla, luz); no justifica una columna ni un endpoint.
- **Cambiar el tema también en el login.** Las estelas y el fondo de la tarjeta de bienvenida están pensados para oscuro.

## Consecuencias

- ~~Un destello oscuro al abrir en claro~~ (resuelto 2026-10-02): un script en `index.html` pone `data-theme` antes del primer pintado con la misma regla que `lib/tema.ts`, salvo en `/login` y `/auth/`; el login además fuerza oscuro al montarse (`useTemaOscuro`) por si llega redirigido desde una ruta del shell.
- En claro también se oscurecen los semánticos (`--success`, `--danger`…) y los estados de Odisea, y las sombras salen de `--sombra-bloque` / `--sombra-difusa`.
- El claro hay que revisarlo en cada pantalla nueva (colores fijos en oscuro rompen contraste). Los acentos claros salen de una tabla en `colors.css`.
- Sigue sin haber tests automáticos de frontend; ambos se verificaron con Chromium (registro de punta a punta contra backend real, y las pantallas en claro y oscuro a escritorio y móvil).
