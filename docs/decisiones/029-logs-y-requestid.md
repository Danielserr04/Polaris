# 029 — Revisión de logs (B8) y `requestId` por petición sin dependencias nuevas

Estado: aceptada · 2026-10-01

## Contexto

B8 incluye "logs". Hasta ahora había 10 llamadas de logger repartidas en 7 clases (todos con SLF4J vía `@Slf4j`, Lombok ya estaba), sin revisar qué datos escriben ni con qué nivel, y ninguna forma de unir las líneas de una misma petición: con varias peticiones simultáneas (el caso de [[020-violacion-unicidad-409-y-errores-http-cliente]]) el log es una mezcla sin hilo conductor. Alcance fijado con el usuario: revisar y corregir con cambios mínimos, y añadir un identificador de petición **sin dependencias nuevas** (`pom.xml` intacto).

## Decisión

**1. Auditoría y correcciones.** Se revisó cada llamada de logger de `src/main/java` y la configuración de `application.yml`. Lo corregido:

| Qué | Dónde | Qué se hizo |
|---|---|---|
| El email del usuario en un `INFO` (dato personal en cada login con Google) | `OAuth2LoginSuccessHandler` | Se loguea solo el id |
| El mensaje de MySQL de una clave duplicada trae el **valor** (`Duplicate entry 'x@y.com' for key ...`) y Hibernate lo escribe a `ERROR` en `SqlExceptionHelper` por cada 409, que es un caso normal. Comprobado con 6 registros simultáneos | `application.yml` | `logging.level.org.hibernate.engine.jdbc.spi.SqlExceptionHelper: "OFF"`. No se pierde información: el 409 lo registra `GlobalExceptionHandler` en `WARN` sin el valor, y en un 500 la excepción completa (con la causa de MySQL) la escribe `GlobalExceptionHandler` |
| Un fallo de SMTP se logueaba con la excepción entera; el mensaje de un rechazo SMTP suele llevar la dirección del destinatario, pese al comentario "el correo no se escribe" | `SmtpEnviarVerificacionAdapter` | Se loguea el tipo de la causa raíz, sin mensaje ni stacktrace. Se mantiene `ERROR`: es un fallo real que se traga para no dejar la cuenta a medias |
| Cancelar el login en Google (`access_denied`) o un `state` caducado se logueaba a `ERROR` con stacktrace | `OAuth2LoginFailureHandler` | Esos códigos (`access_denied`, `invalid_request`, `authorization_request_not_found`, `invalid_state_parameter`) pasan a `WARN` sin stacktrace. El resto (`invalid_client`, `redirect_uri_mismatch`, `invalid_token_response`...) sigue en `ERROR` con stacktrace |
| Los errores 4xx no dejaban ninguna línea, así que el `requestId` de una respuesta 400/404/409 no llevaba a nada en el log | `GlobalExceptionHandler.build` | Una línea `DEBUG` ("Respuesta 400 en POST /ruta: mensaje") solo para 4xx. Sin query string, sin cuerpo, sin stacktrace. En prod (nivel `INFO`) no sale |

Lo revisado y **sin cambios**, con su razón:

| Qué | Dónde | Veredicto |
|---|---|---|
| Enlace de verificación con **token completo y email** en `INFO` | `LogEnviarVerificacionAdapter` | Se queda: es el adaptador de desarrollo, la única forma de probar el registro sin SMTP, y es `@Profile("dev")` (el de prod es `SmtpEnviarVerificacionAdapter`, que no loguea ni token ni correo). Se documenta en su javadoc y un test fija que el perfil sea solo `dev`. **Riesgo conocido:** `spring.profiles.default` es `dev`, así que una instalación que arranque sin `SPRING_PROFILES_ACTIVE=prod` cae en este adaptador y escribe tokens en el log: el despliegue de prod tiene que activar el perfil (queda apuntado como pendiente) |
| Claves de las APIs externas (TMDB, IGDB, Twitch, Open Food Facts) | adaptadores de `externo/` | No se loguean. TMDB e IGDB mandan la credencial en cabecera. La excepción es el `client_secret` de Twitch, que va en la **query** de la petición de token: un test comprueba que ningún mensaje de la cadena de causas de la excepción (401 y conexión rechazada de verdad) lo contiene, porque `RestClient` recorta la query al construir el mensaje de `ResourceAccessException` |
| `ExternalServiceException` a `ERROR` con stacktrace | `GlobalExceptionHandler` | Correcto: es un 502, un fallo ajeno pero inesperado. El mensaje al cliente es genérico |
| 500 a `ERROR` con stacktrace; 409 a `WARN` sin stacktrace; 4xx sin stacktrace | `GlobalExceptionHandler` | Correcto. Se loguea `getRequestURI()`, nunca la query string (puede traer texto de búsqueda) ni el cuerpo |
| `JWT rechazado: <mensaje de Nimbus>` en `DEBUG` | `JwtService` | El mensaje describe el fallo de formato, no incluye el token. Un test lo fija |
| `WARN` con `ex.getMessage()` si MySQL no responde al health | `HealthController` | Correcto: recuperable, una línea por sondeo, sin bucle |
| Cuerpos de petición, contraseñas, hashes | todo `src/main/java` | No se loguean en ningún sitio |
| Logs dentro de bucles | todo `src/main/java` | No hay ninguno |
| Mensajes en español sin acentos | todo | Ya cumplen |

