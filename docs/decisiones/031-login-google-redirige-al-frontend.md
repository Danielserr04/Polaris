# 031 — El login con Google termina redirigiendo al frontend

Estado: aceptada · 2026-10-01

## Contexto

Mientras no había frontend, el login con Google devolvía el JWT como JSON en el navegador (y el token se pegaba a mano en Swagger). Ahora hay una SPA en `frontend/` ([[030-frontend-react-vite-y-design-system]]) y el navegador tiene que volver a ella con la sesión iniciada. El flujo de OAuth2 con Google es una cadena de redirecciones del navegador, así que no se puede resolver con una llamada `fetch` de la SPA.

## Decisión

- `OAuth2LoginSuccessHandler` redirige a `{polaris.frontend-url}/auth/callback#token=<jwt>&expiraEnSegundos=<n>`. El token va en el **fragmento**, no en la query.
- `OAuth2LoginFailureHandler` redirige a `{polaris.frontend-url}/login?error=cancelado` (el usuario cancela en Google) o `?error=google` (cualquier otro fallo). El código de Google sigue yendo solo al log.
- `polaris.frontend-url` viene de la variable de entorno `POLARIS_FRONTEND_URL` (por defecto `http://localhost:5173`, sin barra final).
- El login nativo no cambia: `POST /api/auth/login` devuelve `TokenDto` en el cuerpo.
- La SPA lee el fragmento en `/auth/callback`, guarda el token y lo borra de la URL de inmediato.

## Alternativas descartadas

**Token en la query (`?token=`).** Acabaría en logs de acceso del servidor/proxy y en el `Referer`. El fragmento nunca sale del navegador.

**Cookie `HttpOnly` con el JWT.** Más resistente a XSS, pero obliga a cambiar la autenticación de la API (cookie + protección CSRF, que hoy está desactivada porque no hay cookies de sesión propias) y a resolver dominios/`SameSite` entre frontend y backend. No compensa para una app personal; se reconsidera si se expone a internet con más usuarios.

**Código de un solo uso intercambiable por `fetch`.** El patrón más limpio, pero exige un endpoint y almacenamiento nuevos para algo que aquí lo resuelve el fragmento.

**Seguir devolviendo JSON y que el frontend abra una ventana emergente.** Más piezas móviles (`postMessage`, bloqueo de popups) sin ganancia.

## Consecuencias

- Con la SPA y el backend en orígenes distintos en desarrollo (`:5173` y `:8080`), el botón de Google apunta directamente al backend (`/oauth2/authorization/google`); la URI de redirección registrada en Google sigue siendo la del backend.
- El token guardado en `localStorage` es accesible a cualquier script de la página: hay que mantener limpio el frontend (sin HTML inyectado ni scripts de terceros). La expiración (12 h) acota el daño.
- Si cambia el origen del frontend, hay que actualizar `POLARIS_FRONTEND_URL`; un valor mal puesto manda el token a otro sitio, por eso viene de configuración y no del cliente.
- Pendiente aparte: el enlace de verificación de email sigue apuntando al backend (`/api/auth/verificacion`); cuando haya pantalla de verificación en el frontend se decide si el enlace pasa a apuntar allí.
