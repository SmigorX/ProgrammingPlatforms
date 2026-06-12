# MarketViz

A self-hosted web application for visualising historical market data — commodity ETFs
(gold, silver, Brent / WTI crude, natural gas) and the Micron Technology stock as a
DRAM market proxy — through user-defined dashboards of charts, tables, and KPI tiles.

Each user signs up, builds one or more dashboards, and adds widgets backed by ~20 years
of daily OHLCV history pulled from a free third-party market data API into a local
PostgreSQL store.

---

## Features

- **Authentication** — registration, login, JWT-based stateless sessions, `USER` / `ADMIN` roles.
- **Asset catalogue** — six pre-seeded instruments, easily extensible via a new Flyway migration.
- **Dashboards** — multiple named dashboards per user, persisted server-side (synced across browsers).
- **Widgets** — line chart, bar chart, data table, or single-number tile per asset, with configurable
  time range (1 M / 3 M / 6 M / 1 Y / 3 Y / 5 Y) and colour.
- **Daily ingestion** — cron-driven full-history pull (idempotent via `UNIQUE(asset_id, timestamp)`).
- **OpenAPI / Swagger UI** — full interactive API reference at `/swagger-ui.html`.
- **Dockerised** — `docker compose up` brings up Postgres, backend, and frontend.

---

## Tech stack

### Backend
- **Java 21**, **Spring Boot 3.4** (Web, Data JPA, Security, Validation)
- **PostgreSQL 17**, **Flyway** for schema migrations
- **jjwt 0.12** for HS256 JWT signing/parsing
- **springdoc-openapi** 2.8 for live OpenAPI 3 docs
- **JUnit 5** + **Spring Security Test** for the unit/integration tests

### Frontend
- **React 19** (Create React App / `react-scripts` 5)
- **React Router 7**
- **Axios** for the HTTP client with a request interceptor that injects the JWT
- **Recharts 2** for SVG line / bar charts
- **nginx:stable-alpine** serves the production build

### Infrastructure
- **Docker Compose** orchestrates `postgres`, `backend`, and `frontend` services
- Multi-stage Dockerfiles (Maven → JRE; Node → nginx) keep final images slim

### External data
- **Twelve Data** `time_series` REST API (free tier)

---

## Quick start

