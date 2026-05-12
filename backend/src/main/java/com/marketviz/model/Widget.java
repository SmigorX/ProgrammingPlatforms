package com.marketviz.model;

import jakarta.persistence.*;

/**
 * A single visualization panel within a {@link Dashboard}.
 *
 * <p>Each widget is bound to one {@link Asset} and rendered according to a
 * {@link ChartType} over a named {@link TimeRange}. The {@code color} field
 * carries a CSS hex string (e.g. {@code "#3b82f6"}) used by the frontend chart
 * renderer. {@code displayOrder} controls left-to-right / top-to-bottom ordering
 * on the dashboard grid.
 */
@Entity
@Table(name = "widgets")
public class Widget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dashboard_id", nullable = false)
    private Dashboard dashboard;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "chart_type", nullable = false, length = 20)
    private ChartType chartType;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_range", nullable = false, length = 20)
    private TimeRange timeRange;

    @Column(nullable = false, length = 7)
    private String color = "#3b82f6";

    @Column(length = 100)
    private String title;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    public Long getId() { return id; }

    public Dashboard getDashboard() { return dashboard; }
    public void setDashboard(Dashboard dashboard) { this.dashboard = dashboard; }

    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }

    public ChartType getChartType() { return chartType; }
    public void setChartType(ChartType chartType) { this.chartType = chartType; }

    public TimeRange getTimeRange() { return timeRange; }
    public void setTimeRange(TimeRange timeRange) { this.timeRange = timeRange; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
