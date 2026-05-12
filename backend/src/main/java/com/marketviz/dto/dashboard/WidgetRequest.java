package com.marketviz.dto.dashboard;

import com.marketviz.model.ChartType;
import com.marketviz.model.TimeRange;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Payload for adding or updating a widget on a dashboard. */
public record WidgetRequest(
        @NotNull Long assetId,
        @NotNull ChartType chartType,
        @NotNull TimeRange timeRange,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "must be a valid CSS hex colour") String color,
        @Size(max = 100) String title,
        int displayOrder
) {}
