package com.marketviz.dto.dashboard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload for creating or renaming a dashboard. */
public record DashboardRequest(
        @NotBlank @Size(max = 100) String name,
        boolean isDefault
) {}
