# auth

Identidad. Quién eres y cómo lo demuestras en cada petición.

No es un módulo de negocio: no tiene CRUD ni pantallas. Existe para que el resto
(`nucleo`, `odisea`, `kuiper`, `fusion`, `atlas`) pueda filtrar sus datos por
`usuario_id` sin preguntarse de dónde sale ese número.

Hay dos formas de entrar: **login nativo** (username + contraseña, con
verificación de email) y **login con Google**. Una cuenta puede tener una de
las dos, o ambas si se vinculan por email.

## Cómo funciona el login nativo

```
POST /api/auth/registro  {username, email, password}
        ↓
UsuarioService.registrar()   [BCrypt hashea; emailVerificado = false]
        ↓
JwtService.generarVerificacion(usuarioId)   [JWT de 24h, claim proposito=verificacion]
        ↓
EnviarVerificacionPort.enviar()   [en dev: log · en prod: SMTP, aún sin escribir]

GET /api/auth/verificacion?token=...
        ↓
JwtService.validarTokenVerificacion()  →  VerificarEmailInterface.verificar()

POST /api/auth/login  {usernameOEmail, password}
        ↓
UsuarioService.login()   [rechaza si no coincide o si no está verificado]
        ↓
JwtService.generar(usuarioId)   [JWT de sesión, claim proposito=sesion]
```

## Cómo funciona el login con Google

```
Navegador → /oauth2/authorization/google
              ↓  Google verifica quién eres
            OAuth2LoginSuccessHandler
              ↓  sub, email, name, picture → PerfilGoogle
            UsuarioService.getOrCreate()
              ↓  ¿existe por googleId?      → refresca nombre/avatar
              ↓  si no, ¿existe por email?  → VINCULA la cuenta nativa
              ↓  si no,                     → crea una cuenta solo-Google
            JwtService.generar(usuarioId)
              ↓
            { "token": "...", "tipo": "Bearer", "expiraEnSegundos": 43200 }
```

A partir de cualquiera de los dos logins, cada petición lleva
`Authorization: Bearer <token>`. `JwtAuthenticationFilter` lo valida y deja el
`usuarioId` como principal; `UsuarioActual.id()` lo lee desde cualquier
servicio.

**El token de Google se usa una sola vez, en el login.** Después manda el JWT
propio. Por eso no se guarda ni el access token ni el refresh token de Google.

## Decisiones

**Dos propósitos de JWT bajo la misma firma.** El claim `proposito`
(`sesion` o `verificacion`) los separa: un enlace de verificación filtrado no
sirve para autenticar una sesión, y un token de sesión no sirve para verificar
un email. Reutilizar `JwtService` para el token de verificación evita una
tabla nueva y una dependencia nueva.

**Vinculación de cuentas por email.** Si te registras a mano con
`x@gmail.com` y luego entras con Google usando ese mismo correo, se rellena
el `googleId` en el usuario existente en vez de crear un duplicado. Al
vincular, el email queda verificado: Google ya lo ha comprobado, así que es
prueba más fuerte que nuestra propia verificación. El `passwordHash` no se
toca — el login nativo sigue funcionando después de vincular.

**Login por username o email, indistinto y sin distinguir mayúsculas.**
`username` y `email` se normalizan a minúsculas al guardar y al comparar.

**Mismo mensaje de error tanto si el usuario no existe, si su contraseña
falla, o si es una cuenta solo-Google sin contraseña.** Distinguirlos le
diría a cualquiera qué correos están dados de alta.

**`UnauthorizedException` (401) y `ForbiddenException` (403) viven en
`shared/error/`**, no en `auth/`, con el mismo patrón que `NotFoundException`:
`shared/` no puede conocer una clase de `auth/`, así que las excepciones
concretas (`CredencialesInvalidasException`, `EmailNoVerificadoException`) se
quedan en el dominio de `auth` extendiendo la base compartida.

**`PasswordHasherPort` en vez de inyectar `PasswordEncoder` de Spring
Security en el dominio.** La regla 1 de `CLAUDE.md` prohíbe Spring en
`domain/`; `BCryptPasswordHasherAdapter` en `infrastructure/security/` es el
único sitio que sabe que existe BCrypt.

**`googleId` es la identidad estable de Google, no el email.** Google permite
cambiar la dirección de una cuenta sin que cambie el `sub`.

