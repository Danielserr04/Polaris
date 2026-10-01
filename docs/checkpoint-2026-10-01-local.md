# Checkpoint — 2026-10-01 (cierre de nube · qué falta en la máquina local)

Nota temporal para retomar el trabajo. Borrarla cuando esté todo consumido. No es una nota de la vault: es un estado de trabajo. **Sustituye a `checkpoint-2026-10-01`** (rama `docs/checkpoint-2026-10-01`, obsoleta: todo lo que decía está en `main`).

Para retomar: «lee `docs/checkpoint-2026-10-01-local.md` y sigue por la sección *Tareas en la máquina local*». Está en `main`.

## Dónde estamos

- **Todo lo que se podía hacer desde la nube está hecho y en `main`** (PRs #25 a #36). No hay ramas con trabajo pendiente de fusionar ni trabajo a medias.
- **Backend:** cerrado (B0 a B8), 683 tests (649 unitarios + 34 de integración), migraciones `V1` a `V15`.
- **Frontend completo:** shell, login (usuario/contraseña, Google y **registro**), Perfil (con **Apariencia**: tema oscuro/claro/sistema), Inicio, Odisea, Kuiper, Fusión y **Atlas** (Progresión, Ejercicios, Rutinas, sesiones, peso). **Móvil** responsive (≤ 760 px, `frontend/src/styles/mobile.css`). Tipografías en local (`frontend/public/fonts`).
- Decisiones nuevas desde el checkpoint anterior: [[032-listados-de-odisea-y-atlas-sin-consultas-extra]] (N+1 de Odisea resuelto con `@EntityGraph`, `EntradaListDto` con año/duración, `SesionListDto` con ejercicios y volumen) y [[033-registro-en-el-login-y-tema-claro]].
- **Lo que queda no se puede hacer desde la nube**: necesita red hacia servicios externos (el proxy del contenedor da 403/502 a todos), tu navegador/móvil real o decisiones tuyas. Es la sección siguiente.

## Tareas en la máquina local

Orden recomendado. Cuando algo falle, **pégame aquí el mensaje exacto o el log** (y, si es una API externa, el JSON crudo que devuelve): lo arreglo en la nube y lo vuelves a probar. No hace falta que arregles nada tú.

### 0. Arrancar todo en local

```powershell
copy .env.example .env        # rellenar valores (ver abajo); .env no se sube nunca
docker compose up -d          # MySQL
.\mvnw.cmd spring-boot:run    # backend en :8080
cd frontend; npm install; npm run dev   # frontend en :5173, proxy de /api a :8080
```

Mínimo en `.env`: `POLARIS_DB_*`, `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET` (valen los del `.env.example` si aún no tienes los de Google) y `POLARIS_JWT_SECRETO` (≥ 32 caracteres, generado). Prueba rápida: crear cuenta en el login, abrir el enlace de verificación (en perfil `dev` sale en el log del backend: `Verificacion de email para …`), entrar.

### 1. Open Food Facts (Fusión → Alimentos → Importar)

- `curl -A "Polaris/0.1 (app personal)" https://world.openfoodfacts.org/api/v2/product/3017620422003.json` debe devolver JSON con `product`.
- En la app: buscar «nutella» (o cualquier marca), ver resultados con kcal/macros, importar uno y comprobar que aparece en el catálogo con sus macros por 100 g. Probar también un código de barras directo.
- Comprobar: resultados sin kcal (deben salir descartados o con 0 sin romper), búsquedas sin resultados, y que no hay error 5xx por límite de peticiones.
- El flujo de importar ya se probó contra un servidor falso local (`POLARIS_OPENFOODFACTS_URLBASE=http://localhost:9099`, con `/cgi/search.pl` y `/api/v2/product/{codigo}.json`); lo que no se ha probado es el formato real de la respuesta.

### 2. APIs de Odisea (Odisea → Añadir título)

Variables en `.env`: `POLARIS_TMDB_TOKEN` (token de lectura v4 de themoviedb.org), `POLARIS_IGDB_CLIENT_ID` y `POLARIS_IGDB_CLIENT_SECRET` (consola de Twitch Developers). Open Library no necesita clave.

- Buscar «Interstellar» (película), «Breaking Bad» (serie), «Zelda» (juego) y «Dune» (libro); importar uno de cada tipo.
- Comprobar en cada uno: carátula (`tituloImagenUrl`) que carga, año, géneros, duración, y que la entrada queda en Pendientes.
- Si una fuente falla, dime cuál y con qué código (401 = clave; 429 = límite; 5xx = proxy/fuente).

### 3. Login con Google

- Google Cloud Console → Credenciales → ID de cliente OAuth 2.0. **URI de redirección autorizada: `http://localhost:8080/login/oauth2/code/google`** (la del backend, no la del frontend).
- Variables: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `POLARIS_FRONTEND_URL` (por defecto `http://localhost:5173`).
- Probar: «Continuar con Google» → vuelve a `/auth/callback#token=…` y entra; cancelar en la pantalla de Google → `/login?error=cancelado`; y desde Perfil «Conectar con Google» en una cuenta nativa. Ver [[031-login-google-redirige-al-frontend]].

### 4. Correo real (perfil `prod`)

- Variables `POLARIS_SMTP_HOST`, `POLARIS_SMTP_PUERTO` (587), `POLARIS_SMTP_USUARIO`, `POLARIS_SMTP_PASSWORD`, `POLARIS_CORREO_REMITENTE`, y `POLARIS_URL_BASE` (la URL pública del backend: el enlace de verificación apunta a ella).
- Arrancar con `SPRING_PROFILES_ACTIVE=prod`, registrar una cuenta y comprobar que llega el correo y que el enlace verifica. **Ojo:** `spring.profiles.default` es `dev`; sin la variable, los tokens salen por el log (ahora el arranque lo avisa con un `WARN`).

### 5. Móvil real

- `cd frontend; npx vite --host` y abrir `http://<IP-del-PC>:5173` desde el móvil (misma wifi).
- Mirar: barra de módulos de abajo y el indicador del iPhone (zona segura), hojas inferiores (registrar sesión/comida/movimiento), teclado numérico en las series, scroll del login y el tema claro. Las capturas de la nube eran Chromium a 390 y 360 px, no un móvil.

### 6. Despliegue y PWA (sin decidir)

- Decidir hosting. Mínimos: `SPRING_PROFILES_ACTIVE=prod`, `POLARIS_JWT_SECRETO` propio, `POLARIS_URL_BASE` y `POLARIS_FRONTEND_URL` con las URLs reales, HTTPS, copia de seguridad de MySQL, y la URI de redirección de Google de producción añadida en la consola.
- El frontend se construye con `npm run build` (carpeta `frontend/dist`); en producción backend y frontend deben quedar bajo el mismo origen (el cliente usa rutas `/api` relativas).
- PWA después del despliegue.

### 7. Limpieza de ramas remotas (el sistema de permisos me bloquea borrarlas)

Todas están fusionadas en `main`. Desde tu máquina:

```powershell
git push origin --delete chore/menores-revision-y-docs claude/eager-cray-9ps8rv claude/wizardly-hopper-35k6t1 docs/checkpoint-2026-09-30 docs/checkpoint-2026-10-01 feat/atlas-ejercicio feat/atlas-peso feat/atlas-progresion feat/atlas-rutina feat/atlas-sesion feat/auth-usuario feat/b8-indices feat/b8-logs feat/b8-openapi feat/b8-testcontainers feat/frontend-base feat/frontend-fusion-catalogo feat/frontend-fusion-hoy feat/frontend-inicio feat/frontend-kuiper feat/frontend-kuiper-categorias feat/frontend-login feat/frontend-odisea feat/frontend-perfil feat/fusion-alimento feat/fusion-comida feat/fusion-objetivo feat/fusion-openfoodfacts feat/fusion-peso feat/fusion-resumen feat/kuiper-categoria-movimiento feat/kuiper-presupuesto feat/kuiper-resumen feat/nucleo-perfil-peso fix/shared-conflicto-unicidad-y-errores fix/shared-errores-cliente-legibles
```

Se queda `main`. `claude/magical-bohr-an29mz` es la rama que me asigna el sistema en cada sesión: no la borres.

## Decisiones abiertas (tuyas)

- **Kuiper y Fusión siguen en retícula de tarjetas.** El readme del diseño pide llevarlos al sistema de bandas del Inicio. No lo he tocado: es un rediseño visual completo y tu regla es no disrumpir lo que funciona. Si lo quieres, dilo y va por pantalla, una a una.
- **`GET /api/odisea/entrada/estadisticas`** figura en `docs/modulos/odisea.md` pero nunca se definió qué devuelve; el frontend cuenta en cliente. Decidir si se define o se borra de la doc.
- **Errores del backend sin tildes**: el frontend los corrige por status/texto en los sitios que los muestra. Arreglarlos de raíz toca mensajes que los tests fijan.
- **Tests automáticos de frontend**: no hay (la verificación es Chromium a mano). Añadir Vitest/Playwright implicaría dependencias de npm: se pregunta antes.

## Cómo trabaja el usuario (leer antes de seguir)

- Es programador web junior; quiere respuestas **concisas**, ir al grano. **Ya no quiere que le pregunte por todo**: «pista libre»; decidir, dejarlo documentado (ADR) y avisar.
- Quiere saber **cuánto tardo** al generar documentos (html, md, word).
- Trabaja desde un PC de empresa: **todo se hace en la nube**. **No puede ver el sandbox**: la verificación visual la hago yo con capturas (Playwright) y le paso las relevantes con `SendUserFile` (copiándolas antes a la carpeta scratchpad de la sesión).
- **UX: que no disrumpa la UI.** Las funciones nuevas se añaden con las piezas y patrones que ya existen, sin cambiar lo que funciona.
- **No dejar frentes abiertos si se pueden cerrar.**
- Subagentes: los pide él; si no, sin agentes.
- Commits con su nombre y email (`Daniel Serrano`), una línea en español imperativo, con los trailers `Co-Authored-By` y `Claude-Session`. Un commit por unidad con sentido. Rama designada por el sistema (`claude/…`); nombres normales (`feat/…`) solo si él lo pide.
- Flujo: rama → pruebas reales (Chromium contra backend con MySQL) → `npm run build` → docs (`docs/roadmap.md`, `CLAUDE.md`, ADR si hay decisión) → commit/push → PR con descripción completa → merge con merge commit. Tras fusionar, la rama designada se reinicia desde `main` (`git fetch origin main && git checkout -B <rama> origin/main && git push --force-with-lease`); el sistema de permisos puede pedir confirmación expresa del usuario la primera vez.

## Trucos del entorno cloud

- **`./mvnw` no tiene permiso de ejecución**: `sh ./mvnw test`. Con Docker: ~1 min; sin Docker los 34 de integración se saltan solos; `-DexcludedGroups=integracion` los excluye. Los resultados se suman de `target/surefire-reports/*.txt` (con `-q` no hay resumen).
- **Docker**: si `docker info` falla, `dockerd > /tmp/claude-0/dockerd.log 2>&1 &`. Imagen `mirror.gcr.io/library/mysql:8.4` (Docker Hub da 429). Con Docker 29 hace falta `src/test/resources/docker-java.properties` (`api.version=1.44`), ya en el repo.
- **El contenedor se reinicia a menudo** y mata MySQL, backend y Vite: levantar todo con un script (Docker → `docker start` del MySQL → `mvnw spring-boot:run` con `POLARIS_DB_URL=jdbc:mysql://127.0.0.1:33xx/polaris POLARIS_DB_USER=root POLARIS_DB_PASSWORD=test GOOGLE_CLIENT_ID=x GOOGLE_CLIENT_SECRET=x POLARIS_JWT_SECRETO=<64 caracteres> POLARIS_PORT=81xx` con `(setsid sh ./mvnw -q spring-boot:run > log 2>&1 &)` → `POLARIS_API_URL=http://localhost:81xx npx vite --port 52xx --strictPort`). Registrar en `POST /api/auth/registro`, `update usuario set email_verificado=1` con `docker exec`, login en `POST /api/auth/login` (`usernameOEmail`, `password`).
- **Datos de prueba**: se siembran por la API con un script de Python (`urllib`): títulos y entradas de Odisea, categorías/presupuestos/movimientos de Kuiper, objetivo, alimentos y comidas de Fusión, ejercicios/rutina/sesiones de Atlas y pesos. El script vive en el contenedor (se pierde): reescribirlo mirando los `*RequestDto` en `/v3/api-docs`. El catálogo de ejercicios viene vacío: se crean propios.
- **Frontend en el contenedor**: `cd frontend && npm ci && npm run build`. Capturas: Playwright global en `/opt/node22/lib/node_modules/playwright` con `chromium.launch({executablePath:'/opt/pw-browsers/chromium', args:['--no-sandbox']})`; la sesión se inyecta con `localStorage['polaris.sesion'] = {token, expiraEn}` y el tema con `localStorage['polaris.tema']`. Las fuentes ya son locales: no hay `ERR_CERT_AUTHORITY_INVALID` por Google Fonts.
- **Capturas fiables:** esperar ~600 ms tras abrir un diálogo; botones por nombre exacto (`getByRole('button', {name, exact:true})`). En capturas de página completa la barra inferior móvil (`position:fixed`) sale a mitad de página: es de la captura.
- **Procesos: matar siempre por PID.** `pkill -f` con un patrón que aparezca en el propio comando mata la shell. Para encontrar el java por puerto: recorrer `/proc/*/environ` buscando `POLARIS_PORT=81xx`.
- **CSS: el de página se carga ANTES que `kit.css`**, y `styles/mobile.css` el último. Una regla de página con la misma especificidad que una del kit **pierde**: usar dos clases o variables. Los hijos de `ListRow` llevan `display` en línea: ocultarlos exige `!important`.
- **Diálogos de edición con borrado:** el cierre va en el `onSuccess` del propio hook (`useBorrarX(onClose)`), no en el `mutate`. `Dialog` ya hace portal en `.app`, mete el foco, atrapa el Tab y lo devuelve; `useRestaurarFoco` en los formularios sigue ahí y es inofensivo.
- **El backend omite los campos nulos** (`default-property-inclusion: non_null`): un campo opcional llega como `undefined`, no `null`. Comprobar con `!= null` (nunca `!== null`) y tipar como opcional.
- **Respuestas sin cuerpo** (201 del registro): `api()` ya las acepta.
- **`LineChart`** añade un 12 % de margen por debajo del mínimo: el eje puede salir negativo (se muestra 0 con `min={0}` y `format`).
- **Antes de abrir un PR comprueba `git diff origin/main --stat`.**
- **El clasificador de permisos de Bash falla o bloquea a veces** (p. ej. reiniciar la rama sin confirmación expresa): reintentar una vez con la confirmación del usuario; no dar rodeos.
- **GitHub:** sin `gh`; solo las herramientas MCP `mcp__github__*` (cargarlas con ToolSearch).
- El sistema de permisos bloquea borrar ramas remotas: lo hace el usuario.

## Cifras

- Backend: 683 tests, migraciones `V1` a `V15`, 22 controllers / ~82 operaciones en OpenAPI, ADRs 001 a 033.
- Frontend: sin tests automáticos; 29 primitivas del design system; ~50 ficheros de pantalla y api.
- Estimación: backend 100 %; proyecto ~93 % (faltan las pruebas contra servicios reales, el despliegue y el PWA).
