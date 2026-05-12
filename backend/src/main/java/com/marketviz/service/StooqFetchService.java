package com.marketviz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketviz.model.Asset;
import com.marketviz.model.PricePoint;
import com.marketviz.repository.AssetRepository;
import com.marketviz.repository.PricePointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fetches daily OHLCV price data from the
 * <a href="https://twelvedata.com">Twelve Data</a> {@code time_series} endpoint
 * and persists it locally.
 *
 * <h3>Endpoint</h3>
 * <pre>
 *   https://api.twelvedata.com/time_series
 *       ?symbol={symbol}&amp;interval=1day&amp;outputsize=5000&amp;apikey={key}
 * </pre>
 *
 * <h3>Why Twelve Data</h3>
 * <ul>
 *   <li>stooq.pl — requires a per-session CAPTCHA-solved token; not usable server-side.</li>
 *   <li>Yahoo Finance v8 — blocks container/server IPs with HTTP 429 regardless of rate.</li>
 *   <li>Alpha Vantage — {@code outputsize=full} (needed for 5-year history) is a paid feature.</li>
 *   <li>Twelve Data free tier allows {@code outputsize=5000} (~20 years of daily data),
 *       800 credits/day, 8 credits/minute — sufficient for 6 assets.</li>
 * </ul>
 *
 * <h3>Rate limiting</h3>
 * The free tier allows 8 credits per minute (1 credit = 1 request).
 * A 9-second inter-asset delay in {@link #fetchAllAssets()} keeps burst rate within that limit.
 *
 * <h3>Idempotency</h3>
 * The unique constraint on {@code (asset_id, timestamp)} turns duplicate inserts into
 * {@link DataIntegrityViolationException}s that are caught per-row so the rest of the
 * batch still commits.
 */
@Service
public class StooqFetchService {

    private static final Logger log = LoggerFactory.getLogger(StooqFetchService.class);

    private static final String TD_URL =
            "https://api.twelvedata.com/time_series" +
            "?symbol={symbol}&interval=1day&outputsize=5000&apikey={apikey}";

    @Value("${app.twelve-data.api-key}")
    private String apiKey;

    private final AssetRepository assetRepository;
    private final PricePointRepository pricePointRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public StooqFetchService(AssetRepository assetRepository,
                              PricePointRepository pricePointRepository,
                              ObjectMapper objectMapper) {
        this.assetRepository      = assetRepository;
        this.pricePointRepository = pricePointRepository;
        this.objectMapper         = objectMapper;
        this.restTemplate         = buildRestTemplate();
    }

    /**
     * Fetches incremental price data for every active asset.
     * A 9-second pause between assets respects the 8 credits/minute free-tier limit.
     * Per-asset failures are logged but do not abort the remaining assets.
     */
    @Transactional
    public void fetchAllAssets() {
        var assets = assetRepository.findAllByActiveTrue();
        for (int i = 0; i < assets.size(); i++) {
            try {
                fetchAsset(assets.get(i));
            } catch (Exception e) {
                log.error("Failed to fetch data for {}: {}", assets.get(i).getSymbol(), e.getMessage());
            }
            if (i < assets.size() - 1) {
                try { Thread.sleep(9_000); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            }
        }
    }

    /**
     * Fetches and persists price data for a single asset.
     *
     * <p>Twelve Data returns up to {@code outputsize=5000} rows (≈20 years of daily data)
     * in reverse-chronological order. Rows already in the database are silently skipped
     * via the duplicate constraint.
     *
     * @param asset the asset to update
     */
    @Transactional
    public void fetchAsset(Asset asset) {
        log.info("Fetching {} ({})", asset.getSymbol(), asset.getStooqSymbol());

        var json = restTemplate.getForObject(TD_URL, String.class,
                asset.getStooqSymbol(), apiKey);

        if (json == null || json.isBlank()) {
            log.warn("Empty response from Twelve Data for {}", asset.getSymbol());
            return;
        }

        int saved = parseAndSave(asset, json);
        log.info("Saved {} new price points for {}", saved, asset.getSymbol());
    }

    @SuppressWarnings("unchecked")
    private int parseAndSave(Asset asset, String json) {
        try {
            var root = objectMapper.readValue(json, Map.class);

            if ("error".equals(root.get("status"))) {
                log.warn("Twelve Data error for {}: {}", asset.getSymbol(), root.get("message"));
                return 0;
            }

            var values = (List<Map<String, String>>) root.get("values");
            if (values == null || values.isEmpty()) {
                log.warn("No time series data in Twelve Data response for {}", asset.getSymbol());
                return 0;
            }

            List<PricePoint> batch = new ArrayList<>(values.size());
            for (var entry : values) {
                try {
                    var date   = LocalDate.parse(entry.get("datetime"));
                    var open   = toDecimal(entry.get("open"));
                    var high   = toDecimal(entry.get("high"));
                    var low    = toDecimal(entry.get("low"));
                    var close  = toDecimal(entry.get("close"));
                    var volume = toLong(entry.get("volume"));
                    if (close != null) {
                        batch.add(new PricePoint(asset, date, open, high, low, close, volume));
                    }
                } catch (Exception e) {
                    log.debug("Skipping malformed entry for {}: {}", asset.getSymbol(), entry.get("datetime"));
                }
            }

            int saved = 0;
            for (var pp : batch) {
                try {
                    pricePointRepository.save(pp);
                    saved++;
                } catch (DataIntegrityViolationException ignored) {
                    // duplicate — already stored from a previous run
                }
            }
            return saved;

        } catch (Exception e) {
            log.error("Failed to parse Twelve Data response for {}: {}", asset.getSymbol(), e.getMessage());
            return 0;
        }
    }

    private BigDecimal toDecimal(String s) {
        return (s == null || s.isBlank()) ? null : new BigDecimal(s.trim());
    }

    private Long toLong(String s) {
        return (s == null || s.isBlank()) ? null : Long.parseLong(s.trim());
    }

    private static RestTemplate buildRestTemplate() {
        var rt = new RestTemplate();
        rt.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set(HttpHeaders.USER_AGENT,
                    "Mozilla/5.0 (X11; Linux x86_64; rv:124.0) Gecko/20100101 Firefox/124.0");
            return execution.execute(request, body);
        });
        return rt;
    }
}
