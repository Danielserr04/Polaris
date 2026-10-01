# 027 — Tests de integración contra MySQL real con Testcontainers

Estado: aceptada · 2026-10-01

## Contexto

Toda la suite era unitaria o de slice (`@WebMvcTest`): sin base de datos. Eso deja sin probar justo lo que más ha dado problemas y lo que más depende del motor: el HQL de agregación de [[026-progresion-y-records-por-volumen]] (una tabla derivada que exige MySQL 8.0.19 o superior; la nota 026 decía que "si algún día hay tests de integración con base de datos, estas son las primeras consultas a cubrir"), la violación real de `UNIQUE` que [[020-violacion-unicidad-409-y-errores-http-cliente]] traduce a 409 (los tests del handler fabrican la excepción a mano), el escape de `LIKE` de `PatronLike`, las collations `utf8mb4_unicode_ci` y que Flyway (V1..V14, [[007-esquema-ddl-auto-luego-flyway]]) más `ddl-auto: validate` dejen arrancar la aplicación entera. El roadmap lo apunta en B8. El usuario aprobó explícitamente la dependencia nueva (el `pom.xml` no se toca sin preguntar).

## Decisión

Tests de integración con **Testcontainers** sobre un **MySQL 8.4 real**, solo con `scope` `test`. Dependencias añadidas (versión gestionada por el BOM de Spring Boot 3.5.3, que trae Testcontainers 1.21.2; sin fijar versión): `org.testcontainers:mysql` y `org.testcontainers:junit-jupiter`. No se añade `spring-boot-testcontainers`: la URL del contenedor se inyecta con `@DynamicPropertySource`.

- **Base común** en `src/test/java/com/polaris/shared/testing/`: `IntegracionBase` (`@SpringBootTest` con `@AutoConfigureMockMvc`, perfiles `dev` e `integracion`, `@Tag("integracion")`, `@Testcontainers(disabledWithoutDocker = true)`) y `MySqlContenedor` (un único contenedor por ejecución, arrancado la primera vez que se pide y compartido por todas las clases; lo recoge Ryuk al acabar la JVM). Todas las clases de integración heredan de `IntegracionBase`, así comparten también el contexto de Spring (salvo la que usa `@MockitoSpyBean`, que obtiene el suyo).
- **Misma configuración que la app**: el `application.yml` de test sustituye al principal y no trae Flyway ni JPA, así que `src/test/resources/application-integracion.yml` replica `flyway.enabled: true` y `ddl-auto: validate`. Los secretos son los falsos de siempre (`src/test/resources/application.yml`).
- **Imagen**: `mirror.gcr.io/library/mysql:8.4` por defecto (Docker Hub devuelve 429 en el entorno cloud), configurable con la propiedad de sistema `polaris.test.mysql.imagen` o la variable de entorno `POLARIS_TEST_MYSQL_IMAGEN`. El contenedor arranca con `--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci`, como `docker-compose.yml`.
- **Filtrado**: todos llevan `@Tag("integracion")`. `-Dgroups=integracion` corre solo esos; `-DexcludedGroups=integracion` corre solo los unitarios. **Sin Docker se saltan** (`disabledWithoutDocker`) y `./mvnw test` sigue pasando.
- **Datos**: sin rollback, porque la clase de test, el servicio y el adaptador abren sus propias transacciones. Cada clase usa sus propios `usuarioId` (9101, 9201, 9301, 9401...) o un prefijo `IT ` en los datos del catálogo, y limpia por SQL antes de cada test (la de progresión, también después, porque deja series colgando de ejercicios del catálogo).
- **Qué cubren** (34 tests, sin repetir los unitarios): arranque completo con Flyway aplicado en orden, `validate`, utf8mb4 en todas las tablas, ningún `FLOAT`/`DOUBLE`, `usuario_id` en las tablas personales (`ArranqueIntegracionTest`); progresión y records de Atlas con valores calculados a mano, peso 0, aislamiento entre usuarios y desempates (`ProgresionRecordsIntegracionTest`, adaptadores, servicios y HTTP con JWT); la violación real de `UNIQUE` a 409 y de FK a 500 (`ViolacionUnicidadIntegracionTest`); Specifications reales de Movimiento, Alimento (`PatronLike`: "50%" y "a_b" literales, backslash, tildes y mayúsculas) y Ejercicio.
- **La carrera de unicidad se simula, no se provoca**: dos peticiones simultáneas no dan un resultado estable. Se espía el puerto (`@MockitoSpyBean`) para que la comprobación previa del servicio conteste "no hay duplicado", que es lo que vería la petición perdedora, y el resto (controller, servicio, adaptador, Hibernate, MySQL, handler) es real.

