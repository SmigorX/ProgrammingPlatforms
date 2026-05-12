package com.marketviz.dto.asset;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Daily OHLCV data point returned by the prices endpoint. */
public record PricePointResponse(
        LocalDate date,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        Long volume
) {}
