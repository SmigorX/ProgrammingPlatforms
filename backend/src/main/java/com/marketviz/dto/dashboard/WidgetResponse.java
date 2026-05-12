package com.marketviz.dto.dashboard;

import com.marketviz.dto.asset.AssetResponse;
import com.marketviz.model.ChartType;
import com.marketviz.model.TimeRange;

/** Widget projection returned as part of a {@link DashboardResponse}. */
public record WidgetResponse(
        Long id,
        AssetResponse asset,
        ChartType chartType,
        TimeRange timeRange,
        String color,
        String title,
        int displayOrder
) {}