### Configuración del entorno

- **Docker 29 con Testcontainers 1.21.2**: el motor 29 exige API 1.40 como mínimo y el docker-java incluido habla la 1.32 ("client version 1.32 is too old"). `src/test/resources/docker-java.properties` fija `api.version=1.44` y lo resuelve sin tocar versiones. Hace falta un motor con API 1.44 o superior (Docker 25 o posterior). Cuando el BOM de Spring Boot traiga una versión de Testcontainers que lo arregle (1.21.4 o 2.x), ese fichero sobra.
- **Ryuk** funciona sin configuración especial (la imagen `testcontainers/ryuk` estaba disponible). Si el entorno no puede descargarla, `TESTCONTAINERS_RYUK_DISABLED=true` la desactiva a cambio de que los contenedores se paren a mano. `DOCKER_HOST` no hizo falta: se usa el socket Unix por defecto.
- **Probar que sin Docker se saltan**: `DOCKER_HOST=tcp://127.0.0.1:1` no basta, porque Testcontainers sigue buscando y encuentra el socket Unix. Se comprobó ocultando el socket en un espacio de montajes propio (`unshare -m`, con `mount --bind /dev/null /var/run/docker.sock`): 596 tests pasan y 34 salen como saltados.

## Alternativas descartadas

**H2 (u otra base embebida en modo MySQL).** Es lo que no prueba nada de lo que importa: el dialecto difiere (la tabla derivada de los records, `ENUM`, `BIT(1)`, `DECIMAL`, el escape de `LIKE`), las collations no existen (el unique que ignora mayúsculas y tildes, `LIKE` sin acentos) y el error 1062 que lee el handler es de MySQL. Además el HQL de records requiere MySQL 8.0.19 o superior: contra H2 pasaría por motivos equivocados o no pasaría. Una suite verde contra H2 daría una seguridad falsa.

**La base MySQL de desarrollo (o un MySQL compartido) en vez de un contenedor.** Los tests ensuciarían los datos de dev, dependerían del estado previo y no funcionarían en una máquina limpia ni en CI. El contenedor da una base vacía y con el esquema de Flyway en cada ejecución.

**Un contenedor por clase de test.** Cada arranque cuesta unos 10 segundos; compartir uno entre clases deja el coste en una vez por ejecución.

**`spring-boot-testcontainers` con `@ServiceConnection`.** Es más corto, pero es una dependencia más (la aprobada era mínima) y `@DynamicPropertySource` basta.

**Provocar la carrera de unicidad con dos hilos.** El resultado no es estable (la carrera puede no producirse) y un test que a veces no ejercita lo que dice es peor que ninguno.

## Consecuencias

- **Docker hace falta para correr los de integración**; sin él se saltan en silencio (aparecen como "Skipped" en el informe de Maven). Un desarrollador que nunca tenga Docker no los ejecutará y no se enterará: en CI conviene un paso que lo exija.
- **Tiempo**: la suite unitaria tarda unos 15 s; con los 34 de integración, unos 40 a 45 s en total. El grueso son 10 s de arranque del contenedor (una vez por ejecución) y 8 a 15 s por cada contexto de Spring completo, hay dos (el normal y el de `@MockitoSpyBean`). Añadir `@MockitoBean`/`@MockitoSpyBean` o propiedades distintas a una clase nueva crea otro contexto y suma esos segundos.
- **La primera ejecución descarga la imagen** (unos 250 MB comprimidos) y, si hace falta, Ryuk.
- **Los tests comparten base y no hacen rollback**: una clase nueva debe elegir `usuarioId`s que no usen otras, limpiar antes de cada test y, si deja filas con FK a datos que otra clase borra (los ejercicios del catálogo), limpiar también después.
- **Aviso conocido, no es un fallo**: Flyway avisa de que MySQL 8.4 es más nuevo que lo que su versión soporta oficialmente (8.1). Funciona; se revisará al subir de Flyway.
- Queda cubierto lo que decía [[026-progresion-y-records-por-volumen]] como pendiente; los tests de esa nota ya no son solo de dominio.
- Quedan sin cubrir con base real el resto de Specifications y adaptadores; se añaden por entidad cuando se toquen, no de golpe.
