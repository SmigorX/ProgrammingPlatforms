package com.marketviz.service;

import com.marketviz.dto.asset.AssetResponse;
import com.marketviz.dto.asset.PricePointResponse;
import com.marketviz.exception.ResourceNotFoundException;
import com.marketviz.model.Asset;
import com.marketviz.model.TimeRange;
import com.marketviz.repository.AssetRepository;
import com.marketviz.repository.PricePointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Read-only access to the asset catalogue and associated price history.
 *
 * <p>Price data is served directly from the local database; ingestion from
 * stooq.pl is handled asynchronously by {@link StooqFetchService}.
 */
@Service
@Transactional(readOnly = true)
public class AssetService {

    private final AssetRepository assetRepository;
    private final PricePointRepository pricePointRepository;

    public AssetService(AssetRepository assetRepository,
                        PricePointRepository pricePointRepository) {
        this.assetRepository    = assetRepository;
        this.pricePointRepository = pricePointRepository;
    }

    /** Returns all active assets in the catalogue. */
    public List<AssetResponse> findAll() {
        return assetRepository.findAllByActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Returns daily price data for the given asset over the requested time window.
     *
     * @throws ResourceNotFoundException if no asset with the given ID exists
     */
    public List<PricePointResponse> findPrices(Long assetId, TimeRange timeRange) {
        assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));

        return pricePointRepository
                .findByAssetIdAndTimestampBetweenOrderByTimestampAsc(
                        assetId, timeRange.startDate(), LocalDate.now()
                )
                .stream()
                .map(p -> new PricePointResponse(
                        p.getTimestamp(), p.getOpen(), p.getHigh(),
                        p.getLow(), p.getClose(), p.getVolume()
                ))
                .toList();
    }

    /**
     * Fetches an asset by primary key or throws {@link ResourceNotFoundException}.
     * Used internally by services that need a managed entity reference.
     */
    public Asset getOrThrow(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + id));
    }

    private AssetResponse toResponse(Asset a) {
        return new AssetResponse(a.getId(), a.getSymbol(), a.getName(),
                a.getDescription(), a.getCategory());
    }
}
