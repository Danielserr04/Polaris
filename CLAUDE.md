# Polaris

App web personal y modular. Backend Spring Boot + MySQL, frontend React.
Monolito modular con arquitectura hexagonal, una carpeta por módulo.

La documentación completa está en `docs/`. **Este fichero solo contiene las reglas que no se rompen.**
Lee `docs/00-indice.md` cuando necesites contexto de un módulo concreto.

---

## Reglas duras

1. **`domain/` no importa Spring ni JPA.** Ni `jakarta.persistence`, ni `org.springframework`, ni anotaciones de MapStruct. Si necesitas eso en domain, la solución está mal planteada.
2. **Un módulo no llama a otro módulo.** Si Atlas necesita el peso corporal, consume el puerto de Núcleo. Nunca importa una clase de Fusión.
3. **El Controller solo habla DTOs.** Nunca devuelve modelo de dominio ni entidades JPA.
4. **La estructura de un módulo no se improvisa.** Se replica `docs/plantilla-modulo.md` tal cual, con todos sus ficheros.
5. **Toda tabla de datos personales lleva `usuario_id`.** Sin excepciones, aunque hoy solo haya un usuario.
6. **Ningún secreto en el repo.** Claves de APIs en variables de entorno desde el primer commit.
7. **`DECIMAL` para dinero y pesos.** Nunca `FLOAT` ni `DOUBLE`. Tablas en `utf8mb4`.

## Antes de escribir código

- **No crees un módulo entero de golpe.** Una entidad, completa y probada, antes de la siguiente.
- **No añadas dependencias sin preguntar.** El `pom.xml` se discute.
- **No refactorices lo que no te han pedido.** Si ves algo mal, dilo y sigue.
- **Si una decisión no está en `docs/decisiones/`, pregunta.** No la tomes por tu cuenta.

## Stack

- Java 21, Spring Boot 3.x
- MySQL 8. Esquema con `ddl-auto: update` hasta cerrar B2; Flyway a partir de ahí (ver `docs/decisiones/007-esquema-ddl-auto-luego-flyway.md`)
- MapStruct para todo el mapeo
- JPA Specifications para filtros dinámicos
- React en el frontend (aún sin empezar)

## Estructura raíz

```
com.polaris
├── shared/     config, seguridad, errores globales. No conoce a los módulos
├── auth/       Usuario, OAuth2 Google, JWT
├── nucleo/     perfil y peso corporal, compartido por fusion y atlas
├── odisea/     ocio
├── kuiper/     gastos
├── fusion/     nutrición
└── atlas/      gym
```

## Comandos

Proyecto en `C:\Dev\Polaris` (Windows).

```powershell
docker compose up -d          # MySQL
.\mvnw.cmd spring-boot:run    # backend
.\mvnw.cmd test               # tests
```

La vault de Obsidian es la carpeta `docs/`. Se abre esa carpeta, no la raíz del repo.

## Estado

Fase **B0** (esqueleto). Ver `docs/roadmap.md`.
