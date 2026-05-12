# MarketViz — Architecture

## Overview

MarketViz is a three-tier web application:

```
┌─────────────────────────────────────────────────────────┐
│  Browser                                                │
│  React 19 SPA  ←─────── JWT Bearer ──────────►         │
│  (port 3000 dev / nginx prod)                           │
└──────────────────────────┬──────────────────────────────┘
                           │ HTTP / JSON
┌──────────────────────────▼──────────────────────────────┐
│  Spring Boot 3.4  (port 8080)                           │
│  ├─ SecurityConfig  (stateless JWT, CORS)               │
│  ├─ Controllers     (REST, OpenAPI docs)                 │
│  ├─ Services        (business logic, data mapping)      │
│  ├─ StooqFetchService  (HTTP → JSON → DB)               │
│  └─ DataIngestionScheduler (@Scheduled + startup hook)  │
└──────────────────────────┬──────────────────────────────┘
                           │ JDBC (HikariCP)
┌──────────────────────────▼──────────────────────────────┐
│  PostgreSQL 17                                          │
│  Flyway-managed schema (V1 init, V2 asset seed)         │
└─────────────────────────────────────────────────────────┘
                 ▲
                 │ HTTPS GET (JSON)
        api.twelvedata.com  (free tier)
```

## Data flow

### Ingestion path
1. `DataIngestionScheduler` fires on startup and once daily at 08:00.
2. For each active asset, `StooqFetchService` issues an HTTP GET to Twelve Data requesting
   up to 5000 daily bars (`outputsize=5000`, ~20 years of history).
3. The JSON `values` array is parsed and inserted into `price_points`.
   Duplicate rows (same `asset_id` + `timestamp`) are silently skipped via the DB unique constraint.
4. A 9-second pause between assets keeps the request rate within the free-tier 8 credits/minute limit.

### Read path
1. The React frontend sends `GET /api/assets/{id}/prices?range=ONE_YEAR` with a Bearer token.
2. `AssetController` calls `AssetService.findPrices`, which queries `price_points` directly with a
   date-bounded JPA query.
3. Results are projected to `PricePointResponse` records and serialised as JSON.
4. The frontend feeds the array into a Recharts `LineChart`, `BarChart`, or renders a table.

### Auth path
1. `POST /api/auth/register` or `/login` → `AuthService` → Spring Security `AuthenticationManager`
   → BCrypt verify → `JwtTokenProvider.generateToken` → `TokenResponse` (JWT + username + role).
2. The React `AuthContext` stores the token in `localStorage`; the axios `client.js` interceptor
   injects it as `Authorization: Bearer <token>` on every subsequent request.
3. `JwtAuthenticationFilter` validates the token on each request and populates the
   `SecurityContextHolder`.

## Package structure (backend)

```
com.marketviz
├── config/          OpenAPI definition
├── controller/      REST controllers (thin — delegate to services)
├── dto/             Records for request/response payloads
│   ├── auth/
│   ├── asset/
│   └── dashboard/
├── exception/       ApiException hierarchy + GlobalExceptionHandler
├── model/           JPA entities + enums
├── repository/      Spring Data JPA interfaces
├── security/        JWT provider, filter, UserDetailsService, SecurityConfig
└── service/         Business logic
```

## Component structure (frontend)

```
src/
├── api/             Thin axios wrappers per domain
├── components/
│   ├── charts/      PriceLineChart, PriceBarChart, PriceTable, NumberWidget
│   └── layout/      NavBar, ProtectedRoute
├── contexts/        AuthContext (JWT + localStorage)
└── pages/           LoginPage, RegisterPage, DashboardPage
```

## Database schema

| Table          | Purpose                                              |
|----------------|------------------------------------------------------|
| `users`        | Registered users with BCrypt hashes and roles        |
| `assets`       | Tradeable instruments with Twelve Data ticker mappings (column named `stooq_symbol` for historical reasons) |
| `price_points` | Daily OHLCV records, unique per asset+date           |
| `dashboards`   | Named widget collections owned by a user             |
| `widgets`      | Individual chart/table panels with display config    |

Migrations live in `src/main/resources/db/migration/` and are run by Flyway on startup.
JPA is configured with `ddl-auto: validate` — it never modifies the schema; Flyway owns it.

## Security model

- All API endpoints except `/api/auth/**` and the Swagger UI require a valid JWT.
- Tokens are HS256-signed, 24 h expiry by default (configurable via `JWT_EXPIRATION_MS`).
- Roles: `USER` (default) and `ADMIN`. Admin-only endpoints live under `/api/admin/`.
- Dashboard ownership is enforced at the service layer — a user cannot read or modify another
  user's dashboards even with a valid token.
- CORS is restricted to `http://localhost:3000` by default; update `SecurityConfig` for production.