**2. `requestId`.** Un `RequestIdFilter` (`shared/web`), `OncePerRequestFilter` registrado como `@Component` con `@Order(Ordered.HIGHEST_PRECEDENCE)`:

- Lee `X-Request-Id`; si cumple `[A-Za-z0-9_-]{1,64}` lo respeta (permite seguir una petición desde el front), y si no (ausente, vacío, largo, con espacios, saltos de línea, `${jndi:...}`, etc.) genera un UUID. Un valor del cliente nunca llega al log sin pasar por esa validación: sin ella un `\r\n` falsificaría líneas de log.
- Lo pone en el MDC (`requestId`), lo devuelve en la cabecera de respuesta `X-Request-Id` y lo quita del MDC en `finally`. También corre en el redespacho de error (`/error`) reutilizando el mismo id.
- **Orden real.** Los filtros de servlet de Spring Boot van ordenados y la cadena de Spring Security es uno más (`DelegatingFilterProxyRegistrationBean`, orden `-100`). Con `HIGHEST_PRECEDENCE` el filtro corre antes, así que **también llevan id los 401/403 que genera Security sin llegar a un controller**. Se comprobó con la app real (401 sin token, 401 con JWT basura) y lo fija un test de integración con el contexto completo. No hizo falta tocar `SecurityConfig`: Security no filtra cabeceras de respuesta, y `JwtAuthenticationFilter` sigue siendo un filtro suyo y sin `@Component`, como estaba.
- **Patrón de log.** `logging.pattern.correlation: "[%X{requestId:--}] "` en `application.yml` (común a todos los perfiles). Es el punto de extensión que Spring Boot 3.2+ ofrece para esto: inserta el fragmento entre el hilo y el logger, en consola **y** en fichero, y deja intacto el resto del formato por defecto (no hay que copiar el patrón entero ni mantenerlo). Fuera de una petición sale `[-]`. Ejemplo real:

```
2026-10-01T08:51:21.947+02:00 ERROR 7784 --- [polaris] [nio-8101-exec-2] [boom-500] c.p.shared.error.GlobalExceptionHandler  : Error no controlado en GET /api/kuiper/categoria
```

- **API.** El `requestId` **no** va en el cuerpo de `ErrorResponse`: el contrato no cambia. Solo cabecera. `OpenApiConfig` (en `shared/config`) la declara en todas las respuestas del documento OpenAPI.

## Alternativas descartadas

**Logs JSON estructurados (`logstash-logback-encoder`).** Es dependencia nueva, y hoy no hay nada que consuma JSON: un solo usuario, un solo proceso, logs leídos por una persona. Spring Boot 3.4+ tiene formato JSON propio (`logging.structured.format.console`) sin dependencia, pero no se activa: lo que hace falta ahora es poder buscar por id, y eso ya lo da el patrón de texto. Se reabre si algún día hay un agregador de logs.

**Spring Cloud Sleuth / Micrometer Tracing (trazas distribuidas).** Dependencia pesada (más un bridge y un exportador) para un monolito sin llamadas entre servicios propios. El `requestId` propio cubre el único caso de uso, unir las líneas de una petición. Micrometer Tracing, además, rellenaría `traceId`/`spanId` en la misma clave `logging.pattern.correlation`: migrar más adelante es cambiar ese valor y quitar el filtro.

**Poner `requestId` en `ErrorResponse`.** Facilita al usuario citar el error, pero cambia el contrato de la API y el cliente ya lo recibe en la cabecera.

**Mutear solo el valor de `Duplicate entry` en vez de apagar `SqlExceptionHelper`.** Exigiría un filtro o un converter de Logback propio. Apagar ese logger no pierde nada que no esté ya en `GlobalExceptionHandler` y es una línea de configuración.

**Loguear siempre la excepción completa en `SmtpEnviarVerificacionAdapter`.** Más datos para diagnosticar, pero el mensaje de SMTP lleva la dirección del destinatario. El tipo de la causa raíz basta para distinguir autenticación, conexión y destinatario.

## Consecuencias

- Con el id de la cabecera de una respuesta (o el que manda el front) se encuentran en el log todas las líneas de esa petición. El front debería leer `X-Request-Id` y mostrarlo en los errores.
- `spring.jpa.show-sql: true` (perfil `dev`) imprime por la salida estándar, fuera de Logback, así que esas líneas no llevan `requestId`. Es solo desarrollo: en prod está a `false`. Para SQL con id, la alternativa es `logging.level.org.hibernate.SQL: DEBUG`; no se cambia por no refactorizar lo que no se ha pedido.
- Las trazas que escribe el servidor antes de entrar en la cadena de filtros (p. ej. la inicialización perezosa del `DispatcherServlet` en la primera petición) salen con `[-]`.
- Las líneas de hilos propios (tareas programadas, si algún día las hay) salen con `[-]`: tendrían que poner el MDC ellas mismas.
- Las líneas `DEBUG` de los 4xx solo existen en dev. En prod un 4xx sigue sin dejar rastro en el log (no hay access log); si hace falta, será una decisión aparte.
- Pendiente fuera de B8: una petición con `Accept: application/xml` a una ruta inexistente provoca un fallo dentro de `GlobalExceptionHandler` (no puede serializar `ErrorResponse` como XML) y Spring escribe un `WARN` con stacktrace por su cuenta. No es un dato sensible y no se ha tocado, queda como incidencia.
- Pendiente de despliegue: activar el perfil `prod` explícitamente (ver el riesgo de `LogEnviarVerificacionAdapter`).
