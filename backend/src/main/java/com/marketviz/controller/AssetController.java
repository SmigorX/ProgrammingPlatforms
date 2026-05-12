package com.marketviz.controller;

import com.marketviz.dto.asset.AssetResponse;
import com.marketviz.dto.asset.PricePointResponse;
import com.marketviz.model.TimeRange;
import com.marketviz.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes the asset catalogue and price history to authenticated clients.
 */
@RestController
@RequestMapping("/api/assets")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Assets", description = "Asset catalogue and price history")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    @Operation(summary = "List all active assets")
    public List<AssetResponse> list() {
        return assetService.findAll();
    }

    @GetMapping("/{id}/prices")
    @Operation(summary = "Get daily OHLCV data for an asset over a time window")
    public List<PricePointResponse> prices(
            @PathVariable Long id,
            @Parameter(description = "Time window — default ONE_YEAR")
            @RequestParam(defaultValue = "ONE_YEAR") TimeRange range
    ) {
        return assetService.findPrices(id, range);
    }
}
