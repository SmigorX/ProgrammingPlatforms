package com.marketviz.dto.dashboard;

import java.time.Instant;
import java.util.List;

/** Dashboard projection returned by the API, including its full widget list. */
public record DashboardResponse(
        Long id,
        String name,
        boolean isDefault,
        Instant createdAt,
        Instant updatedAt,
        List<WidgetResponse> widgets
) {}
