package com.marketviz.model;

import jakarta.persistence.*;
import java.util.List;

/**
 * A tradeable instrument whose price history is tracked by the platform.
 *
 * <p>{@code stooqSymbol} is the ticker passed to the stooq.pl CSV API when
 * fetching historical data. {@code category} is a free-form grouping label
 * (e.g. {@code "COMMODITY"}, {@code "STOCK"}).
 */
@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "stooq_symbol", nullable = false, length = 20)
    private String stooqSymbol;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PricePoint> pricePoints;

    public Long getId() { return id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStooqSymbol() { return stooqSymbol; }
    public void setStooqSymbol(String stooqSymbol) { this.stooqSymbol = stooqSymbol; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<PricePoint> getPricePoints() { return pricePoints; }
}
