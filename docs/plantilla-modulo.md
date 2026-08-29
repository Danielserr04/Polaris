# Plantilla de módulo

El molde exacto que se replica para **cada entidad**. No se improvisa ni se recorta: si una entidad existe, tiene estos ficheros.

Ejemplo con la entidad `Titulo` del módulo [[odisea]].

> **Esta nota es la fuente de la verdad del estilo de código.** No es un borrador ni está pendiente de contrastar con nada. Lo que está aquí escrito es lo que se escribe. Ver [[005-estilo-propio-sin-referencia]].

## Árbol

```
odisea/
├── application/
│   ├── in/
│   │   ├── CreateTituloInterface
│   │   ├── DeleteTituloInterface
│   │   ├── GetTituloInterface
│   │   ├── ListTituloInterface
│   │   └── UpdateTituloInterface
│   └── out/
│       └── TituloRepositoryPort
├── domain/
│   ├── model/
│   │   ├── Titulo
│   │   └── TituloFilter
│   └── service/
│       └── TituloService
└── infrastructure/
    └── persistence/
        ├── dto/
        │   ├── in/
        │   │   ├── TituloRequestDto
        │   │   └── TituloFilterListDto
        │   └── out/
        │       ├── TituloFormDto
        │       └── TituloListDto
        ├── mapper/
        │   ├── TituloEntityMapper
        │   ├── TituloFilterMapper
        │   ├── TituloFormDtoMapper
        │   ├── TituloListDtoMapper
        │   └── TituloRequestDtoMapper
        ├── TituloController
        ├── TituloEntity
        ├── TituloJpaAdapter
        └── TituloRepository
```

Son 15 ficheros por entidad. Es mucho, y es a propósito: cada uno tiene una sola razón para cambiar.

## Qué hace cada pieza

| Fichero | Responsabilidad |
|---|---|
| `Create<E>Interface` | Un caso de uso, un método. Lo implementa el Service |
| `<E>RepositoryPort` | Lo que el dominio necesita de la persistencia, en lenguaje de dominio |
| `<E>` | Modelo puro. Sin anotaciones, sin dependencias |
| `<E>Filter` | Criterios de búsqueda como datos, no como `Specification` |
| `<E>Service` | Implementa todas las interfaces de `application/in`. Aquí vive la lógica |
| `<E>Controller` | Entrada HTTP. Solo habla DTOs |
| `<E>Entity` | Mapeo JPA. Anotaciones aquí y solo aquí |
| `<E>Repository` | Interfaz Spring Data. Solo la usa el JpaAdapter |
| `<E>JpaAdapter` | Implementa el Port. Traduce entre modelo y Entity, construye Specifications |
| `<E>RequestDto` | Lo que llega en un POST o PUT |
| `<E>FilterListDto` | Los filtros que llegan por query params |
| `<E>FormDto` | La ficha completa que devuelve el detalle |
| `<E>ListDto` | La versión ligera para el listado |
| `mapper/*` | Un mapper MapStruct por conversión |

## La regla de los DTOs

- **`dto/in`** — lo que entra por HTTP
- **`dto/out`** — lo que sale

`FormDto` y `ListDto` existen por separado a propósito: el listado de 200 títulos no debe arrastrar sinopsis ni géneros. Es la diferencia entre una lista que va fluida y una que no.

## Esqueletos

### `application/in/CreateTituloInterface`

```java
public interface CreateTituloInterface {
    Titulo create(Titulo titulo);
}
```

Una interfaz, un método. Sin excepciones.

### `application/out/TituloRepositoryPort`

```java
public interface TituloRepositoryPort {
    Titulo save(Titulo titulo);
    Optional<Titulo> findById(Long id);
    List<Titulo> findAll(TituloFilter filter);
    void deleteById(Long id);
    boolean existsByIdExterno(String idExterno, FuenteExterna fuente);
}
```

Habla de `Titulo`, no de `TituloEntity`. El dominio no sabe que JPA existe.

### `domain/model/Titulo`

```java
public class Titulo {
    private Long id;
    private TipoContenido tipo;
    private String titulo;
    private Integer anio;
    // ...
}
```

Sin `@Entity`, sin `@Table`, sin `@Column`. Si aparece una anotación de persistencia aquí, algo se ha colado.

### `domain/service/TituloService`

```java
@Service
@RequiredArgsConstructor
public class TituloService implements
        CreateTituloInterface,
        GetTituloInterface,
        ListTituloInterface,
        UpdateTituloInterface,
        DeleteTituloInterface {

    private final TituloRepositoryPort repository;
    // ...
}
```

Campos `final` y `@RequiredArgsConstructor`. Nunca `@Autowired` en campo. `@Service` y la anotación de Lombok son las únicas concesiones a Spring dentro de `domain/`, y `@Service` es la que hereda la convención de GestionER.

### `infrastructure/persistence/TituloJpaAdapter`

```java
@Component
@RequiredArgsConstructor
public class TituloJpaAdapter implements TituloRepositoryPort {

    private final TituloRepository repository;
    private final TituloEntityMapper mapper;

    @Override
    public List<Titulo> findAll(TituloFilter filter) {
        Specification<TituloEntity> spec = TituloSpecifications.from(filter);
        return mapper.toDomainList(repository.findAll(spec));
    }
}
```

El único punto del módulo donde conviven modelo y Entity.

### `infrastructure/persistence/mapper/TituloEntityMapper`

```java
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TituloEntityMapper {
    Titulo toDomain(TituloEntity entity);
    TituloEntity toEntity(Titulo domain);
    List<Titulo> toDomainList(List<TituloEntity> entities);
}
```

`unmappedTargetPolicy = ERROR` es importante: si añades un campo al modelo y olvidas mapearlo, **falla al compilar** en vez de darte un `null` silencioso en producción.

### `infrastructure/persistence/TituloController`

```java
@RestController
@RequestMapping("/api/odisea/titulo")
@RequiredArgsConstructor
public class TituloController {

    private final ListTituloInterface listTitulo;
    private final GetTituloInterface getTitulo;
    // ...
}
```

**Inyecta las interfaces de caso de uso, no el Service.** Así el Controller declara exactamente qué necesita, y se ve de un vistazo.

## Checklist al crear una entidad

- [ ] Los 15 ficheros creados
- [ ] `domain/` sin un solo import de Spring ni JPA (salvo `@Service` y `@RequiredArgsConstructor`)
- [ ] Migración Flyway con la tabla
- [ ] `usuario_id` presente si son datos personales
- [ ] Mappers con `unmappedTargetPolicy = ERROR`
- [ ] Controller devolviendo DTOs, nunca modelo ni Entity
- [ ] Probado con Postman o similar
- [ ] Nota del módulo en `docs/modulos/` actualizada

## Sobre el origen de esta convención

La **estructura de carpetas** viene de GestionER. El **estilo interno de las clases** es propio de Polaris: se fija aquí y aquí se consulta.

No hay nada pendiente de contrastar. Si en algún momento se quiere alinear con otra referencia, se abre una nota nueva en `docs/decisiones/` y se cambia esta con conocimiento de causa. Mientras tanto, esto es lo que hay.

Ver [[005-estilo-propio-sin-referencia]].
