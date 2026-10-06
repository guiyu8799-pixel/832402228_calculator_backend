# Calculator Backend — 前后端分离计算器系统（后端）

Calculator system backend for the *First Assignment — Front-End and Back-End Separation Calculator
System*. It owns **all** calculation logic, input validation, exception handling and the
**calculation history database**.

The front end only sends an expression and renders whatever this service returns.

**Demonstration screenshots:** see [`docs/screenshots/`](docs/screenshots/README.md).

---

## Table of contents

- [1. Introduction](#1-introduction)
- [2. Technology stack](#2-technology-stack)
- [3. Runtime environment](#3-runtime-environment)
- [4. Installation](#4-installation)
- [5. Startup](#5-startup)
- [6. Configuration](#6-configuration)
- [7. Database initialisation](#7-database-initialisation)
- [8. API reference](#8-api-reference)
- [9. Front-end / back-end connection](#9-front-end--back-end-connection)
- [10. Testing](#10-testing)
- [11. Project structure](#11-project-structure)
- [12. Design decisions](#12-design-decisions)
- [13. Troubleshooting](#13-troubleshooting)

---

## 1. Introduction

The service exposes a JSON REST API that:

1. receives a raw mathematical expression,
2. validates and normalises it,
3. parses and evaluates it with a **hand-written recursive descent parser**,
4. stores every successful calculation in a **SQLite** database,
5. returns the result, and
6. serves and deletes the stored history.

User input is **never** passed to `eval`, `exec`, `ScriptEngine`, Nashorn, Groovy or any other
arbitrary code execution facility. The grammar is limited to numbers, `+ - * /` and parentheses, so
no input can escape into program code.

## 2. Technology stack

| Layer | Technology | Version | Why |
| --- | --- | --- | --- |
| Language | Java | 8 | Verified with JDK 1.8.0_231 |
| Framework | Spring Boot | 2.7.18 | Self-contained REST + dependency injection |
| Web | Spring MVC (`spring-boot-starter-web`) | — | REST controllers, JSON serialisation |
| Persistence | Spring Data JPA + Hibernate | 5.6.x | Repository abstraction over the history table |
| Database | SQLite (`org.xerial:sqlite-jdbc`) | 3.44.1.0 | Zero-install single-file database |
| Dialect | `com.github.gwenn:sqlite-dialect` | 0.1.4 | Hibernate 5.6 has no built-in SQLite dialect |
| Build | Apache Maven | 3.6+ | Standard Spring Boot build |
| Tests | JUnit 5 + MockMvc | — | Unit tests of the parser and end-to-end API tests |

**No third-party expression parser is used**: the engine in `com.fzu.calculator.expression` is
written from scratch for this assignment.

## 3. Runtime environment

| Requirement | Version |
| --- | --- |
| JDK | 8 or newer |
| Maven | 3.6 or newer |

Verify with `java -version` and `mvn -v`.

## 4. Installation

```bash
git clone <backend-repository-url>
cd <StudentID>_calculator_backend

# Download dependencies and produce target/calculator-backend-1.0.0.jar
mvn clean package
```

## 5. Startup

```bash
# packaged jar (recommended)
java -jar target/calculator-backend-1.0.0.jar

# or from source
mvn spring-boot:run
```

The service then listens on **`http://localhost:8080`**. Confirm it is alive:

```bash
curl http://localhost:8080/api/health
# {"success":true,"status":"UP","service":"calculator-backend"}
```

Use another port with `--server.port=9090`.

## 6. Configuration

All settings live in [`src/main/resources/application.yml`](src/main/resources/application.yml) and
can be overridden on the command line without touching the source.

| Property | Default | Meaning |
| --- | --- | --- |
| `server.port` | `8080` | HTTP port |
| `app.database.path` | `./data/calculator.db` | SQLite file, resolved against the working directory of the process |
| `app.cors.allowed-origins` | `*` | Comma-separated origins allowed to call `/api/**` |
| `spring.jpa.hibernate.ddl-auto` | `update` | Creates/updates the schema on startup |

```bash
java -jar target/calculator-backend-1.0.0.jar \
  --server.port=9090 \
  --app.database.path=/var/lib/calculator/calculator.db
```

> For a public deployment set `app.cors.allowed-origins` to the exact front-end origin instead of `*`.

## 7. Database initialisation

**Nothing has to be installed and no SQL script has to be run manually.**

1. On startup the application creates the parent directory of `app.database.path` if it is missing
   (SQLite creates the file itself but not the folder).
2. Hibernate with `ddl-auto: update` creates the table and the index on first run.

```sql
CREATE TABLE calculation_history (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    expression  VARCHAR(512) NOT NULL,   -- canonical expression, e.g. "(1+2)*3"
    result      VARCHAR(128) NOT NULL,   -- result as plain decimal text (no precision loss)
    created_at  DATETIME     NOT NULL    -- creation time
);

CREATE INDEX idx_history_created_at ON calculation_history (created_at);
```

Inspect the data with the `sqlite3` CLI:

```bash
sqlite3 data/calculator.db "SELECT * FROM calculation_history ORDER BY id DESC LIMIT 5;"
```

To reset the database, stop the service and delete `data/calculator.db`.

## 8. API reference

Base URL: `http://localhost:8080`. All responses are JSON.

| Method | Path | Purpose | Success |
| --- | --- | --- | --- |
| `GET` | `/api/health` | Liveness probe | `200 OK` |
| `POST` | `/api/calculate` | Evaluate an expression and persist the result | `200 OK` |
| `GET` | `/api/history` | The stored history, newest first | `200 OK` |
| `DELETE` | `/api/history/{id}` | Delete one history record | `200 OK` |

Errors: `400 Bad Request` (invalid or empty expression), `404 Not Found` (unknown history id),
`500 Internal Server Error` (unexpected failure).

### `POST /api/calculate`

Request:

```json
{ "expression": "(1+2)*3" }
```

Success — `200 OK`:

```json
{
  "success": true,
  "expression": "(1+2)*3",
  "result": 9,
  "id": 1,
  "createdAt": "2026-10-01 10:20:00"
}
```

Failure — `400 Bad Request`:

```json
{
  "success": false,
  "errorCode": "DIVISION_BY_ZERO",
  "message": "除数不能为 0"
}
```

`errorCode` is one of `INVALID_EXPRESSION`, `DIVISION_BY_ZERO`, `EMPTY_EXPRESSION`, `NOT_FOUND`,
`INTERNAL_ERROR`.

### `GET /api/history`

```json
{
  "success": true,
  "data": [
    { "id": 3, "expression": "(2+3)*4", "result": 20, "createdAt": "2026-10-01 10:22:00" },
    { "id": 2, "expression": "5*8",     "result": 40, "createdAt": "2026-10-01 10:21:00" }
  ],
  "total": 3
}
```

### `DELETE /api/history/{id}`

```json
{ "success": true, "message": "历史记录 3 已删除", "deletedId": 3 }
```

Unknown id → `404 Not Found` with `"errorCode": "NOT_FOUND"`.

### Copy-paste verification

A runnable request collection is provided in [`docs/api.http`](docs/api.http).

```bash
curl -X POST http://localhost:8080/api/calculate \
     -H "Content-Type: application/json" -d "{\"expression\":\"12+8\"}"

curl -X POST http://localhost:8080/api/calculate \
     -H "Content-Type: application/json" -d "{\"expression\":\"(1+2)*3\"}"

curl -X POST http://localhost:8080/api/calculate \
     -H "Content-Type: application/json" -d "{\"expression\":\"1/0\"}"

curl "http://localhost:8080/api/history"

curl -X DELETE http://localhost:8080/api/history/1
```

## 9. Front-end / back-end connection

The repositories are completely independent. This service knows nothing about the front end; the
front end talks to it over HTTP/JSON.

1. Start this backend (`http://localhost:8080`).
2. Start the front end (`http://localhost:5510` by default).
3. The front end's API base URL is the `API_BASE_URL` constant at the top of its `src/js/app.js`.

With the default `app.cors.allowed-origins: "*"` no extra setup is needed locally.

**Separation proof:** stop this service, keep the front end open and press `=`. The UI still
responds, but no new result can be produced — the front end shows a backend-unavailable error. The
client cannot compute anything by itself.

## 10. Testing

```bash
mvn test
```

- `ExpressionCalculatorTest` — the expression engine: the four basic operations, operator
  precedence, parentheses, unary signs, decimals, division by zero, invalid and empty input, and
  normalisation of `×`/`÷`/full-width characters.
- `CalculatorApiIntegrationTest` — the REST API against a real Spring context and a real SQLite
  database: calculation, error status codes, history retrieval, deleting one record, and the
  guarantee that failed calculations are not persisted.

The tests write their database to `target/test-data/` and never touch `data/calculator.db`.

## 11. Project structure

```text
calculator_backend/
├── src/
│   ├── main/
│   │   ├── java/com/fzu/calculator/
│   │   │   ├── CalculatorApplication.java          # entry point, DB directory bootstrap
│   │   │   ├── config/WebConfig.java               # CORS configuration
│   │   │   ├── controller/CalculatorController.java # REST endpoints
│   │   │   ├── service/
│   │   │   │   ├── CalculationService.java         # validate -> evaluate -> persist
│   │   │   │   └── HistoryService.java             # query / delete history
│   │   │   ├── expression/
│   │   │   │   ├── ExpressionNormalizer.java       # ×, ÷, full-width -> canonical ASCII
│   │   │   │   ├── Lexer.java                      # text -> tokens
│   │   │   │   ├── Token.java / TokenType.java     # token model
│   │   │   │   └── ExpressionCalculator.java       # recursive descent parser + evaluator
│   │   │   ├── entity/CalculationHistory.java      # JPA entity of calculation_history
│   │   │   ├── repository/CalculationHistoryRepository.java
│   │   │   ├── dto/                                # request/response bodies
│   │   │   └── exception/                          # error codes, exceptions, handler
│   │   └── resources/application.yml               # configuration
│   └── test/java/com/fzu/calculator/               # JUnit 5 test suites
├── docs/
│   ├── api.http                                    # runnable request collection
│   └── screenshots/                                # demonstration screenshots + index
├── pom.xml
├── README.md
├── codestyle.md
└── .gitignore
```

## 12. Design decisions

| Decision | Reason |
| --- | --- |
| Hand-written recursive descent parser instead of `eval`/`exec`/`ScriptEngine` | Required by the assignment and by security. Whitelisting a tiny grammar makes arbitrary code execution impossible. |
| Grammar `expression → term → unary → primary` | Encodes operator precedence directly in the grammar, so precedence is structural rather than a special case. |
| `BigDecimal` with `MathContext.DECIMAL128` | `0.1 + 0.2` must be `0.3`, which binary floating point cannot guarantee. |
| Result stored as `VARCHAR` plain decimal text | Persisting the exact decimal string avoids any round-trip through a floating point column and keeps very large integers intact. |
| Result recomputed by the server on every request | The front end must never be able to influence or supply a result. |
| `CalculationException` + `ErrorCode` enum | One place maps business failures to HTTP 400 with a stable machine-readable code the UI can react to. |
| History ordered by `id DESC` | The id is monotonic, so ordering is stable even for records created within the same second. |
| Failed calculations are not stored | The history table is a log of *successful* calculations; a `1/0` attempt is not a result. |

## 13. Troubleshooting

| Symptom | Cause / fix |
| --- | --- |
| `Unable to resolve name [org.hibernate.dialect.SQLiteDialect]` | The dialect must be `org.sqlite.hibernate.dialect.SQLiteDialect` (from `sqlite-dialect`). |
| `path to 'data/calculator.db': ... does not exist` | The working directory is not writable or `data/` could not be created; start the jar from a writable directory. |
| Front end reports it cannot reach the backend | The service is not running, the port differs, or CORS blocks the origin. Check `curl http://localhost:8080/api/health`. |
| `Port 8080 was already in use` | Another process holds the port: `--server.port=9090`. |
| Tests fail with a locked database | A previously running instance still holds `target/test-data/calculator-test.db`; stop it and re-run. |

---

## Code standard

See [`codestyle.md`](codestyle.md) — based on the
[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
