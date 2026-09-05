# kovu-backend

Backend educativo: HTTP con `com.sun.net.httpserver`, persistencia JDBC pura
sobre MySQL, sin ORM ni frameworks web. Cada capa (router, filtros, pool de
conexiones, mapeo fila→objeto) está escrita a mano a propósito.

## Stack

| Capa | Elección |
|---|---|
| Lenguaje | Java 25 (LTS) |
| Servidor HTTP | `com.sun.net.httpserver` (JDK) |
| Persistencia | JDBC puro + `mysql-connector-j` |
| Base de datos | MySQL 8.4 (InnoDB) |
| Build | Maven |
| JSON | Jackson Databind |
| Tests | JUnit 5 + AssertJ |
| Cobertura | JaCoCo |
| Cliente HTTP (tests) | `java.net.http.HttpClient` (JDK) |
| Logging | `System.Logger` (JDK) |
| Empaquetado | `maven-shade-plugin` + Docker |

## Arquitectura

```
com.kovu
├── Main.java            # cablea todo a mano (sin DI framework)
├── config/               # AppConfig: lectura de env vars
├── controller/            # adaptador HTTP: valida forma, delega, mapea a DTO
├── service/                # casos de uso, reglas de negocio, orquestación
├── repository/             # INTERFAZ del repositorio (el "puerto")
│   └── jdbc/                #   implementación JDBC (el "adaptador")
├── model/                   # entidades de dominio (records)
├── dto/                      # forma del JSON de entrada/salida
├── mapper/                    # dominio <-> DTO
├── exception/                  # jerarquía sellada de excepciones de negocio
└── platform/                    # el "mini-framework" propio
    ├── http/                     # Router, Filter, RequestContext, ErrorMapper, Server
    └── db/                        # ConnectionPool, Database, MigrationRunner
```

**Por qué así y no hexagonal "completo" desde el día 1:** ver la
justificación completa que se discutió con el profe/alumno — en resumen,
`controller/service/repository/exception` es el vocabulario estándar de la
industria y no requiere traducir mentalmente "puerto/adaptador" antes de
escribir el primer endpoint. La única idea hexagonal que se adopta ya
mismo es separar `repository/` (interfaz) de `repository/jdbc/`
(implementación): es lo que permite testear el `service` sin MySQL
levantado (ver `InMemoryProductoRepository` en los tests). El resto
(`domain/`, `application/`, `presentation/` con puertos y adaptadores
explícitos) es un refactor natural para más adelante, cuando el
`ProductoService` empiece a doler de verdad.

## Flujo de una request

`HttpServer` → `Router` (matchea método+path, extrae path params) →
cadena de `Filter`s (`RequestIdFilter`, `LoggingFilter`) → `Handler` del
`Controller` → `Service` → `Repository` (interfaz) → `JdbcProductoRepository`
→ `Database`/`ConnectionPool` → MySQL.

Cualquier excepción (de negocio o no) que se escape de un handler la
atrapa el `Router` y la traduce a una respuesta HTTP consistente a través
de `ErrorMapper`, usando pattern matching sobre la jerarquía **sellada**
`AppException` (`NotFoundException` → 404, `ValidationException` → 422,
`ConflictException` → 409; cualquier otra cosa → 500).

## Requisitos

- JDK 25 (`java -version` debe mostrar 25.x)
- Maven 3.9+
- Docker + Docker Compose (para MySQL y/o el empaquetado final)

Si tenés varias versiones de Java instaladas, fijá `JAVA_HOME` antes de
correr Maven:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 25)   # macOS
mvn -version   # confirmar que dice "Java version: 25..."
```

## Correr en local

1. Levantar solo la base:
   ```bash
   docker compose up -d mysql
   ```
2. Compilar y correr la app (usa los defaults de `AppConfig`, que ya
   apuntan a `localhost:3306`):
   ```bash
   mvn -B clean package
   java -jar target/kovu-backend.jar
   ```
3. Probar:
   ```bash
   curl http://localhost:8080/health
   curl -X POST http://localhost:8080/productos \
     -H "Content-Type: application/json" \
     -d '{"sku":"MATE-1","nombre":"Mate imperial","precioCentavos":500000,"stock":10}'
   curl http://localhost:8080/productos
   ```

## Correr todo con Docker

```bash
docker compose up --build
```

Levanta MySQL 8.4 y la app (compilada dentro del contenedor con Maven +
JDK 25, empaquetada como jar único con `maven-shade-plugin`, corrida
sobre un JRE 25 liviano).

## Tests

```bash
mvn test                       # suite rápida: unit tests + integration tests sin MySQL
```

Los tests contra MySQL real (`@Tag("integration")`, ej.
`JdbcProductoRepositoryTest`) están excluidos del ciclo normal (ver
`excludedGroups` en `pom.xml`) porque necesitan la base levantada:

```bash
docker compose up -d mysql
mvn test -DexcludedGroups= -Dgroups=integration -Dtest=JdbcProductoRepositoryTest
```

### Cobertura

```bash
mvn test
open target/site/jacoco/index.html   # macOS
```

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/health` | chequeo de vida |
| GET | `/productos` | listar todos |
| GET | `/productos/{id}` | obtener uno |
| POST | `/productos` | crear (`{sku, nombre, precioCentavos, stock}`) |
| PUT | `/productos/{id}` | actualizar (mismo body; el `sku` no se puede cambiar) |
| DELETE | `/productos/{id}` | eliminar |
| POST | `/productos/{id}/venta` | descontar stock (`{cantidad}`), atómico contra condiciones de carrera |

## Cómo agregar un nuevo recurso (ej. `Cliente`)

Copiar el patrón de `Producto` de punta a punta:

1. `model/Cliente.java` — record con invariantes en el constructor compacto.
2. `repository/ClienteRepository.java` — interfaz.
3. `repository/jdbc/JdbcClienteRepository.java` — implementación con SQL explícito.
4. `service/ClienteService.java` — reglas de negocio.
5. `dto/ClienteRequest.java` / `ClienteResponse.java` + `mapper/ClienteMapper.java`.
6. `controller/ClienteController.java` — registra sus rutas en el `Router`.
7. `src/main/resources/db/migration/V2__create_clientes.sql` + agregarlo al
   array `MIGRACIONES` en `MigrationRunner`.
8. Cablear en `Main.java`.
9. Tests: `ClienteServiceTest` (con un fake repo in-memory) y, opcionalmente,
   `ClienteControllerTest` y `JdbcClienteRepositoryTest`.
