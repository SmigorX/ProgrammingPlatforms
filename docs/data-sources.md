# Data Sources

## Twelve Data

All market data is fetched from the [Twelve Data](https://twelvedata.com) `time_series`
endpoint. A **free API key** is required and can be obtained at
[twelvedata.com](https://twelvedata.com) (register, then Dashboard → API Keys; no credit card).

### Endpoint

```
GET https://api.twelvedata.com/time_series
    ?symbol={symbol}
    &interval=1day
    &outputsize=5000
    &apikey={key}
```

| Parameter    | Description                                                    |
| ------------ | -------------------------------------------------------------- |
| `symbol`     | Ticker symbol (ETF or stock, see table below)                  |
| `interval`   | `1day` — daily OHLCV bars                                      |
| `outputsize` | Number of data points to return; 5000 ≈ 20 years of daily data |
| `apikey`     | Free API key from twelvedata.com                               |

### Response format

JSON object with a `values` array in reverse-chronological order:

```json
{
  "meta": { "symbol": "GLD", "interval": "1day", ... },
  "values": [
    {
      "datetime": "2024-01-10",
      "open": "184.50000",
      "high": "185.00000",
      "low":  "183.50000",
      "close": "184.80000",
      "volume": "5000000"
    }
  ],
  "status": "ok"
}
```

On error (invalid key, rate limit exceeded) the response has `"status": "error"` and a
`"message"` field. The service detects this and logs a warning without crashing.

### Rate limits (free tier)

| Limit          | Value | How we handle it                                     |
| -------------- | ----- | ---------------------------------------------------- |
| Credits/minute | 8     | 9-second pause between assets in `fetchAllAssets()`  |
| Credits/day    | 800   | Daily cron (`0 0 8 * * *`); 6 assets/run = 6 credits |

### Tracked instruments

| Platform symbol | Name                  | Twelve Data ticker | Category  | Notes                                 |
| --------------- | --------------------- | ------------------ | --------- | ------------------------------------- |
| `GOLD`          | Gold (SPDR ETF)       | `GLD`              | COMMODITY | SPDR Gold Shares, tracks gold bullion |
| `SILVER`        | Silver (iShares ETF)  | `SLV`              | COMMODITY | iShares Silver Trust                  |
| `OIL_BRENT`     | Brent Crude Oil (ETF) | `BNO`              | COMMODITY | United States Brent Oil Fund          |
| `OIL_WTI`       | WTI Crude Oil (ETF)   | `USO`              | COMMODITY | United States Oil Fund                |
| `NAT_GAS`       | Natural Gas (ETF)     | `UNG`              | COMMODITY | United States Natural Gas Fund        |
| `MU`            | Micron Technology     | `MU`               | STOCK     | NASDAQ-listed; DRAM/NAND proxy        |

### Adding a new instrument

1. Find the ticker at [twelvedata.com/stocks](https://twelvedata.com/stocks) or the symbol search.
2. Add a new Flyway migration, e.g. `V6__add_copper.sql`:

```sql
INSERT INTO assets (symbol, name, description, category, stooq_symbol, active)
VALUES ('COPPER', 'Copper (ETF)', 'iPath Bloomberg Copper ETN', 'COMMODITY', 'JJC', true);
```

_(The `stooq_symbol` column holds the Twelve Data ticker. The column name is a historical
artefact from an earlier iteration that used stooq.pl)_

3. Restart the backend (or call `POST /api/admin/fetch` as an ADMIN user) to trigger an
   immediate ingestion run.

---

## Why not stooq.pl, Yahoo Finance, or Alpha Vantage?

Three earlier implementations were evaluated and abandoned:

| Source               | Outcome                                                                           |
| -------------------- | --------------------------------------------------------------------------------- |
| **stooq.pl**         | Returns a CAPTCHA challenge page from server code; per-session token required.    |
| **Yahoo Finance v8** | IP-level HTTP 429 block for all container/server requests, regardless of rate.    |
| **Alpha Vantage**    | `outputsize=full` (needed for 5-year history) is a paid feature on the free tier. |

Twelve Data was chosen because its free tier supports `outputsize=5000` (~20 years of daily
data), does not block cloud IPs, and provides 800 API credits/day — well above the 6/day
needed for the six tracked assets.

---

## DRAM pricing note

No free public API for DRAM spot prices exists. Micron Technology (MU) stock is used as a
proxy because:

- Micron is the world's largest pure-play DRAM/NAND manufacturer (alongside Samsung and SK Hynix).
- Its stock price reacts quickly to DRAM supply/demand shifts, pricing cycle inflections, and
  semiconductor capex news.
- 20+ years of daily history is available for free via Twelve Data.

If actual DRAM spot data becomes available through a free API, the asset's ticker can be
updated in the database without any code changes.
