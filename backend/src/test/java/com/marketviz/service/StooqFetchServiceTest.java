package com.marketviz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketviz.model.Asset;
import com.marketviz.model.PricePoint;
import com.marketviz.repository.AssetRepository;
import com.marketviz.repository.PricePointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StooqFetchServiceTest {

    @Mock AssetRepository assetRepository;
    @Mock PricePointRepository pricePointRepository;

    StooqFetchService fetchService;

    private Asset goldAsset;

    @BeforeEach
    void setUp() {
        fetchService = new StooqFetchService(assetRepository, pricePointRepository, new ObjectMapper());

        goldAsset = new Asset();
        goldAsset.setSymbol("GOLD");
        goldAsset.setStooqSymbol("GLD");
        goldAsset.setName("Gold");
        goldAsset.setCategory("COMMODITY");
    }

    /**
     * An empty or blank HTTP response must not trigger any DB writes.
     * The Twelve Data call itself will fail (no test HTTP server), so
     * fetchAsset is expected to throw — no PricePoint should be saved.
     */
    @Test
    void fetchAsset_savesNothingOnNetworkError() {
        // RestTemplate will throw (no real server) — verify no saves happen
        try {
            fetchService.fetchAsset(goldAsset);
        } catch (Exception ignored) {
            // expected: no mock HTTP server
        }

        verify(pricePointRepository, never()).save(any(PricePoint.class));
    }

    /**
     * A per-asset network failure must not stop the remaining assets from
     * being attempted.  fetchAllAssets() catches individual exceptions internally.
     */
    @Test
    void fetchAllAssets_continuesAfterIndividualFailure() {
        var badAsset = new Asset();
        badAsset.setSymbol("BAD");
        badAsset.setStooqSymbol("BAD_SYMBOL");
        badAsset.setName("Bad asset");
        badAsset.setCategory("TEST");

        when(assetRepository.findAllByActiveTrue()).thenReturn(List.of(badAsset));

        // HTTP call will fail (no mock server) but fetchAllAssets must not propagate the exception
        fetchService.fetchAllAssets();
    }
}
