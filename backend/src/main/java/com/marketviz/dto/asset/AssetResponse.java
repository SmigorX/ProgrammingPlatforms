package com.marketviz.dto.asset;

/** Projection of {@link com.marketviz.model.Asset} for API responses. */
public record AssetResponse(Long id, String symbol, String name, String description, String category) {}
