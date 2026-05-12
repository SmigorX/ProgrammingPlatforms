package com.marketviz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the MarketViz backend.
 *
 * <p>MarketViz is a market data visualization platform that ingests commodity and
 * stock prices from stooq.pl, stores them in PostgreSQL, and serves them via a
 * REST API consumed by the React frontend.
 *
 * <p>{@link EnableScheduling} enables the hourly price-data refresh job defined in
 * {@link com.marketviz.service.DataIngestionScheduler}.
 */
@SpringBootApplication
@EnableScheduling
public class MarketVizApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketVizApplication.class, args);
    }
}
