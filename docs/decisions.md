# Architecture Decision Records

## ADR-001 — Java / Spring Boot instead of .NET

**Context:** The course spec mandates .NET / C#. The project is permitted to use Java as an alternative.

**Decision:** Spring Boot 3.4 on Java 21 with Spring Data JPA, Spring Security, and springdoc-openapi.

**Rationale:**
- Spring Boot maps 1:1 to the .NET spec requirements: JPA ≈ Entity Framework, Spring Security ≈ ASP.NET Identity, springdoc ≈ Swagger/NSwag.
- Java 21 records serve the same role as C# record types — compact, immutable DTOs.
- The team is more productive in Java.

---

## ADR-002 — PostgreSQL as the relational store

**Decision:** PostgreSQL 17 in Docker, managed by Flyway migrations.

**Rationale:**
- Free, battle-tested, excellent JPA support.
- `NUMERIC(18,6)` type stores prices without floating-point rounding errors.
- Flyway (code-as-schema) means the schema can be reviewed in PRs and rolled back cleanly.
- JPA `ddl-auto: validate` ensures the running schema always matches the entity definitions;
  mismatches fail loudly on startup rather than silently truncating data.

---

## ADR-003 — Stateless JWT authentication

**Decision:** HS256-signed JWTs, 24 h expiry, no server-side session storage.

**Rationale:**
- The frontend is a React SPA — cookies and server sessions add complexity without benefit.
- Stateless tokens work across horizontal replicas without sticky sessions or a shared store.
- The signing key is supplied via `JWT_SECRET` env var; the default value in `application.yml`
  is only safe for local development.

**Trade-off:** Tokens cannot be revoked before expiry. Acceptable for this scope; a
  Redis-backed blocklist could be added later if needed.

---

## ADR-004 — Twelve Data as the market data source

**Decision:** All price data fetched from the Twelve Data `time_series` endpoint using a
free API key (instant registration at twelvedata.com, no credit card required).

**Rationale:**
Three sources were evaluated before settling on Twelve Data:

| Source               | Reason abandoned                                                                 |
|----------------------|----------------------------------------------------------------------------------|
| **stooq.pl**         | Returns a CAPTCHA challenge page from server code; per-session token required.   |
| **Yahoo Finance v8** | IP-level HTTP 429 block for all container requests, regardless of rate.          |
| **Alpha Vantage**    | `outputsize=full` (required for 5-year history) is a paid-tier-only feature.    |

Twelve Data's free tier supports `outputsize=5000` (~20 years of daily OHLCV data per
request), does not block cloud/container IPs, and allows 800 API credits/day at 8/minute —
well above what 6 daily asset fetches require.

**Trade-off:** Requires registering for a free API key and setting `TWELVE_DATA_API_KEY`
  in the `.env` file. Without a key the ingestion service will log an error on startup but
  the application starts normally and serves any already-stored data.

**Risk:** Twelve Data is a third-party SaaS. Already-stored data continues to serve normally
  if the API becomes unavailable or the key expires.

**Instruments used:** ETFs tracking commodity prices (GLD, SLV, BNO, USO, UNG) plus Micron
  Technology (MU) as a DRAM/NAND market proxy (see ADR-005).

---

## ADR-005 — Micron Technology (MU) as DRAM market proxy

**Decision:** Use the Micron Technology stock price (NASDAQ: MU) as the stand-in for
DRAM / RAM pricing.

**Rationale:**
- No free public DRAM spot price index API exists. DRAM Exchange (dramexchange.com) and
  DRAMeXchange have no public API; their data is paywalled.
- Micron is the largest pure-play DRAM and NAND manufacturer. Its stock price closely
  tracks DRAM market health: supply/demand shifts, pricing cycles, and capex announcements
  all move both MU and underlying DRAM contract prices.
- 20+ years of daily history available for free via Twelve Data.

---

## ADR-006 — Full-history download with DB-layer deduplication

**Decision:** Each ingestion run requests `outputsize=5000` from Twelve Data (~20 years of
daily data) and relies on the `UNIQUE(asset_id, timestamp)` DB constraint to silently
discard rows already stored.

**Rationale:**
- Twelve Data does not expose an incremental endpoint; the simplest correct approach is to
  always fetch the full available history and let the DB constraint handle duplicates.
- `DataIntegrityViolationException` on duplicate inserts is caught per-row and ignored, so
  the rest of the batch always commits successfully.
- With 800 credits/day and 6 assets/run, a once-daily full fetch uses less than 1% of the
  free-tier allowance.
- Idempotent by design — re-running or re-deploying never corrupts stored data.

---

## ADR-007 — Widget configuration stored in the backend database

**Decision:** Dashboard and widget config (asset, chart type, time range, colour) is
persisted in PostgreSQL. The frontend mirrors the active dashboard ID in `localStorage`
only for fast tab restoration.

**Rationale:**
- Backend storage satisfies the "persistence between sessions" requirement without
  tying the data to a single browser.
- Multiple devices / browsers see the same dashboards after login.
- `localStorage` is used only for the active-tab selection, not for chart config.

---

## ADR-008 — Recharts for frontend visualisation

**Decision:** Recharts 2.x as the chart library.

**Rationale:**
- Most mature React-native charting library with composable components.
- `ResponsiveContainer` handles resize automatically.
- Supports line, bar, tooltip, and reference lines out of the box — covers all required chart types.
- No Canvas dependency (SVG-based) — easier to inspect and style.

---

## ADR-009 — RFC 7807 error responses

**Decision:** `GlobalExceptionHandler` returns `ProblemDetail` (RFC 7807) for all errors.

**Rationale:**
- Standard format; clients can rely on `status`, `detail`, and optional `fields` extension.
- Spring Boot 3 provides `ProblemDetail` natively — no extra library needed.
- Validation errors include a `fields` map so the frontend can highlight individual form fields.