### Prerequisites
- Docker + Docker Compose
- A free Twelve Data API key — register at [twelvedata.com](https://twelvedata.com) and
  copy the key from **Dashboard → API Keys** (no credit card required).

### Run

```bash
cp .env.example .env
# edit .env: set TWELVE_DATA_API_KEY=... and a long random JWT_SECRET
docker compose up --build
```

Then open:

| URL                                | What                                  |
| ---------------------------------- | ------------------------------------- |
| http://localhost:3000              | React frontend                        |
| http://localhost:8080/swagger-ui.html | Interactive API reference          |
| http://localhost:8080/api-docs     | Raw OpenAPI 3 JSON                    |
| localhost:5432                     | PostgreSQL (user/password `marketviz`) |

On the first startup the backend runs Flyway migrations, then `DataIngestionScheduler`
triggers a full price-history pull (~6 API credits). Subsequent runs fire daily at 08:00.

---

## Configuration

All runtime config is environment-variable driven.

| Variable              | Required | Default                                    | Purpose                          |
| --------------------- | -------- | ------------------------------------------ | -------------------------------- |
| `TWELVE_DATA_API_KEY` | **Yes**  | _(empty — ingestion logs an error)_        | Free API key for Twelve Data     |
| `JWT_SECRET`          | **Yes**  | dev-only placeholder (≥ 32 chars required) | HS256 signing key                |
| `JWT_EXPIRATION_MS`   | No       | `86400000` (24 h)                          | Token lifetime in milliseconds   |
| `DB_HOST`             | No       | `localhost` (`postgres` in compose)        | PostgreSQL hostname              |
| `DB_PORT`             | No       | `5432`                                     | PostgreSQL port                  |
| `DB_NAME`             | No       | `marketviz`                                | Database name                    |
| `DB_USER`             | No       | `marketviz`                                | DB user                          |
| `DB_PASSWORD`         | No       | `marketviz`                                | DB password                      |

`.env` is gitignored — never commit secrets. Use `.env.example` as a template.

---

## Project layout

```
.
├── backend/                       Spring Boot service (Java 21, Maven)
│   ├── src/main/java/com/marketviz/
│   │   ├── config/                OpenAPI bean definition
│   │   ├── controller/            REST endpoints (Auth, Asset, Dashboard, Admin)
│   │   ├── dto/                   Request/response records
│   │   ├── exception/             ApiException hierarchy + RFC 7807 handler
│   │   ├── model/                 JPA entities (User, Asset, PricePoint, Dashboard, Widget)
│   │   ├── repository/            Spring Data JPA interfaces
│   │   ├── security/              JWT provider, filter, SecurityConfig
│   │   └── service/               Business logic (Auth, Asset, Dashboard, Ingestion)
│   ├── src/main/resources/
│   │   ├── application.yml        Spring config (env-var driven)
│   │   └── db/migration/          Flyway SQL migrations (V1..V5)
│   ├── src/test/                  Unit tests (AuthService, StooqFetchService)
│   ├── Dockerfile                 Multi-stage Maven → Temurin JRE
│   └── pom.xml
├── frontend/                      React 19 SPA
│   ├── src/
│   │   ├── api/                   Thin axios wrappers per domain
│   │   ├── components/
│   │   │   ├── charts/            Line, Bar, Table, Number widgets
│   │   │   └── layout/            NavBar, ProtectedRoute
│   │   ├── contexts/              AuthContext (JWT + localStorage)
│   │   └── pages/                 Login, Register, Dashboard
│   ├── Dockerfile                 Multi-stage Node → nginx-alpine
│   ├── nginx.conf                 SPA-aware nginx config (proxies `/api/*` → backend)
│   └── package.json
├── docs/                          Project documentation (see below)
├── docker-compose.yml             Postgres + backend + frontend
├── .env.example                   Copy to .env and fill in real values
└── README.md                      You are here.
```

---

## API overview

All endpoints under `/api/**` except `/api/auth/**` require a Bearer JWT.

| Method | Path                                       | Auth      | Description                          |
| ------ | ------------------------------------------ | --------- | ------------------------------------ |
| POST   | `/api/auth/register`                       | public    | Register, returns JWT                |
| POST   | `/api/auth/login`                          | public    | Login, returns JWT                   |
| GET    | `/api/assets`                              | USER      | List all active assets               |
| GET    | `/api/assets/{id}/prices?range=ONE_YEAR`   | USER      | OHLCV data for a window              |
| GET    | `/api/dashboards`                          | USER      | List current user's dashboards       |
| POST   | `/api/dashboards`                          | USER      | Create dashboard                     |
| GET    | `/api/dashboards/{id}`                     | USER      | Get one dashboard                    |
| PUT    | `/api/dashboards/{id}`                     | USER      | Rename / set default                 |
| DELETE | `/api/dashboards/{id}`                     | USER      | Delete dashboard + widgets           |
| POST   | `/api/dashboards/{id}/widgets`             | USER      | Add a widget                         |
| PUT    | `/api/dashboards/{id}/widgets/{widgetId}`  | USER      | Update widget config                 |
| DELETE | `/api/dashboards/{id}/widgets/{widgetId}`  | USER      | Remove widget                        |
| POST   | `/api/admin/fetch`                         | **ADMIN** | Trigger an immediate ingestion run   |

`range` values: `ONE_MONTH`, `THREE_MONTHS`, `SIX_MONTHS`, `ONE_YEAR`, `THREE_YEARS`, `FIVE_YEARS`.
`chart_type` values: `LINE`, `BAR`, `TABLE`, `NUMBER`.

Full interactive reference: **http://localhost:8080/swagger-ui.html**.

---

## Local development

### Backend only

```bash
cd backend
mvn spring-boot:run
```

Requires a running PostgreSQL on `localhost:5432` matching the `DB_*` env vars
(or start just the DB with `docker compose up postgres`).

Run tests:

```bash
mvn test
```

Build a runnable JAR:

```bash
mvn package
java -jar target/marketviz-backend-0.1.0.jar
```

### Frontend only

```bash
cd frontend
npm install
npm start         # dev server on http://localhost:3000, proxies /api/* → :8080
npm test          # Jest + React Testing Library
npm run build     # production bundle into build/
```

### Generate Javadoc

```bash
cd backend
mvn javadoc:javadoc       # output: ../docs/javadoc/
```

### Promoting a user to ADMIN

There is no public registration path to the `ADMIN` role. Connect to the database and
update the row directly:

```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your-name';
```

---

## Documentation

In-depth design notes live in `docs/`:

- **[docs/architecture.md](docs/architecture.md)** — Three-tier overview, data/auth flow,
  package layout, DB schema, security model.
- **[docs/decisions.md](docs/decisions.md)** — Architecture Decision Records covering the
  Java/Spring choice, Postgres, stateless JWT, Twelve Data selection, MU as DRAM proxy,
  Recharts, and RFC 7807 error responses.
- **[docs/data-sources.md](docs/data-sources.md)** — Twelve Data endpoint contract, rate
  limits, tracked instrument table, and how to add a new asset.
- **[docs/javadoc/index.html](docs/javadoc/index.html)** — Generated Javadoc (run
  `mvn javadoc:javadoc` first).

---

## Notes

- The `assets.stooq_symbol` column name is a historical artefact — it now holds the
  Twelve Data ticker. See ADR-004 for the data-source history.
- JPA is configured with `ddl-auto: validate`; Flyway owns the schema. Schema/entity
  drift fails loudly on startup.
- Idempotent ingestion: re-running a fetch never duplicates rows
  (`UNIQUE(asset_id, timestamp)`).
- CORS in `SecurityConfig` is hard-coded to `http://localhost:3000`; update before
  deploying anywhere else.