**Nombre y avatar se refrescan en cada login de Google**, porque pueden haber
cambiado en la cuenta de Google. La fecha de alta no se toca.

**`UsuarioActual` vive en `shared/security/`, no aquí.** Lo van a consumir
todos los módulos, y ninguno puede importar una clase de `auth`. Devuelve un
`Long`, así que `shared/` sigue sin conocer a ningún módulo.

**Nimbus para firmar el JWT**, que ya entra con `spring-boot-starter-oauth2-client`.
Ninguna dependencia nueva. Se descartó jjwt: tres artefactos más para hacer lo
mismo.

**Sesión sólo para el baile de OAuth2.** `SessionCreationPolicy.IF_REQUIRED`
porque el parámetro `state` de OAuth2 la necesita. La API en sí es stateless.

**401 en vez de redirect al login de Google.** Un cliente que llama sin
token quiere un código de error, no el HTML de Google.

**Las rutas de acción se salen del patrón `/api/<modulo>/<entidad>`.**
`/api/auth/registro`, `/api/auth/login`, `/api/auth/verificacion` son verbos,
no sustantivos. No hay forma honesta de forzarlos al molde CRUD.

## Estructura

Plantilla adaptada, no los 15 ficheros: `Usuario` no tiene casos de uso de
listado ni de borrado, y crear interfaces vacías para cumplir el molde sería
ruido. Se mantienen las tres capas y `domain/` sin Spring ni JPA.

```
auth/
├── application/
│   ├── in/   GetUsuarioInterface · GetOrCreateUsuarioInterface
│   │         RegistrarUsuarioInterface · LoginInterface · VerificarEmailInterface
│   └── out/  UsuarioRepositoryPort · PasswordHasherPort · EnviarVerificacionPort
├── domain/
│   ├── model/    Usuario · PerfilGoogle · UsuarioNotFoundException
│   │             CredencialesInvalidasException · EmailNoVerificadoException
│   └── service/  UsuarioService
└── infrastructure/
    ├── persistence/  UsuarioEntity · UsuarioRepository · UsuarioJpaAdapter
    │                 AuthController · dto/in · dto/out · mapper/
    └── security/     SecurityConfig · JwtService · JwtAuthenticationFilter
                      OAuth2LoginSuccessHandler · BCryptPasswordHasherAdapter
                      LogEnviarVerificacionAdapter (@Profile dev)
```

`GetOrCreate` no está en la lista de verbos de [[convenciones]]. Es la
composición de dos que sí están, y describe exactamente lo que hace el caso de
uso: buscar por `googleId` y, si no existe, buscar por email o crear.

## Endpoints

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/oauth2/authorization/google` | Arranca el login con Google. Lo sirve Spring Security |
| GET | `/api/auth/usuario` | **Protegido.** Devuelve tu usuario |
| POST | `/api/auth/registro` | Registro nativo. Manda email de verificación |
| POST | `/api/auth/login` | Login nativo por username o email |
| GET | `/api/auth/verificacion?token=` | Confirma el email del enlace de registro |

## Configuración

En `.env`, ver `.env.example`:

- `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET` — de Google Cloud Console
- `POLARIS_JWT_SECRETO` — mínimo 32 caracteres, o la aplicación no arranca
- `POLARIS_JWT_EXPIRACION` — segundos de vida del token de sesión, por
  defecto 43200 (12 h). El token de verificación siempre dura 24 h, fijo en
  `JwtService`
- `POLARIS_URL_BASE` — para construir el enlace de verificación, por defecto
  `http://localhost:8080`

URI de redirección autorizada en Google:
`http://localhost:8080/login/oauth2/code/google`

Scopes `openid`, `email` y `profile`: no son sensibles, así que la app no pasa
por el proceso de verificación de Google. **Pásala a "In production"** en la
consola: en estado "Testing" los refresh tokens caducan a los 7 días.

## Pendiente

- **`SmtpEnviarVerificacionAdapter`**, el adaptador de prod de
  `EnviarVerificacionPort`. Necesita aprobar `spring-boot-starter-mail`
  (dependencia nueva) y una cuenta SMTP real. Hoy, en `dev`, el enlace se ve
  en el log.
- Cuando exista React (tras B3), `OAuth2LoginSuccessHandler` deja de devolver
  JSON y pasa a redirigir al frontend con el token.
- Refresh token propio. Hoy la sesión caduca a las 12 h y toca volver a
  pasar por Google o a hacer login nativo otra vez.
