package com.marketviz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Drives scheduled and startup price-data ingestion.
 *
 * <p>On application startup ({@link ApplicationReadyEvent}) the full incremental
 * history is fetched for all assets — only the missing tail is downloaded if data
 * already exists. Afterwards, a cron job runs at the top of every hour to keep
 * prices current.
 */
@Component
public class DataIngestionScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionScheduler.class);

    private final StooqFetchService fetchService;

    public DataIngestionScheduler(StooqFetchService fetchService) {
        this.fetchService = fetchService;
    }

    /** Runs once after the application context is fully started. */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Starting initial data ingestion on startup");
        fetchService.fetchAllAssets();
    }

    /**
     * Runs once per day at 08:00 server time.
     *
     * <p>Alpha Vantage's free tier allows 25 requests per day; fetching 6 assets
     * once daily consumes 6 of those, leaving headroom for manual refreshes.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void daily() {
        log.info("Running daily price data refresh");
        fetchService.fetchAllAssets();
    }
}
