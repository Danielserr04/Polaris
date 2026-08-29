# auth

Identidad. Quién eres y cómo lo demuestras en cada petición.

No es un módulo de negocio: no tiene CRUD ni pantallas. Existe para que el resto
(`nucleo`, `odisea`, `kuiper`, `fusion`, `atlas`) pueda filtrar sus datos por
`usuario_id` sin preguntarse de dónde sale ese número.

## Cómo funciona el login

```
Navegador → /oauth2/authorization/google
              ↓  Google verifica quién eres
            OAuth2LoginSuccessHandler
              ↓  sub, email, name, picture → PerfilGoogle
            UsuarioService.getOrCreate()      [alta implícita la 1ª vez]
              ↓
            JwtService.generar(usuarioId)
              ↓
            { "token": "...", "tipo": "Bearer", "expiraEnSegundos": 43200 }
```

A partir de ahí, cada petición lleva `Authorization: Bearer <token>`.
`JwtAuthenticationFilter` lo valida y deja el `usuarioId` como principal;
`UsuarioActual.id()` lo lee desde cualquier servicio.

**El token de Google se usa una sola vez, en el login.** Después manda el JWT
propio. Por eso no se guarda ni el access token ni el refresh token de Google.

## Decisiones

**`googleId` es la identidad, no el email.** Google permite cambiar la dirección
de una cuenta sin que cambie el `sub`. Si la identidad fuera el email, un cambio
en la cuenta de Google crearía un usuario nuevo y los datos anteriores quedarían
huérfanos.

**No hay registro manual.** El alta ocurre en el primer login. Tampoco hay
`update` ni `delete` de `Usuario` por HTTP: no tendría a quién servir.

**Nombre y avatar se refrescan en cada login**, porque pueden haber cambiado en
la cuenta de Google. La fecha de alta no se toca.

**`UsuarioActual` vive en `shared/security/`, no aquí.** Lo van a consumir todos
los módulos, y ninguno puede importar una clase de `auth`. Devuelve un `Long`,
así que `shared/` sigue sin conocer a ningún módulo.

**Nimbus para firmar el JWT**, que ya entra con `spring-boot-starter-oauth2-client`.
Ninguna dependencia nueva. Se descartó jjwt: tres artefactos más para hacer lo
mismo.

**Sesión sólo para el baile de OAuth2.** `SessionCreationPolicy.IF_REQUIRED`
porque el parámetro `state` de OAuth2 la necesita. La API en sí es stateless.

**401 en vez de redirect al login.** Un cliente que llama sin token quiere un
código de error, no el HTML de Google.

## Estructura

Plantilla adaptada, no los 15 ficheros: `Usuario` no tiene casos de uso de
listado ni de borrado, y crear interfaces vacías para cumplir el molde sería
ruido. Se mantienen las tres capas y `domain/` sin Spring ni JPA.

```
auth/
├── application/
│   ├── in/   GetUsuarioInterface · GetOrCreateUsuarioInterface
│   └── out/  UsuarioRepositoryPort
├── domain/
│   ├── model/    Usuario · PerfilGoogle · UsuarioNotFoundException
│   └── service/  UsuarioService
└── infrastructure/
    ├── persistence/  UsuarioEntity · UsuarioRepository · UsuarioJpaAdapter
    │                 AuthController · dto/out · mapper/
    └── security/     SecurityConfig · JwtService
                      JwtAuthenticationFilter · OAuth2LoginSuccessHandler
```

`GetOrCreate` no está en la lista de verbos de [[convenciones]]. Es la
composición de dos que sí están, y describe exactamente lo que hace el caso de
uso: buscar por `googleId` y, si no existe, crear.

## Endpoints

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/oauth2/authorization/google` | Arranca el login. Lo sirve Spring Security |
| GET | `/api/auth/usuario` | **Protegido.** Devuelve tu usuario |

## Configuración

En `.env`, ver `.env.example`:

- `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET` — de Google Cloud Console
- `POLARIS_JWT_SECRETO` — mínimo 32 caracteres, o la aplicación no arranca
- `POLARIS_JWT_EXPIRACION` — segundos, por defecto 43200 (12 h)

URI de redirección autorizada en Google:
`http://localhost:8080/login/oauth2/code/google`

Scopes `openid`, `email` y `profile`: no son sensibles, así que la app no pasa
por el proceso de verificación de Google. **Pásala a "In production"** en la
consola: en estado "Testing" los refresh tokens caducan a los 7 días.

## Pendiente

- Cuando exista React (tras B3), el handler deja de devolver JSON y pasa a
  redirigir al frontend con el token.
- Refresh token propio. Hoy caduca a las 12 h y toca volver a pasar por Google.
