package com.marketviz.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A single daily OHLCV data point for an {@link Asset}.
 *
 * <p>The composite unique constraint on {@code (asset_id, timestamp)} makes
 * repeated ingestion runs idempotent — inserting a row that already exists
 * raises a {@link org.springframework.dao.DataIntegrityViolationException} that
 * the fetch service catches and silently ignores.
 */
@Entity
@Table(
    name = "price_points",
    uniqueConstraints = @UniqueConstraint(columnNames = {"asset_id", "timestamp"})
)
public class PricePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false)
    private LocalDate timestamp;

    @Column(precision = 18, scale = 6)
    private BigDecimal open;

    @Column(precision = 18, scale = 6)
    private BigDecimal high;

    @Column(precision = 18, scale = 6)
    private BigDecimal low;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal close;

    private Long volume;

    public PricePoint() {}

    public PricePoint(Asset asset, LocalDate timestamp,
                      BigDecimal open, BigDecimal high, BigDecimal low,
                      BigDecimal close, Long volume) {
        this.asset     = asset;
        this.timestamp = timestamp;
        this.open      = open;
        this.high      = high;
        this.low       = low;
        this.close     = close;
        this.volume    = volume;
    }

    public Long getId() { return id; }

    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }

    public LocalDate getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDate timestamp) { this.timestamp = timestamp; }

    public BigDecimal getOpen() { return open; }
    public void setOpen(BigDecimal open) { this.open = open; }

    public BigDecimal getHigh() { return high; }
    public void setHigh(BigDecimal high) { this.high = high; }

    public BigDecimal getLow() { return low; }
    public void setLow(BigDecimal low) { this.low = low; }

    public BigDecimal getClose() { return close; }
    public void setClose(BigDecimal close) { this.close = close; }

    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }
}
