package com.marketviz.controller;

import com.marketviz.service.StooqFetchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Administrative operations restricted to users with the {@code ADMIN} role.
 */
@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin", description = "Administrative operations (ADMIN role required)")
public class AdminController {

    private final StooqFetchService fetchService;

    public AdminController(StooqFetchService fetchService) {
        this.fetchService = fetchService;
    }

    @PostMapping("/fetch")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Manually trigger a full data ingestion run for all assets")
    public void triggerFetch() {
        fetchService.fetchAllAssets();
    }
}
